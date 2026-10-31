package click.shatir.merchant_panel.data.repo



import click.shatir.merchant_panel.data.ApiClient
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.model.category.Category
import click.shatir.merchant_panel.model.category.CategoryRequest
import click.shatir.merchant_panel.model.category.MessageResponse
import click.shatir.merchant_panel.model.error.ApiError
import com.google.gson.Gson
import retrofit2.HttpException

class CategoryRepository(private val tokenManager: TokenManager) {

    suspend fun getCategories(storeId: Int): Result<List<Category>> = call {
        ApiClient.service.getCategories(bearer(), storeId)
    }

    suspend fun createCategory(storeId: Int, name: String): Result<Category> = call {
        ApiClient.service.createCategory(bearer(), storeId, CategoryRequest(name))
    }

    suspend fun updateCategory(storeId: Int, categoryId: Int, name: String): Result<Category> = call {
        ApiClient.service.updateCategory(bearer(), storeId, categoryId, CategoryRequest(name))
    }

    suspend fun deleteCategory(storeId: Int, categoryId: Int): Result<MessageResponse> = call {
        ApiClient.service.deleteCategory(bearer(), storeId, categoryId)
    }

    private fun bearer(): String =
        "Bearer ${tokenManager.getToken() ?: ""}"

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
