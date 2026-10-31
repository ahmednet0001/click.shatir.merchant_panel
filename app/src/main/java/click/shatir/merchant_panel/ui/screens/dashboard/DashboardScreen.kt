package click.shatir.merchant_panel.ui.screens.dashboard

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import click.shatir.merchant_panel.R
import click.shatir.merchant_panel.model.dashboard.DashboardResponse
import click.shatir.merchant_panel.ui.components.AppCard
import click.shatir.merchant_panel.ui.components.AppColors
import click.shatir.merchant_panel.ui.components.EmptyState
import click.shatir.merchant_panel.ui.components.ErrorState
import click.shatir.merchant_panel.ui.components.IconBadge
import click.shatir.merchant_panel.ui.components.ScreenScaffold
import click.shatir.merchant_panel.ui.components.SectionCard
import click.shatir.merchant_panel.ui.components.StatusChip
import click.shatir.merchant_panel.ui.components.StoreLinkCard
import click.shatir.merchant_panel.ui.components.SwitchRow
import click.shatir.merchant_panel.ui.components.shimmerBrush
import click.shatir.merchant_panel.ui.theme.BrandNavy
import click.shatir.merchant_panel.ui.theme.BrandOrangeDark
import click.shatir.merchant_panel.ui.theme.BrandOrangeLight

@Composable
fun DashboardScreen(
    onNeedStoreSelection: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenOffers: () -> Unit = {},
    onOpenShipping: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val toggling by viewModel.toggling.collectAsState()
    val snackbarMsg by viewModel.snackbar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state) {
        if (state is DashboardState.GoToStoreSelection) onNeedStoreSelection()
    }

    // كل مرة تعود فيها الشاشة للواجهة (رجوع من صفحة أخرى، تبديل تبويب، أو العودة للتطبيق) حدّث البيانات
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    ScreenScaffold(
        title = (state as? DashboardState.Ready)?.data?.store?.name ?: "لوحة التحكم",
        snackbarHostState = snackbarHostState
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                DashboardState.Loading -> DashboardSkeleton()

                is DashboardState.Error -> ErrorState(
                    message = s.message,
                    onRetry = { viewModel.load() }
                )

                DashboardState.NoStores -> EmptyState(
                    icon = R.drawable.store_24px,
                    title = "لا توجد متاجر مرتبطة بحسابك",
                    message = "تواصل مع الدعم لربط متجر بحسابك."
                )

                is DashboardState.Ready -> {
                    DashboardContent(
                        data = s.data,
                        toggling = toggling,
                        onToggleOpen = { viewModel.toggleOpen() },
                        onToggleMenu = { viewModel.toggleMenu() },
                        onToggleImage = { viewModel.toggleProductImage() },
                        onOpenProducts = onOpenProducts,
                        onOpenCategories = onOpenCategories,
                        onOpenOffers = onOpenOffers,
                        onOpenShipping = onOpenShipping
                    )
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: DashboardResponse,
    toggling: Set<String>,
    onToggleOpen: () -> Unit,
    onToggleMenu: () -> Unit,
    onToggleImage: () -> Unit,
    onOpenProducts: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenOffers: () -> Unit,
    onOpenShipping: () -> Unit
) {
    val isSimple = data.packageInfo.isSimple

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StoreHero(
            storeName = data.store.name,
            packageName = data.packageInfo.name,
            isOpen = data.settings.isOpen,
            loading = toggling.contains("open"),
            onToggleOpen = onToggleOpen
        )

        // بطاقات الإحصاء: قابلة للضغط للانتقال للقسم
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = R.drawable.shopping_bag_24px,
                value = data.usage.products.used.toString(),
                label = "المنتجات",
                tint = BrandNavy,
                onClick = onOpenProducts,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = R.drawable.category_24px,
                value = data.usage.categories.used.toString(),
                label = "التصنيفات",
                tint = BrandNavy,
                onClick = onOpenCategories,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                icon = R.drawable.price_check_24px,
                value = data.counts.offers.toString(),
                label = "العروض",
                tint = BrandOrangeDark,
                onClick = onOpenOffers,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = R.drawable.local_shipping_24px,
                value = data.counts.shippingPrices.toString(),
                label = "أسعار الشحن",
                tint = BrandOrangeDark,
                onClick = onOpenShipping,
                modifier = Modifier.weight(1f)
            )
        }

        SectionCard(
            title = "إعدادات العرض",
            icon = R.drawable.settings_alert_24px
        ) {
            SwitchRow(
                label = "وضع المنيو فقط",
                checked = data.settings.isMenu,
                loading = toggling.contains("menu"),
                onToggle = onToggleMenu
            )
            if (!isSimple) {
                HorizontalDivider(color = AppColors.Border)
                SwitchRow(
                    label = "عرض صور المنتجات",
                    checked = data.settings.showProductImage,
                    loading = toggling.contains("image"),
                    onToggle = onToggleImage
                )
            }
        }

        SectionCard(
            title = "استخدام الباقة",
            subtitle = "${data.packageInfo.name} · ${data.packageInfo.type}",
            icon = R.drawable.price_check_24px
        ) {
            UsageBar(
                label = "الأقسام",
                used = data.usage.categories.used,
                max = data.usage.categories.max,
                unlimited = data.usage.categories.unlimited
            )
            Spacer(Modifier.height(14.dp))
            UsageBar(
                label = "المنتجات",
                used = data.usage.products.used,
                max = data.usage.products.max,
                unlimited = data.usage.products.unlimited
            )
            if (!isSimple && data.usage.additions.supported) {
                Spacer(Modifier.height(14.dp))
                UsageBar(
                    label = "الإضافات",
                    used = data.usage.additions.used,
                    max = data.usage.additions.max,
                    unlimited = data.usage.additions.unlimited
                )
            }
        }

        StoreLinkCard(
            storeName = data.store.name,
            storeUrl = data.store.url
        )

        Spacer(Modifier.height(8.dp))
    }
}

