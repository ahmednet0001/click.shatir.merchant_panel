package click.shatir.merchant_panel.data.repo

import click.shatir.merchant_panel.data.ApiClient
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.model.dashboard.DashboardResponse
import click.shatir.merchant_panel.model.dashboard.ToggleMenuResponse
import click.shatir.merchant_panel.model.dashboard.ToggleProductImageResponse
import click.shatir.merchant_panel.model.dashboard.ToggleStatusResponse
import click.shatir.merchant_panel.model.error.ApiError
import click.shatir.merchant_panel.model.store.Store
import com.google.gson.Gson
import retrofit2.HttpException

class StoreRepository(private val tokenManager: TokenManager) {

    suspend fun getStores(): Result<List<Store>> {
        val token = tokenManager.getToken()
            ?: return Result.failure(Exception("لا يوجد توكن"))
        return try {
            Result.success(ApiClient.service.getStores("Bearer $token"))
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: Exception) {
            Result.failure(Exception("تعذّر الاتصال بالسيرفر."))
        }
    }

    suspend fun getDashboard(storeId: Int): Result<DashboardResponse> {
        val token = tokenManager.getToken()
            ?: return Result.failure(Exception("لا يوجد توكن"))
        return try {
            Result.success(ApiClient.service.getDashboard("Bearer $token", storeId))
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: Exception) {
            Result.failure(Exception("تعذّر الاتصال بالسيرفر."))
        }
    }

    suspend fun toggleStatus(storeId: Int): Result<ToggleStatusResponse> {
        val token = tokenManager.getToken()
            ?: return Result.failure(Exception("لا يوجد توكن"))
        return try {
            Result.success(ApiClient.service.toggleStatus("Bearer $token", storeId))
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: Exception) {
            Result.failure(Exception("تعذّر الاتصال بالسيرفر."))
        }
    }

    suspend fun toggleMenuMode(storeId: Int): Result<ToggleMenuResponse> {
        val token = tokenManager.getToken()
            ?: return Result.failure(Exception("لا يوجد توكن"))
        return try {
            Result.success(ApiClient.service.toggleMenuMode("Bearer $token", storeId))
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: Exception) {
            Result.failure(Exception("تعذّر الاتصال بالسيرفر."))
        }
    }

    suspend fun toggleProductImage(storeId: Int): Result<ToggleProductImageResponse> {
        val token = tokenManager.getToken()
            ?: return Result.failure(Exception("لا يوجد توكن"))
        return try {
            Result.success(ApiClient.service.toggleProductImage("Bearer $token", storeId))
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