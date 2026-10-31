package click.shatir.merchant_panel.data.repo

import click.shatir.merchant_panel.data.ApiClient
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.model.category.MessageResponse
import click.shatir.merchant_panel.model.error.ApiError
import click.shatir.merchant_panel.model.offer.Offer
import click.shatir.merchant_panel.model.offer.OfferRequest
import com.google.gson.Gson
import retrofit2.HttpException

class OfferRepository(private val tokenManager: TokenManager) {

    suspend fun getOffers(storeId: Int): Result<List<Offer>> = call {
        ApiClient.service.getOffers(bearer(), storeId)
    }

    suspend fun createOffer(storeId: Int, request: OfferRequest): Result<Offer> = call {
        ApiClient.service.createOffer(bearer(), storeId, request)
    }

    suspend fun updateOffer(storeId: Int, id: Int, request: OfferRequest): Result<Offer> = call {
        ApiClient.service.updateOffer(bearer(), storeId, id, request)
    }

    suspend fun deleteOffer(storeId: Int, id: Int): Result<MessageResponse> = call {
        ApiClient.service.deleteOffer(bearer(), storeId, id)
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
