package com.composeform.model.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class SchemaNode {
    abstract val title: String?

    @Serializable
    @SerialName("string")
    data class StringNode(
        override val title: String? = null,
        val enum: List<String>? = null,
        val minLength: Int? = null,
        val maxLength: Int? = null,
        val format: String? = null
    ) : SchemaNode()

    @Serializable
    @SerialName("integer")
    data class IntegerNode(
        override val title: String? = null,
        val minimum: Int? = null,
        val maximum: Int? = null
    ) : SchemaNode()

    @Serializable
    @SerialName("boolean")
    data class BooleanNode(
        override val title: String?
    ) : SchemaNode()

    @Serializable
    @SerialName("object")
    data class ObjectNode(
        override val title: String? = null,
        val properties: Map<String, SchemaNode>,
        val required: List<String>? = null
    ) : SchemaNode()

    @Serializable
    @SerialName("number")
    data class NumberNode(
        override val title: String? = null,
        val minimum: Double? = null,
        val maximum: Double? = null
    ) : SchemaNode()

    @Serializable
    @SerialName("array")
    data class ArrayNode(
        override val title: String? = null,
        val items: SchemaNode? = null,
        val additionalItems: Boolean? = null
    ) : SchemaNode()
}
