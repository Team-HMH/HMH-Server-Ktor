package sopt.org.hmh.global.common.response

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T): BaseResponse<T> {
            return BaseResponse(
                success = true,
                code = "SUCCESS",
                message = "요청이 성공적으로 처리되었습니다.",
                data = data
            )
        }
        
        fun <T> success(data: T, message: String): BaseResponse<T> {
            return BaseResponse(
                success = true,
                code = "SUCCESS",
                message = message,
                data = data
            )
        }
        
        fun <T> error(code: String, message: String): BaseResponse<T> {
            return BaseResponse(
                success = false,
                code = code,
                message = message,
                data = null
            )
        }
    }
}