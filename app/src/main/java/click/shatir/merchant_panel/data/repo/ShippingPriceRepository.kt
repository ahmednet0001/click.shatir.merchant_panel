package click.shatir.merchant_panel.data.repo


import click.shatir.merchant_panel.data.ApiClient
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.model.category.MessageResponse
import click.shatir.merchant_panel.model.error.ApiError
import click.shatir.merchant_panel.model.shippingprice.ShippingPrice
import click.shatir.merchant_panel.model.shippingprice.ShippingPriceRequest
import com.google.gson.Gson
import retrofit2.HttpException

class ShippingPriceRepository(private val tokenManager: TokenManager) {

    suspend fun getShippingPrices(storeId: Int): Result<List<ShippingPrice>> = call {
        ApiClient.service.getShippingPrices(bearer(), storeId)
    }

    suspend fun createShippingPrice(storeId: Int, city: String, price: Double): Result<ShippingPrice> = call {
        ApiClient.service.createShippingPrice(bearer(), storeId, ShippingPriceRequest(city, price))
    }

    suspend fun updateShippingPrice(storeId: Int, id: Int, city: String, price: Double): Result<ShippingPrice> = call {
        ApiClient.service.updateShippingPrice(bearer(), storeId, id, ShippingPriceRequest(city, price))
    }

    suspend fun deleteShippingPrice(storeId: Int, id: Int): Result<MessageResponse> = call {
        ApiClient.service.deleteShippingPrice(bearer(), storeId, id)
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