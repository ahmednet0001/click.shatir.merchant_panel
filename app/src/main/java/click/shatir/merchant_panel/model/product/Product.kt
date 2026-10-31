package click.shatir.merchant_panel.model.product


import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int,
    val name: String,
    val price: Double? = null,
    val description: String? = null,
    val image: String? = null,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("is_active") val isActive: Int = 1,
    val category: CategoryRef? = null,
    val variants: List<Variant> = emptyList()
) {
    val isActiveBoolean: Boolean get() = isActive == 1

    // السعر المعروض: من المنتج أو من أول variant
    val displayPrice: Double?
        get() = price ?: variants.firstOrNull()?.priceDouble
}

data class CategoryRef(
    val id: Int,
    val name: String
)

data class Variant(
    val id: Int? = null,
    val size: String,
    val price: String,
    @SerializedName("product_id") val productId: Int? = null
) {
    val priceDouble: Double get() = price.toDoubleOrNull() ?: 0.0
}

data class ProductLite(
    val id: Int,
    val name: String
)

data class ProductRequest(
    @SerializedName("category_id") val categoryId: Int,
    val name: String,
    val price: Double,
    val description: String,
    val variants: List<ProductVariantRequest>? = null
)

data class ProductVariantRequest(
    val size: String,
    val price: Double
)
