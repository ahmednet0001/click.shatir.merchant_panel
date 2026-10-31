package click.shatir.merchant_panel.data.repo

import android.content.Context
import android.net.Uri
import click.shatir.merchant_panel.data.ApiClient
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.model.error.ApiError
import click.shatir.merchant_panel.model.settings.SettingsRequest
import click.shatir.merchant_panel.model.settings.StoreSettingsData
import click.shatir.merchant_panel.util.LogoEncoder
import com.google.gson.Gson
import okhttp3.MultipartBody
import retrofit2.HttpException

class SettingsRepository(
    private val tokenManager: TokenManager,
    private val context: Context
) {

    suspend fun getSettings(storeId: Int): Result<StoreSettingsData> = call {
        ApiClient.service.getSettings(bearer(), storeId)
    }

    /**
     * يحفظ البيانات (JSON) ثم يرفع الشعار أو يحذفه إن لزم (طلب منفصل).
     * الرد الأخير هو الحالة النهائية على السيرفر.
     */
    suspend fun saveSettings(
        storeId: Int,
        request: SettingsRequest,
        newLogoUri: Uri?,
        removeLogo: Boolean
    ): Result<StoreSettingsData> = call {
        var result = ApiClient.service.updateSettings(bearer(), storeId, request)
        if (newLogoUri != null) {
            val part = LogoEncoder.toPart(context, newLogoUri)
            result = ApiClient.service.updateSettingsLogo(bearer(), storeId, listOf(part))
        } else if (removeLogo) {
            val part = MultipartBody.Part.createFormData("remove_logo", "1")
            result = ApiClient.service.updateSettingsLogo(bearer(), storeId, listOf(part))
        }
        result
    }

    private fun bearer(): String = "Bearer ${tokenManager.getToken() ?: ""}"

    private suspend fun <T> call(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: IllegalArgumentException) {
            Result.failure(Exception(e.message ?: "تعذرت معالجة الصورة."))
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
