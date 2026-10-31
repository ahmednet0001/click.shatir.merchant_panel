package click.shatir.merchant_panel.model.category


import com.google.gson.annotations.SerializedName

data class Category(
    val id: Int,
    val name: String,
    @SerializedName("store_id") val storeId: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("products_count") val productsCount: Int? = null
)

data class CategoryRequest(
    val name: String
)

data class MessageResponse(
    val message: String
)