// ========== بطاقة المتجر العلوية ==========

@Composable
private fun StoreHero(
    storeName: String,
    packageName: String,
    isOpen: Boolean,
    loading: Boolean,
    onToggleOpen: () -> Unit
) {
    val brush = Brush.verticalGradient(listOf(BrandNavy, Color(0xFF002B5C)))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(brush)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "متجرك",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    storeName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(label = packageName, color = BrandOrangeLight)
        }

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            SwitchRow(
                label = if (isOpen) "المتجر مفتوح للطلبات" else "المتجر مغلق حالياً",
                description = "اضغط لتغيير حالة المتجر",
                checked = isOpen,
                loading = loading,
                onToggle = onToggleOpen,
                labelColor = Color.White,
                descriptionColor = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun StatTile(
    @DrawableRes icon: Int,
    value: String,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, tint = tint, size = 40.dp, iconSize = 20.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = tint,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

// ========== أعمدة الاستخدام ==========

@Composable
private fun UsageBar(
    label: String,
    used: Int,
    max: Int?,
    unlimited: Boolean
) {
    val ratio: Float = when {
        unlimited -> 0f
        max == null || max == 0 -> 0f
        else -> (used.toFloat() / max).coerceIn(0f, 1f)
    }
    val animatedRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(durationMillis = 700),
        label = "usageRatio"
    )

    // لون العمود: أخضر < 70%، برتقالي 70-90%، أحمر > 90%
    val barColor = when {
        unlimited -> BrandOrangeLight
        ratio >= 0.9f -> Color(0xFFD32F2F)
        ratio >= 0.7f -> BrandOrangeDark
        else -> Color(0xFF2E7D32)
    }

    val trailing = if (unlimited) "$used / غير محدود" else "$used / ${max ?: "-"}"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = BrandNavy
            )
            Text(
                text = trailing,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AppColors.Track)
        ) {
            if (unlimited) {
                // شريط كامل بلون برتقالي فاتح للدلالة على "غير محدود"
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrandOrangeLight.copy(alpha = 0.5f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedRatio)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(barColor)
                )
            }
        }
        if (!unlimited && ratio >= 0.9f) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (ratio >= 1f) "وصلت للحد الأقصى في باقتك" else "اقتربت من الحد الأقصى في باقتك",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD32F2F)
            )
        }
    }
}

// ========== هيكل التحميل ==========

@Composable
private fun DashboardSkeleton() {
    val brush = shimmerBrush()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(brush)
        )
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(brush)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(brush)
        )
    }
}
