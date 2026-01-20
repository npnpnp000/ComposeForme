package com.composeform.dependency_injection

import com.composeform.data.remote.api.FormApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkModule {

    private const val SCHEMA_BASE_URL = "https://schematest.com/"
    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }
    }

    fun provideFormApi(): FormApi {
        return provideRetrofitClient().create(FormApi::class.java)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun provideRetrofitClient(): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(SCHEMA_BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}