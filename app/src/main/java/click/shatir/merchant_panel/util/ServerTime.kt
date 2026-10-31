package click.shatir.merchant_panel.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * التعامل مع أوقات العروض.
 *
 * - السيرفر يرجع التواريخ بصيغة ISO بتوقيت UTC (مثل 2026-10-29T12:00:00.000000Z).
 * - ويحفظ ما نرسله كما هو بتوقيت APP_TIMEZONE، لذلك نرسل الوقت المحلي للسيرفر بدون منطقة زمنية.
 * لا نستخدم java.time لأن minSdk = 24.
 */
object ServerTime {

    private val serverTz: TimeZone get() = TimeZone.getTimeZone(Constants.SERVER_TIME_ZONE)
    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    /** يحول رد السيرفر إلى epoch millis، أو null إن تعذر. */
    fun parse(iso: String?): Long? {
        if (iso.isNullOrBlank()) return null
        val clean = iso.trim().replace(' ', 'T')
        if (clean.length < 19) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = utc }
                .parse(clean.substring(0, 19))?.time
        } catch (_: Exception) {
            null
        }
    }

    /** القيمة المرسلة للسيرفر. */
    fun toRequest(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply { timeZone = serverTz }
            .format(Date(millis))

    /** نص العرض في الواجهة. */
    fun display(millis: Long): String =
        SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.US).apply { timeZone = serverTz }
            .format(Date(millis))

    fun displayIso(iso: String?): String = parse(iso)?.let { display(it) } ?: "-"

    /** الآن مقرّباً لأقرب دقيقة. */
    fun nowMillis(): Long = (System.currentTimeMillis() / 60_000L) * 60_000L

    class Parts(val year: Int, val month: Int, val day: Int, val hour: Int, val minute: Int)

    fun parts(millis: Long): Parts {
        val c = Calendar.getInstance(serverTz).apply { timeInMillis = millis }
        return Parts(
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)
        )
    }

    fun build(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance(serverTz).apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }.timeInMillis

    /** DatePicker يعمل بمنتصف ليل UTC لليوم المحدد. */
    fun toPickerMillis(millis: Long): Long {
        val p = parts(millis)
        return Calendar.getInstance(utc).apply {
            clear()
            set(p.year, p.month, p.day, 0, 0, 0)
        }.timeInMillis
    }

    /** يرجع (سنة، شهر، يوم) من قيمة DatePicker. */
    fun fromPickerMillis(pickerMillis: Long): Triple<Int, Int, Int> {
        val c = Calendar.getInstance(utc).apply { timeInMillis = pickerMillis }
        return Triple(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
    }
}
