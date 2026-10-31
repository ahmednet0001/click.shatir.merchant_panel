package click.shatir.merchant_panel.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import click.shatir.merchant_panel.data.ApiClient
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.model.category.MessageResponse
import click.shatir.merchant_panel.model.error.ApiError
import click.shatir.merchant_panel.model.product.Product
import click.shatir.merchant_panel.model.product.ProductRequest
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream

class ProductRepository(
    private val tokenManager: TokenManager,
    private val context: Context
) {

    suspend fun getProducts(storeId: Int): Result<List<Product>> = call {
        ApiClient.service.getProducts(bearer(), storeId)
    }

    suspend fun createProduct(
        storeId: Int,
        request: ProductRequest,
        imageUri: Uri?
    ): Result<Product> = call {
        if (imageUri == null) {
            ApiClient.service.createProduct(bearer(), storeId, request)
        } else {
            ApiClient.service.createProductWithImage(
                bearer(),
                storeId,
                productParts(request, imageUri)
            )
        }
    }

    suspend fun updateProduct(
        storeId: Int,
        productId: Int,
        request: ProductRequest,
        imageUri: Uri?,
        removeImage: Boolean
    ): Result<Product> = call {
        val updated = ApiClient.service.updateProduct(bearer(), storeId, productId, request)
        if (imageUri == null && !removeImage) {
            updated
        } else {
            val imageParts = mutableListOf(
                MultipartBody.Part.createFormData("_method", "PUT")
            )
            if (imageUri != null) {
                imageParts += imagePart(imageUri)
            } else {
                imageParts += MultipartBody.Part.createFormData("remove_image", "1")
            }
            ApiClient.service.updateProductImage(
                bearer(),
                storeId,
                productId,
                imageParts
            )
        }
    }

    suspend fun deleteProduct(storeId: Int, productId: Int): Result<MessageResponse> = call {
        ApiClient.service.deleteProduct(bearer(), storeId, productId)
    }

    private fun bearer(): String = "Bearer ${tokenManager.getToken() ?: ""}"

    private fun productParts(
        request: ProductRequest,
        imageUri: Uri
    ): List<MultipartBody.Part> = buildList {
        add(MultipartBody.Part.createFormData("category_id", request.categoryId.toString()))
        add(MultipartBody.Part.createFormData("name", request.name))
        add(MultipartBody.Part.createFormData("price", request.price.toString()))
        add(MultipartBody.Part.createFormData("description", request.description))
        request.variants.orEmpty().forEachIndexed { index, variant ->
            add(
                MultipartBody.Part.createFormData(
                    "variants[$index][size]",
                    variant.size
                )
            )
            add(
                MultipartBody.Part.createFormData(
                    "variants[$index][price]",
                    variant.price.toString()
                )
            )
        }
        add(imagePart(imageUri))
    }

    private fun imagePart(uri: Uri): MultipartBody.Part {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = resolver.openInputStream(uri)
            ?: throw IllegalArgumentException("تعذرت قراءة الصورة.")
        boundsStream.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IllegalArgumentException("ملف الصورة غير صالح.")
        }

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > 1600 ||
            bounds.outHeight / sampleSize > 1600
        ) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        var bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IllegalArgumentException("تعذرت قراءة الصورة.")

        try {
            val output = ByteArrayOutputStream()
            var quality = 85
            while (true) {
                output.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                if (output.size() <= MAX_IMAGE_BYTES) break

                if (quality > 45) {
                    quality -= 10
                } else {
                    if (bitmap.width <= 480 || bitmap.height <= 480) {
                        throw IllegalArgumentException(
                            "تعذر ضغط الصورة إلى أقل من 512 كيلوبايت."
                        )
                    }
                    val smaller = Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * 0.75f).toInt().coerceAtLeast(1),
                        (bitmap.height * 0.75f).toInt().coerceAtLeast(1),
                        true
                    )
                    if (smaller !== bitmap) bitmap.recycle()
                    bitmap = smaller
                    quality = 85
                }
            }

            val body = output.toByteArray().toRequestBody("image/jpeg".toMediaType())
            return MultipartBody.Part.createFormData("image", "product.jpg", body)
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

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

    private companion object {
        const val MAX_IMAGE_BYTES = 512 * 1024
    }
}