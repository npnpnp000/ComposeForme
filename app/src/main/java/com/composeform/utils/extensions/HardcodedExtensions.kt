package com.composeform.utils.extensions

import android.util.Log
import com.composeform.data.error_handler.DataError
import com.composeform.data.error_handler.Result
import com.composeform.model.schema.SchemaNode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray


fun getHardcodedSchema(): Result<SchemaNode?, DataError> {

    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; classDiscriminator = "type" }
    try {
        val jsonObject = json.decodeFromString<JsonObject>(schemaString())
        val rootTitle = jsonObject["title"]?.jsonPrimitive?.contentOrNull ?: jsonObject["description"]?.jsonPrimitive?.contentOrNull

        val propertiesElement = jsonObject["properties"]?.jsonObject
        val properties = propertiesElement?.mapValues { (propKey, propValue) ->
            createSchemaNodeFromElement(propKey, propValue, json)
        } ?: emptyMap()

        return Result.Success(SchemaNode.ObjectNode(title = rootTitle, properties = properties))
    } catch (e: Exception) {
        Log.e("Schemadecode", e.toString())
        return Result.Error(DataError.Local.Decoding_error)
    }
}

fun getHardcodedData(): Result<JsonObject?, DataError> {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    try {
        return Result.Success(json.decodeFromString<JsonObject>(dataString()))
    } catch (e: Exception) {
        Log.e("Datacode",e.toString())
        return Result.Error(DataError.Local.Decoding_error)
    }

}

private fun schemaString() =
    "{\n" +
            "  \"\$id\": \"https://example.com/job-posting.schema.json\",\n" +
            "  \"\$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
            "  \"description\": \"A representation of a job posting\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"required\": [\"title\", \"company\", \"location\", \"description\"],\n" +
            "  \"properties\": {\n" +
            "    \"title\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"company\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"location\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"description\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"employmentType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"salary\": {\n" +
            "      \"type\": \"number\",\n" +
            "      \"minimum\": 0\n" +
            "    },\n" +
            "    \"applicationDeadline\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"format\": \"date\"\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "\n"

private fun dataString() =
    "{\n" +
            "  \"title\": \"Software Engineer\",\n" +
            "  \"company\": \"Tech Solutions Inc.\",\n" +
            "  \"location\": \"Cityville\",\n" +
            "  \"description\": \"Join our team as a software engineer...\",\n" +
            "  \"employmentType\": \"Full-time\",\n" +
            "  \"salary\": 80000,\n" +
            "  \"applicationDeadline\": \"2023-09-15\"\n" +
            "}"

private fun createSchemaNodeFromElement(
    key: String?,
    element: JsonElement,
    json: Json
): SchemaNode {
    val type = element.jsonObject["type"]?.jsonPrimitive?.content

    return when (type) {
        "string" -> {
            val maxLength = element.jsonObject["maxLength"]?.jsonPrimitive?.intOrNull
            val format = element.jsonObject["format"]?.jsonPrimitive?.content
            json.decodeFromJsonElement<SchemaNode.StringNode>(element).copy(title = key, maxLength = maxLength, format = format)
        }
        "integer" -> {
            val minimum = element.jsonObject["minimum"]?.jsonPrimitive?.intOrNull
            val maximum = element.jsonObject["maximum"]?.jsonPrimitive?.intOrNull
            json.decodeFromJsonElement<SchemaNode.IntegerNode>(element).copy(title = key, minimum = minimum, maximum = maximum)
        }
        "number" -> {
            val minimum = element.jsonObject["minimum"]?.jsonPrimitive?.doubleOrNull
            val maximum = element.jsonObject["maximum"]?.jsonPrimitive?.doubleOrNull
            json.decodeFromJsonElement<SchemaNode.NumberNode>(element).copy(title = key, minimum = minimum, maximum = maximum)
        }
        "boolean" -> json.decodeFromJsonElement<SchemaNode.BooleanNode>(element).copy(title = key)
        "object" -> {
            val propertiesElement = element.jsonObject["properties"]?.jsonObject
            val properties = propertiesElement?.mapValues { (propKey, propValue) ->
                createSchemaNodeFromElement(propKey, propValue, json)
            } ?: emptyMap()
            val required = element.jsonObject["required"]?.jsonArray?.map { it.jsonPrimitive.content }
            json.decodeFromJsonElement<SchemaNode.ObjectNode>(element).copy(title = key, properties = properties, required = required)
        }
        "array" -> {
            val itemsElement = element.jsonObject["items"]
            val items = if (itemsElement != null) createSchemaNodeFromElement(null, itemsElement, json) else null
            json.decodeFromJsonElement<SchemaNode.ArrayNode>(element).copy(title = key, items = items)
        }
        else -> {
            // Handle cases where 'type' is missing but '$ref' is present, assuming it refers to an object.
            if (element.jsonObject.containsKey("\$ref")) {
                // For a hardcoded schema example, we'll treat it as an ObjectNode with no explicit properties here.
                // In a real-world scenario, you might want to fetch and parse the referenced schema.
                SchemaNode.ObjectNode(title = key, properties = emptyMap())
            } else {
                Log.e("SchemaParsing", "Unknown schema node element: $element")
                throw IllegalArgumentException("Unknown schema node type: $type")
            }
        }
    }
}