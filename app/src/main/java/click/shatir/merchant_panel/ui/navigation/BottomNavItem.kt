package click.shatir.merchant_panel.ui.navigation

import androidx.annotation.DrawableRes
import click.shatir.merchant_panel.R

data class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
)

/** كل عناصر الشريط السفلي (يتم تمريره أفقياً). الإضافات تُخفى تلقائياً في الباقة البسيطة. */
val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD, "الرئيسية", R.drawable.store_24px),
    BottomNavItem(Routes.PRODUCTS, "المنتجات", R.drawable.shopping_bag_24px),
    BottomNavItem(Routes.CATEGORIES, "التصنيفات", R.drawable.category_24px),
    BottomNavItem(Routes.OFFERS, "العروض", R.drawable.price_check_24px),
    BottomNavItem(Routes.ADDITIONS, "الإضافات", R.drawable.add_circle_24px),
    BottomNavItem(Routes.SHIPPING, "التوصيل", R.drawable.local_shipping_24px),
    BottomNavItem(Routes.STORE_SETTINGS, "الإعدادات", R.drawable.settings_alert_24px)
)
