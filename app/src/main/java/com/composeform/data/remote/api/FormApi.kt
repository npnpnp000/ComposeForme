package com.composeform.data.remote.api

import com.composeform.model.schema.SchemaNode
import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

interface FormApi {
    @Headers("Accept: application/json")
    @GET("schema")
    suspend fun fetchSchema(): SchemaNode

    @Headers("Accept: application/json")
    @GET
    suspend fun fetchPrefillData(@Url url: String = "https://datatest.com/data"): JsonObject
}
