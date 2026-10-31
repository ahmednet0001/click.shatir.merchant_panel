package click.shatir.merchant_panel.ui.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import click.shatir.merchant_panel.ui.theme.BrandNavy
import click.shatir.merchant_panel.ui.theme.BrandOrangeLight
import click.shatir.merchant_panel.R

@Composable
fun SplashScreen(
    onGoToLogin: () -> Unit,
    onGoToDashboard: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            is SplashState.GoToLogin -> onGoToLogin()
            is SplashState.GoToDashboard -> onGoToDashboard()
            else -> Unit
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(BrandNavy, Color(0xFF002B5C))
    )

    // ظهور تدريجي ناعم بدل ظهور مفاجئ
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "splashAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .alpha(contentAlpha),
        contentAlignment = Alignment.Center
    ) {
        when (val s = state) {

            SplashState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.light_logo),
                        contentDescription = "شعار التطبيق",
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .heightIn(max = 200.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(40.dp))
                    CircularProgressIndicator(
                        color = BrandOrangeLight,
                        strokeWidth = 3.dp
                    )
                }
            }

            is SplashState.Error -> {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "شعار التطبيق",
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .heightIn(max = 160.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = s.message,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.retry() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandOrangeLight,
                            contentColor = Color.White
                        )
                    ) {
                        Text("إعادة المحاولة")
                    }
                }
            }

            else -> Unit
        }
    }
}