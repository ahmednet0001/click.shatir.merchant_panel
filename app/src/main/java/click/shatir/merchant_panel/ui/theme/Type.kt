package click.shatir.merchant_panel.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import click.shatir.merchant_panel.R

val CairoFontFamily = FontFamily(
    Font(R.font.cairo_regular, FontWeight.Normal),
    Font(R.font.cairo_medium, FontWeight.Medium),
    Font(R.font.cairo_semibold, FontWeight.SemiBold),
    Font(R.font.cairo_bold, FontWeight.Bold)
)

private fun cairo(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    letterSpacing: Double = 0.0
) = TextStyle(
    fontFamily = CairoFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

/**
 * كل أنماط Material3 تستخدم خط Cairo.
 * سابقاً كانت 6 أنماط فقط معرّفة، وباقي الأنماط (bodySmall, labelMedium, titleMedium...)
 * كانت تسقط للخط الافتراضي للنظام فيظهر النص العربي بخطين مختلفين.
 */
val AppTypography = Typography(
    displayLarge = cairo(FontWeight.Bold, 48, 60),
    displayMedium = cairo(FontWeight.Bold, 40, 52),
    displaySmall = cairo(FontWeight.Bold, 34, 44),
    headlineLarge = cairo(FontWeight.Bold, 32, 40),
    headlineMedium = cairo(FontWeight.Bold, 26, 34),
    headlineSmall = cairo(FontWeight.Bold, 22, 30),
    titleLarge = cairo(FontWeight.SemiBold, 20, 28),
    titleMedium = cairo(FontWeight.SemiBold, 16, 24),
    titleSmall = cairo(FontWeight.SemiBold, 14, 22),
    bodyLarge = cairo(FontWeight.Normal, 16, 24),
    bodyMedium = cairo(FontWeight.Normal, 14, 22),
    bodySmall = cairo(FontWeight.Normal, 12, 18),
    labelLarge = cairo(FontWeight.SemiBold, 16, 22),
    labelMedium = cairo(FontWeight.Medium, 12, 18),
    labelSmall = cairo(FontWeight.Medium, 11, 16)
)
