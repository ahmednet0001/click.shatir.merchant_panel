package click.shatir.merchant_panel.data.repo

import click.shatir.merchant_panel.model.error.ApiError

import click.shatir.merchant_panel.data.ApiClient
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.model.login.LoginRequest
import click.shatir.merchant_panel.model.login.LoginResponse

import com.google.gson.Gson
import retrofit2.HttpException

class AuthRepository(private val tokenManager: TokenManager) {

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = ApiClient.service.login(LoginRequest(email, password))
            tokenManager.saveToken(response.token)
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: Exception) {
            Result.failure(Exception("تعذّر الاتصال بالسيرفر. تحقق من الإنترنت."))
        }
    }

    suspend fun logout(): Result<Unit> {
        val token = tokenManager.getToken()
            ?: return Result.success(Unit) // لا توكن، امسح محلياً فقط

        return try {
            ApiClient.service.logout("Bearer $token")
            tokenManager.clearToken()
            Result.success(Unit)
        } catch (e: HttpException) {
            // حتى لو فشل الخادم، امسح التوكن محلياً
            tokenManager.clearToken()
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: Exception) {
            tokenManager.clearToken()
            Result.failure(Exception("تعذّر الاتصال بالسيرفر."))
        }
    }

    private fun parseHttpError(e: HttpException): String {
        return try {
            val body = e.response()?.errorBody()?.string()
            Gson().fromJson(body, ApiError::class.java).firstError()
        } catch (_: Exception) {
            "حدث خطأ (${e.code()})"
        }
    }
}
