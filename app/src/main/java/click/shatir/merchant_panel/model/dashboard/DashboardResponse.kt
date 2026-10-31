package click.shatir.merchant_panel.model.dashboard

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    val store: StoreInfo,
    val settings: StoreSettings,
    @SerializedName("package") val packageInfo: PackageInfo,
    val usage: Usage,
    val counts: Counts
)

data class StoreInfo(
    val id: Int,
    val name: String,
    val slug: String,
    val url: String
)

data class StoreSettings(
    val isOpen: Boolean,
    val isMenu: Boolean,
    val showProductImage: Boolean
)

data class PackageInfo(
    val name: String,
    val type: String
) {
    val isSimple: Boolean get() = type.equals("simple", ignoreCase = true)
}

data class Usage(
    val categories: UsageItem,
    val products: UsageItem,
    val additions: UsageItemExtended
)

data class UsageItem(
    val used: Int,
    val max: Int?,
    val unlimited: Boolean
)

data class UsageItemExtended(
    val used: Int,
    val max: Int?,
    val unlimited: Boolean,
    val supported: Boolean
)

data class Counts(
    val offers: Int,
    val shippingPrices: Int
)

// ردود أزرار التبديل
data class ToggleStatusResponse(
    @SerializedName("is_open") val isOpen: Boolean,
    val message: String
)

data class ToggleMenuResponse(
    @SerializedName("is_menu") val isMenu: Boolean,
    val message: String
)

data class ToggleProductImageResponse(
    @SerializedName("show_product_image") val showProductImage: Boolean,
    val message: String
)