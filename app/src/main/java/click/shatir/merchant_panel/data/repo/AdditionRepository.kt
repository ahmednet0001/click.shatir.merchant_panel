package click.shatir.merchant_panel.data.repo


import click.shatir.merchant_panel.data.ApiClient
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.model.addition.Addition
import click.shatir.merchant_panel.model.addition.AdditionRequest
import click.shatir.merchant_panel.model.category.MessageResponse
import click.shatir.merchant_panel.model.error.ApiError
import com.google.gson.Gson
import retrofit2.HttpException

class AdditionRepository(private val tokenManager: TokenManager) {

    suspend fun getAdditions(storeId: Int): Result<List<Addition>> = call {
        ApiClient.service.getAdditions(bearer(), storeId)
    }

    suspend fun createAddition(storeId: Int, name: String, price: Double): Result<Addition> = call {
        ApiClient.service.createAddition(bearer(), storeId, AdditionRequest(name, price))
    }

    suspend fun updateAddition(storeId: Int, additionId: Int, name: String, price: Double): Result<Addition> = call {
        ApiClient.service.updateAddition(bearer(), storeId, additionId, AdditionRequest(name, price))
    }

    suspend fun deleteAddition(storeId: Int, additionId: Int): Result<MessageResponse> = call {
        ApiClient.service.deleteAddition(bearer(), storeId, additionId)
    }

    private fun bearer(): String = "Bearer ${tokenManager.getToken() ?: ""}"

    private suspend fun <T> call(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: Exception) {
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