package click.shatir.merchant_panel.ui.navigation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.data.repo.AuthRepository
import click.shatir.merchant_panel.ui.components.LocalSessionActions
import click.shatir.merchant_panel.ui.components.SessionActions
import click.shatir.merchant_panel.ui.screens.additions.AdditionsScreen
import click.shatir.merchant_panel.ui.screens.categories.CategoriesScreen
import click.shatir.merchant_panel.ui.screens.dashboard.DashboardScreen
import click.shatir.merchant_panel.ui.screens.login.LoginScreen
import click.shatir.merchant_panel.ui.screens.offers.OffersScreen
import click.shatir.merchant_panel.ui.screens.settings.SettingsScreen
import click.shatir.merchant_panel.ui.screens.products.ProductsScreen
import click.shatir.merchant_panel.ui.screens.shippingprice.ShippingScreen
import click.shatir.merchant_panel.ui.screens.splash.SplashScreen
import click.shatir.merchant_panel.ui.screens.store.StoreSelectionScreen
import click.shatir.merchant_panel.ui.theme.BrandNavy
import click.shatir.merchant_panel.ui.theme.BrandOrangeDark

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val STORE_SELECTION = "store_selection"
    const val DASHBOARD = "dashboard"
    const val CATEGORIES = "categories"
    const val PRODUCTS = "products"
    const val OFFERS = "offers"
    const val ADDITIONS = "additions"
    const val SHIPPING = "shipping"
    const val STORE_SETTINGS = "store_settings"

}

private val bottomRoutes = setOf(
    Routes.DASHBOARD,
    Routes.STORE_SETTINGS,
    Routes.PRODUCTS,
    Routes.OFFERS,
    Routes.CATEGORIES,
    Routes.ADDITIONS,
    Routes.SHIPPING
)

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomRoutes

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()

    // الإضافات لا تظهر في الباقة البسيطة (تتغير تلقائياً عند تحميل بيانات المتجر)
    val packageType by remember { tokenManager.packageTypeFlow() }
        .collectAsState(initial = tokenManager.getPackageType())
    val isSimplePackage = packageType?.equals("simple", ignoreCase = true) == true
    val visibleItems = bottomNavItems.filter { it.route != Routes.ADDITIONS || !isSimplePackage }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Routes.DASHBOARD) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    var loggingOut by remember { mutableStateOf(false) }

    val sessionActions = SessionActions(
        onSwitchStore = {
            // مسح المتجر المحدد ثم الذهاب لاختيار المتجر مع تفريغ كل الصفحات السابقة
            tokenManager.clearStoreSelection()
            // إزالة حالات التبويبات المحفوظة حتى لا تظهر بيانات المتجر السابق
            bottomRoutes.forEach { navController.clearBackStack(it) }
            navController.navigate(Routes.STORE_SELECTION) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        },
        onLogout = {
            if (!loggingOut) {
                loggingOut = true
                scope.launch {
                    // يمسح التوكن محلياً حتى لو فشل الاتصال بالسيرفر
                    AuthRepository(tokenManager).logout()
                    loggingOut = false
                    bottomRoutes.forEach { navController.clearBackStack(it) }
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        },
        isLoggingOut = loggingOut
    )

    CompositionLocalProvider(LocalSessionActions provides sessionActions) {
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    items = visibleItems,
                    currentRoute = currentRoute,
                    onNavigate = navigateToTab
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onGoToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onGoToDashboard = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNeedStoreSelection = {
                        navController.navigate(Routes.STORE_SELECTION) {
                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                        }
                    },
                    onOpenProducts = { navigateToTab(Routes.PRODUCTS) },
                    onOpenCategories = { navigateToTab(Routes.CATEGORIES) },
                    onOpenOffers = { navigateToTab(Routes.OFFERS) },
                    onOpenShipping = { navigateToTab(Routes.SHIPPING) }
                )
            }

            composable(Routes.STORE_SELECTION) {
                StoreSelectionScreen(
                    onStoreSelected = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.STORE_SELECTION) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.CATEGORIES) {
                CategoriesScreen()
            }

            composable(Routes.PRODUCTS) {
                ProductsScreen()
            }

            composable(Routes.OFFERS) {
                OffersScreen()
            }

            composable(Routes.ADDITIONS) {
                AdditionsScreen()
            }

            composable(Routes.SHIPPING) {
                ShippingScreen()
            }

            composable(Routes.STORE_SETTINGS) {
                SettingsScreen()
            }
        }
    }
    }
}

/**
 * الشريط السفلي بنفس الثيم السابق، لكنه قابل للتمرير أفقياً.
 * إذا كانت العناصر 5 أو أقل تتوزع على العرض كاملاً، وإذا زادت يظهر 4.5 عنصر تقريباً
 * (النصف يوضح أن هناك المزيد). ويُمرَّر تلقائياً للعنصر المحدد.
 */
@Composable
private fun BottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    Surface(color = BrandNavy, tonalElevation = 4.dp) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(NavigationBarDefaults.windowInsets)
        ) {
            val itemWidth = if (items.size <= 5) maxWidth / items.size else maxWidth / 4.5f
            val itemWidthPx = with(density) { itemWidth.toPx() }
            val viewportPx = with(density) { maxWidth.toPx() }

            LaunchedEffect(currentRoute, items.size, itemWidthPx) {
                val index = items.indexOfFirst { it.route == currentRoute }
                if (index >= 0) {
                    val target = (index * itemWidthPx - (viewportPx - itemWidthPx) / 2f)
                        .toInt()
                        .coerceAtLeast(0)
                    scrollState.animateScrollTo(target)
                }
            }

            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .height(80.dp)
                    .selectableGroup()
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    Row(modifier = Modifier.width(itemWidth)) {
                        NavigationBarItem(
                            selected = selected,
                            onClick = { if (!selected) onNavigate(item.route) },
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.label,
                                    tint = if (selected) BrandOrangeDark else Color.White.copy(alpha = 0.8f)
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else Color.White.copy(alpha = 0.8f),
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White)
                        )
                    }
                }
            }
        }
    }
}
