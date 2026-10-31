package click.shatir.merchant_panel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    secondary = BrandOrangeLight,
    onSecondary = Color.White,
    tertiary = BrandOrangeDark,
    onTertiary = Color.White,
    background = BrandSurface,
    onBackground = Color(0xFF1B1B1B),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1B),
    error = BrandErrorRed,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = BrandOrangeLight,
    onPrimary = Color(0xFF1B1B1B),
    secondary = BrandNavyLight,
    onSecondary = Color.White,
    tertiary = BrandOrangeDark,
    onTertiary = Color.White,
    background = Color(0xFF101418),
    onBackground = Color(0xFFEAEAEA),
    surface = Color(0xFF1A1F24),
    onSurface = Color(0xFFEAEAEA),
    error = BrandErrorRed,
    onError = Color.White
)

@Composable
fun ControlPanelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}