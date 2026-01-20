package com.composeform.data.remote.source

import android.util.Log
import com.composeform.data.error_handler.DataError
import com.composeform.data.error_handler.Result
import com.composeform.data.remote.api.FormApi
import com.composeform.model.schema.SchemaNode
import com.composeform.utils.extensions.findErrorSchemaNode
import com.composeform.utils.extensions.findErrorJsonObject
import com.composeform.utils.extensions.getHardcodedData
import com.composeform.utils.extensions.getHardcodedSchema
import kotlinx.serialization.json.JsonObject
import retrofit2.HttpException

class RemoteDataSource(private val formApi: FormApi) {

    suspend fun fetchSchema(): Result<SchemaNode?, DataError> {
      //  return remoteFetchSchema()
        Log.e("hardcodedSchema", hardcodedSchema().toString())
        return hardcodedSchema()
    }

    suspend fun fetchPrefillData(dataUrl: String): Result<JsonObject?, DataError> {
      //  return remoteFetchPrefillData(dataUrl)
        return hardcodedData()
    }

    suspend fun remoteFetchPrefillData(dataUrl: String): Result<JsonObject?, DataError.Network> {
        return try {
            Result.Success(formApi.fetchPrefillData(dataUrl))
        } catch (e: HttpException) {
            return findErrorJsonObject(e)
        } catch (e: Exception) {
            Result.Error(DataError.Network.Unknown)
        }
    }

    private suspend fun remoteFetchSchema(): Result<SchemaNode?, DataError.Network> {
        return try {
            Result.Success(formApi.fetchSchema())
        } catch (e: HttpException) {
            return findErrorSchemaNode(e)
        } catch (e: Exception) {
            Result.Error(DataError.Network.Unknown)
        }
    }

    private suspend fun hardcodedSchema(): Result<SchemaNode?, DataError> {
        return getHardcodedSchema()
    }

    private suspend fun hardcodedData(): Result<JsonObject?, DataError> {
        return getHardcodedData()
    }
}