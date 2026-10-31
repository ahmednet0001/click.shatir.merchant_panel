package click.shatir.merchant_panel.ui.screens.store


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import click.shatir.merchant_panel.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import click.shatir.merchant_panel.model.store.Store
import click.shatir.merchant_panel.ui.components.LogoutTextButton
import click.shatir.merchant_panel.ui.theme.BrandNavy
import click.shatir.merchant_panel.ui.theme.BrandOrangeDark
import click.shatir.merchant_panel.ui.theme.BrandOrangeLight


@Composable
fun StoreSelectionScreen(
    onStoreSelected: () -> Unit,
    viewModel: StoreSelectionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(BrandNavy, Color(0xFF002B5C))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                LogoutTextButton()
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "اختر المتجر",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "حسابك مرتبط بأكثر من متجر، اختر واحداً للمتابعة",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            when {
                state.isLoading -> {
                    CircularProgressIndicator(color = BrandOrangeLight)
                }

                state.error != null -> {
                    Text(
                        text = state.error!!,
                        color = BrandOrangeLight,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadStores() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandOrangeDark,
                            contentColor = Color.White
                        )
                    ) { Text("إعادة المحاولة") }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.stores) { store ->
                            StoreCard(
                                store = store,
                                onClick = {
                                    viewModel.selectStore(store)
                                    onStoreSelected()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreCard(store: Store, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // أيقونة دائرية بأول حرف من اسم المتجر
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BrandOrangeLight.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = store.name.trim().take(1),
                style = MaterialTheme.typography.titleLarge,
                color = BrandOrangeLight
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = store.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f)
        )
    }
}
