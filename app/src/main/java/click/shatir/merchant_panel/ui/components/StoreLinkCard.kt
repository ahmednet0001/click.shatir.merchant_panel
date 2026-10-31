package click.shatir.merchant_panel.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import click.shatir.merchant_panel.R
import click.shatir.merchant_panel.ui.theme.BrandNavy
import click.shatir.merchant_panel.ui.theme.BrandOrangeDark
import java.io.File
import java.io.FileOutputStream

@Composable
fun StoreLinkCard(
    storeName: String,
    storeUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    SectionCard(
        title = "رابط المتجر",
        subtitle = "شاركه مع عملائك أو اطبع رمز QR",
        icon = R.drawable.ic_share,
        modifier = modifier
    ) {
        // رمز QR على خلفية بيضاء بإطار، حتى يبقى واضحاً للمسح
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, AppColors.Border)
            ) {
                QrCodeImage(
                    content = storeUrl,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(180.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // الرابط + زر نسخ
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AppColors.ScreenBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = storeUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp)
                )
                IconButton(onClick = { copyLink(context, storeName, storeUrl) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = "نسخ الرابط",
                        tint = BrandNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PrimaryButton(
                text = "مشاركة",
                icon = R.drawable.ic_share,
                onClick = { shareLink(context, storeName, storeUrl) },
                modifier = Modifier.weight(1f)
            )
            SecondaryButton(
                text = "تحميل QR",
                icon = R.drawable.ic_download,
                onClick = { downloadQrToGallery(context, storeName, storeUrl) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun copyLink(context: Context, storeName: String, storeUrl: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(storeName, storeUrl))
    android.widget.Toast.makeText(context, "تم نسخ الرابط", android.widget.Toast.LENGTH_SHORT).show()
}

private fun shareLink(context: Context, storeName: String, storeUrl: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, storeName)
        putExtra(Intent.EXTRA_TEXT, storeUrl)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة رابط المتجر"))
}

// تحميل صورة QR مباشرة إلى معرض الصور (Pictures/ClickShatir)
private fun downloadQrToGallery(context: Context, storeName: String, storeUrl: String) {
    try {
        val sizePx = 1024
        val matrix = com.google.zxing.qrcode.QRCodeWriter()
            .encode(storeUrl, com.google.zxing.BarcodeFormat.QR_CODE, sizePx, sizePx)

        val bmp = android.graphics.Bitmap.createBitmap(
            sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888
        )
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bmp.setPixel(
                    x, y,
                    if (matrix.get(x, y)) android.graphics.Color.BLACK
                    else android.graphics.Color.WHITE
                )
            }
        }

        val safeName = storeName.replace(Regex("[^\\p{L}\\p{N}_-]"), "_")
        val fileName = "qr_${safeName}_${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // أندرويد 10+ : MediaStore بدون صلاحيات
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/ClickShatir")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("تعذّر إنشاء الملف")

            resolver.openOutputStream(uri)?.use { out ->
                bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            } ?: throw IllegalStateException("تعذّر الكتابة")

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

        } else {
            // أندرويد 9 وأقدم : حفظ مباشر في Pictures (يحتاج WRITE_EXTERNAL_STORAGE)
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "ClickShatir"
            ).apply { mkdirs() }
            val file = File(dir, fileName)
            FileOutputStream(file).use {
                bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            // إبلاغ المعرض بالصورة الجديدة
            android.media.MediaScannerConnection.scanFile(
                context, arrayOf(file.absolutePath), arrayOf("image/png"), null
            )
        }

        android.widget.Toast.makeText(
            context,
            "تم حفظ الصورة في المعرض",
            android.widget.Toast.LENGTH_SHORT
        ).show()

    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "فشل حفظ الصورة: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}