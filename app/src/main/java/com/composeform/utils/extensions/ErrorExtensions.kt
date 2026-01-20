@file:Suppress("UNCHECKED_CAST")

package com.composeform.utils.extensions

import com.composeform.data.error_handler.DataError
import com.composeform.data.error_handler.DataError.Network.*
import com.composeform.data.error_handler.Result
import com.composeform.model.schema.SchemaNode
import kotlinx.serialization.json.JsonObject
import retrofit2.HttpException

    fun findErrorSchemaNode(e: HttpException): Result<SchemaNode?, DataError.Network> {
        return  findError(e) as Result<SchemaNode?, DataError.Network>

    }
fun findErrorJsonObject(e: HttpException): Result<JsonObject?, DataError.Network> {
    return  findError(e) as Result<JsonObject?, DataError.Network>

}

private fun findError(e: HttpException): Result<Any?, DataError.Network> {
    return when (e.code()) {

        // Client Errors
        BadRequest.code -> Result.Error(BadRequest)
        Unauthorized.code -> Result.Error(Unauthorized)
        PaymentRequired.code -> Result.Error(PaymentRequired)
        Forbidden.code -> Result.Error(Forbidden)
        NotFound.code -> Result.Error(NotFound)
        MethodNotAllowed.code -> Result.Error(MethodNotAllowed)
        NotAcceptable.code -> Result.Error(NotAcceptable)
        ProxyAuthenticationRequired.code -> Result.Error(ProxyAuthenticationRequired)
        RequestTimeout.code -> Result.Error(RequestTimeout)
        Conflict.code -> Result.Error(Conflict)
        Gone.code -> Result.Error(Gone)
        LengthRequired.code -> Result.Error(LengthRequired)
        PreconditionFailed.code -> Result.Error(PreconditionFailed)
        PayloadTooLarge.code -> Result.Error(PayloadTooLarge)
        UriTooLong.code -> Result.Error(UriTooLong)
        UnsupportedMediaType.code -> Result.Error(UnsupportedMediaType)
        RangeNotSatisfiable.code -> Result.Error(RangeNotSatisfiable)
        ExpectationFailed.code -> Result.Error(ExpectationFailed)
        ImATeapot.code -> Result.Error(ImATeapot)
        MisdirectedRequest.code -> Result.Error(MisdirectedRequest)
        UnprocessableEntity.code -> Result.Error(UnprocessableEntity)
        Locked.code -> Result.Error(Locked)
        FailedDependency.code -> Result.Error(FailedDependency)
        UpgradeRequired.code -> Result.Error(UpgradeRequired)
        PreconditionRequired.code -> Result.Error(PreconditionRequired)
        TooManyRequests.code -> Result.Error(TooManyRequests)
        RequestHeaderFieldsTooLarge.code -> Result.Error(RequestHeaderFieldsTooLarge)
        UnavailableForLegalReasons.code -> Result.Error(UnavailableForLegalReasons)
        // Server Errors
        InternalServerError.code -> Result.Error(InternalServerError)
        NotImplemented.code -> Result.Error(NotImplemented)
        BadGateway.code -> Result.Error(BadGateway)
        ServiceUnavailable.code -> Result.Error(ServiceUnavailable)
        GatewayTimeout.code -> Result.Error(GatewayTimeout)
        HttpVersionNotSupported.code -> Result.Error(HttpVersionNotSupported)
        VariantAlsoNegates.code -> Result.Error(VariantAlsoNegates)
        InsufficientStorage.code -> Result.Error(InsufficientStorage)
        LoopDetected.code -> Result.Error(LoopDetected)
        NotExtended.code -> Result.Error(NotExtended)
        NetworkAuthenticationRequired.code -> Result.Error(NetworkAuthenticationRequired)

        else -> Result.Error(Unknown)
    }
}