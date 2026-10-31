package click.shatir.merchant_panel.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

/**
 * يجهز صورة الشعار للرفع (السيرفر يقبل jpeg/png/webp بحد أقصى 1024KB).
 * إذا كانت الصورة بها شفافية نحتفظ بها كـ PNG، وإلا نضغطها JPEG.
 */
object LogoEncoder {

    private const val MAX_BYTES = 1000 * 1024
    private const val MAX_SIDE = 1024

    fun toPart(context: Context, uri: Uri): MultipartBody.Part {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        (resolver.openInputStream(uri) ?: throw IllegalArgumentException("تعذرت قراءة الصورة."))
            .use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IllegalArgumentException("ملف الصورة غير صالح.")
        }

        var sample = 1
        while (bounds.outWidth / sample > MAX_SIDE || bounds.outHeight / sample > MAX_SIDE) sample *= 2

        var bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: throw IllegalArgumentException("تعذرت قراءة الصورة.")

        val hasAlpha = bitmap.hasAlpha()
        val out = ByteArrayOutputStream()

        try {
            if (hasAlpha) {
                // PNG مع تصغير تدريجي حتى يدخل في الحد المسموح
                while (true) {
                    out.reset()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    if (out.size() <= MAX_BYTES) break
                    if (bitmap.width <= 256 || bitmap.height <= 256) {
                        throw IllegalArgumentException("تعذر ضغط الشعار إلى أقل من 1 ميجابايت.")
                    }
                    bitmap = shrink(bitmap)
                }
                return part(out.toByteArray(), "image/png", "logo.png")
            }

            var quality = 90
            while (true) {
                out.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                if (out.size() <= MAX_BYTES) break
                if (quality > 50) {
                    quality -= 10
                } else {
                    if (bitmap.width <= 256 || bitmap.height <= 256) {
                        throw IllegalArgumentException("تعذر ضغط الشعار إلى أقل من 1 ميجابايت.")
                    }
                    bitmap = shrink(bitmap)
                    quality = 90
                }
            }
            return part(out.toByteArray(), "image/jpeg", "logo.jpg")
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    private fun shrink(src: Bitmap): Bitmap {
        val smaller = Bitmap.createScaledBitmap(
            src,
            (src.width * 0.75f).toInt().coerceAtLeast(1),
            (src.height * 0.75f).toInt().coerceAtLeast(1),
            true
        )
        if (smaller !== src) src.recycle()
        return smaller
    }

    private fun part(bytes: ByteArray, mime: String, name: String): MultipartBody.Part =
        MultipartBody.Part.createFormData("logo", name, bytes.toRequestBody(mime.toMediaType()))
}
