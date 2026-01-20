package com.composeform.ui.pages.main.activaty

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import com.composeform.ui.components.MainTitle
import com.composeform.ui.pages.main.viewmodel.DynamicFormViewModel
import com.composeform.ui.theme.ComposeFormTheme
import com.composeform.utils.extensions.provideViewModel
import androidx.compose.material3.Checkbox
import androidx.compose.ui.platform.LocalContext
import com.composeform.model.schema.SchemaNode


class MainActivity : ComponentActivity() {

    private val formViewModel: DynamicFormViewModel by provideViewModel()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeFormTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FormScreen(viewModel = formViewModel)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FormScreen(viewModel: DynamicFormViewModel) {
        val schema = viewModel.schema
        val isLoading = viewModel.isLoading
        val error = viewModel.loadError

        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            when {
                isLoading -> LoadingState(Modifier.padding(innerPadding))
                error != null -> ErrorState(
                    message = error,
                    onRetry = { viewModel.load() },
                    modifier = Modifier.padding(innerPadding)
                )
                schema != null -> RecursiveFormEngine(
                    schema = schema,
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun MainPreview() {
        ComposeFormTheme {
            LoadingState(Modifier.padding())
        }
    }

}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun RecursiveFormEngine(
    schema: SchemaNode,
    viewModel: DynamicFormViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        RenderNode("", schema, viewModel)

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(24.dp))

        Button(
            onClick = {
                viewModel.schema?.let { schemaNode ->
                    val formIsValid = viewModel.validateAll(schemaNode)
                    if (formIsValid) {
                        val resultJson = viewModel.formData.toMap().toString()
                        Log.d("FormSuccess", "Payload: $resultJson")
                        Toast.makeText(context, "The form was sent successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Please correct the errors in the form.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun RenderNode(path: String, node: SchemaNode, viewModel: DynamicFormViewModel) {
    val error = viewModel.errors[path]
    val value = viewModel.formData[path]

    when (node) {
        is SchemaNode.ObjectNode -> {
            node.title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            node.properties.forEach { (key, child) ->
                val newPath = if (path.isEmpty()) key else "$path.$key"
                RenderNode(newPath, child, viewModel)
            }
        }

        is SchemaNode.StringNode -> {
            if (node.enum != null) {
                DropdownRenderer(
                    label = node.title ?: path,
                    options = node.enum,
                    selected = value as? String,
                    error = error
                ) { viewModel.updateValue(path, it, node) }
            } else if (node.format == "date") {
                DateRenderer(
                    label = node.title ?: path,
                    value = value as? String ?: "",
                    error = error
                ) { viewModel.updateValue(path, it, node) }
            } else {
                StringRenderer(
                    label = node.title ?: path,
                    value = value as? String ?: "",
                    error = error
                ) { viewModel.updateValue(path, it, node) }
            }
        }

        is SchemaNode.BooleanNode -> {
            CheckboxRow(
                label = node.title ?: path,
                checked = value as? Boolean ?: false
            ) { viewModel.updateValue(path, it, node) }
        }

        is SchemaNode.IntegerNode -> {
            IntegerRenderer(
                label = node.title ?: path,
                value = value?.toString() ?: "",
                error = error
            ) {
                viewModel.updateValue(path, it.toIntOrNull(), node)
            }
        }

        is SchemaNode.NumberNode -> {
            NumberRenderer(
                label = node.title ?: path,
                value = value?.toString() ?: "",
                error = error
            ) {
                viewModel.updateValue(path, it.toDoubleOrNull(), node)
            }
        }

        is SchemaNode.ArrayNode -> {
            // Assuming array items are always strings for simplicity based on schemaString()
            ArrayStringRenderer(
                label = node.title ?: path,
                values = (value as? List<String>) ?: emptyList(),
                error = error
            ) { updatedList ->
                viewModel.updateValue(path, updatedList, node)
            }
        }
    }
}

@Composable
fun StringRenderer(label: String, value: String, error: String?, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownRenderer(
    label: String,
    options: List<String>,
    selected: String?,
    error: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val text = selected ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = text,
            onValueChange = {},
            readOnly = true,
            isError = error != null,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun IntegerRenderer(label: String, value: String, error: String?, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = error != null,
        supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        singleLine = true
    )
}


@Composable
fun NumberRenderer(label: String, value: String, error: String?, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = error != null,
        supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        singleLine = true
    )
}


@Composable
fun DateRenderer(label: String, value: String, error: String?, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = error != null,
        supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        singleLine = true
    )
}

@Composable
fun CheckboxRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun ArrayStringRenderer(label: String, values: List<String>, error: String?, onValuesChange: (List<String>) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        values.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item,
                    onValueChange = { newValue ->
                        val newList = values.toMutableList()
                        newList[index] = newValue
                        onValuesChange(newList)
                    },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    singleLine = true
                )
                Button(onClick = { onValuesChange(values.toMutableList().also { it.removeAt(index) }) }) {
                    Text("Remove")
                }
            }
        }
        Button(onClick = { onValuesChange(values + "") }, modifier = Modifier.fillMaxWidth()) {
            Text("Add Item")
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}