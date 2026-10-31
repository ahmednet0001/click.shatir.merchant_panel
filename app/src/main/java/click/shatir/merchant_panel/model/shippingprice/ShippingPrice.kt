package click.shatir.merchant_panel.model.shippingprice


import com.google.gson.annotations.SerializedName

data class ShippingPrice(
    val id: Int,
    val city: String,
    val price: Double,
    @SerializedName("store_id") val storeId: Int? = null
)

data class ShippingPriceRequest(
    val city: String,
    val price: Double
)