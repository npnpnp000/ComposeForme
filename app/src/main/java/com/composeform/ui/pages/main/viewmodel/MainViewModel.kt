package com.composeform.ui.pages.main.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeform.data.error_handler.Result
import com.composeform.data.repositories.Repository
import com.composeform.model.schema.SchemaNode
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Locale

class DynamicFormViewModel(private val repository: Repository) : ViewModel() {

    var schema: SchemaNode? by mutableStateOf(null)
        private set
    var isLoading by mutableStateOf(true)
    var loadError by mutableStateOf<String?>(null)

    // Store values: "address.city" -> "Tel Aviv"
    val formData = mutableStateMapOf<String, Any?>()
    // Store errors: "address.city" -> "Field too short"
    val errors = mutableStateMapOf<String, String>()

    private val prefillUrl = "https://datatest.com/data"

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            loadError = null
            when (val schemaResult = repository.fetchSchema()) {
                is Result.Success -> {
                    schema = schemaResult.data
                    schemaResult.data?.let { schemaNode ->
                        fetchPrefill(schemaNode)
                    } ?: run {
                        loadError = "Schema missing"
                    }
                }

                is Result.Error -> loadError = "Failed to load schema (${schemaResult.error})"
            }
            isLoading = false
        }
    }

    private suspend fun fetchPrefill(root: SchemaNode) {
        when (val dataResult = repository.fetchPrefillData(prefillUrl)) {
            is Result.Success -> dataResult.data?.let { prefillForm(it, root) }
            is Result.Error -> loadError = "Failed to load initial data (${dataResult.error})"
        }
    }

    private fun prefillForm(data: JsonObject, root: SchemaNode) {
        formData.clear()
        flatten(data, root, "")
    }

    private fun flatten(element: JsonElement?, node: SchemaNode, currentPath: String) {
        when (node) {
            is SchemaNode.ObjectNode -> {
                val obj = element?.jsonObject
                node.properties.forEach { (key, child) ->
                    val childPath = if (currentPath.isEmpty()) key else "$currentPath.$key"
                    flatten(obj?.get(key), child, childPath)
                }
            }

            is SchemaNode.StringNode -> {
                formData[currentPath] = element?.jsonPrimitive?.contentOrNull ?: ""
            }

            is SchemaNode.IntegerNode -> {
                formData[currentPath] = element?.jsonPrimitive?.intOrNull
            }
            is SchemaNode.NumberNode -> {
                formData[currentPath] = element?.jsonPrimitive?.doubleOrNull
            }

            is SchemaNode.BooleanNode -> {
                formData[currentPath] = element?.jsonPrimitive?.booleanOrNull ?: false
            }

            is SchemaNode.ArrayNode -> {
                val arr = element?.jsonArray
                // For array of strings, flatten as List<String>
                formData[currentPath] = arr?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList<String>()
            }
        }
    }

    fun updateValue(path: String, value: Any?, node: SchemaNode) {
        formData[path] = value
        schema?.let { validateAll(it) }
    }

    fun validateAll(node: SchemaNode, currentPath: String = "", isRequired: Boolean = false): Boolean {
        Log.d("Validation", "validateAll called for path: $currentPath, isRequired: $isRequired")
        if (currentPath.isEmpty()) {
            errors.clear()
            Log.d("Validation", "errors.clear() executed at root validation.")
        }
        var isValid = true
        when (node) {
            is SchemaNode.ObjectNode -> {
                node.properties.forEach { (key, child) ->
                    val path = if (currentPath.isEmpty()) key else "$currentPath.$key"
                    val isChildRequired = node.required?.contains(key) ?: false
                    Log.d("Validation", "  ObjectNode child: $key, path: $path, isChildRequired: $isChildRequired")
                    if (!validateAll(child, path, isChildRequired)) isValid = false
                }
            }
            else -> {
                // Use the isRequired parameter passed from the parent's required list
                Log.d("Validation", "  Non-ObjectNode path: $currentPath, isRequired: $isRequired")
                val error = validateNode(currentPath, node, formData[currentPath], isRequired)
                if (error != null) isValid = false
            }
        }
        Log.d("Validation", "validateAll for $currentPath returning: $isValid")
        return isValid
    }

    private fun validateNode(path: String, node: SchemaNode, value: Any?, isRequired: Boolean): String? {
        Log.d("Validation", "  validateNode called for path: $path, node: ${node::class.simpleName}, value: $value, isRequired: $isRequired")
        
        // Check required field first
        if (isRequired && (value == null || (value is String && value.isEmpty()))) {
            val error = "Field is required"
            errors[path] = error
            Log.d("Validation", "    Error: Field is required for $path. Value: $value")
            return error
        }

        val error = when (node) {
            is SchemaNode.StringNode -> {
                val str = value?.toString() ?: ""
                if (node.minLength != null && str.length < node.minLength) {
                    "Must be at least ${node.minLength} characters long"
                } else if (node.maxLength != null && str.length > node.maxLength) {
                    Log.d("Validation", "    Error: MaxLength validation failed for $path. Value length: ${str.length}, MaxLength: ${node.maxLength}")
                    "Must be at most ${node.maxLength} characters long"
                } else if (node.format == "date" && str.isNotEmpty() && !isValidDate(str)) {
                    "Invalid date format. Expected YYYY-MM-DD"
                } else null
            }
            is SchemaNode.IntegerNode -> {
                val num = value?.toString()?.toIntOrNull()
                if (num == null && value != null && value.toString().isNotEmpty()) {
                    "Invalid integer value"
                } else if (num != null && node.minimum != null && num < node.minimum) {
                    "Minimum value: ${node.minimum}"
                } else if (num != null && node.maximum != null && num > node.maximum) {
                    Log.d("Validation", "    Error: Maximum value validation failed for $path. Value: $num, MaxValue: ${node.maximum}")
                    "Maximum value: ${node.maximum}"
                } else null
            }
            is SchemaNode.NumberNode -> {
                val num = value?.toString()?.toDoubleOrNull()
                if (num == null && value != null && value.toString().isNotEmpty()) {
                    "Invalid number value"
                } else if (num != null && node.minimum != null && num < node.minimum) {
                    "Minimum value: ${node.minimum}"
                } else if (num != null && node.maximum != null && num > node.maximum) {
                    Log.d("Validation", "    Error: Maximum value validation failed for $path. Value: $num, MaxValue: ${node.maximum}")
                    "Maximum value: ${node.maximum}"
                } else null
            }
            is SchemaNode.ArrayNode -> {
                if (isRequired && (value == null || (value is List<*>) && value.isEmpty())) {
                    Log.d("Validation", "    Error: Field is required (array is empty) for $path. Value: $value")
                    "Field is required"
                } else null
            }
            is SchemaNode.BooleanNode -> {
                // Boolean nodes don't have specific content validation beyond being present if required
                null
            }
            is SchemaNode.ObjectNode -> {
                // Object nodes handle their own required fields through recursive validateAll
                null
            }
        }

        if (error != null) {
            errors[path] = error
            Log.d("Validation", "    Error added for $path: $error. Current errors: ${errors.toMap()}")
        } else {
            errors.remove(path)
            Log.d("Validation", "    Error removed for $path. Current errors: ${errors.toMap()}")
        }
        return error
    }

    private fun isValidDate(dateString: String): Boolean {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.isLenient = false
            format.parse(dateString) != null
        } catch (e: Exception) {
            false
        }
    }
}
