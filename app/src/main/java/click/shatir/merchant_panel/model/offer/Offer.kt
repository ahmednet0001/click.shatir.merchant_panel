package click.shatir.merchant_panel.model.offer

import click.shatir.merchant_panel.model.product.ProductLite
import com.google.gson.annotations.SerializedName

data class Offer(
    val id: Int,
    val title: String,
    /** product | store */
    val scope: String,
    @SerializedName("product_id") val productId: Int? = null,
    /** percentage | fixed */
    @SerializedName("discount_type") val discountType: String,
    @SerializedName("discount_value") val discountValue: Double,
    /** ISO-8601 بتوقيت UTC كما يرجعها Laravel */
    @SerializedName("starts_at") val startsAt: String,
    @SerializedName("ends_at") val endsAt: String,
    @SerializedName("is_active") val isActive: Boolean = false,
    val product: ProductLite? = null
) {
    val isProductScope: Boolean get() = scope == SCOPE_PRODUCT
    val isPercentage: Boolean get() = discountType == TYPE_PERCENTAGE

    companion object {
        const val SCOPE_PRODUCT = "product"
        const val SCOPE_STORE = "store"
        const val TYPE_PERCENTAGE = "percentage"
        const val TYPE_FIXED = "fixed"
    }
}

data class OfferRequest(
    val title: String,
    val scope: String,
    @SerializedName("product_id") val productId: Int? = null,
    @SerializedName("discount_type") val discountType: String,
    @SerializedName("discount_value") val discountValue: Double,
    /** yyyy-MM-dd HH:mm:ss بتوقيت السيرفر */
    @SerializedName("starts_at") val startsAt: String,
    @SerializedName("ends_at") val endsAt: String,
    @SerializedName("is_active") val isActive: Boolean
)
