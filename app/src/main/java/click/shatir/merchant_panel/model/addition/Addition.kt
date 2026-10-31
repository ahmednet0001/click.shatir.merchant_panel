package click.shatir.merchant_panel.model.addition


import com.google.gson.annotations.SerializedName

data class Addition(
    val id: Int,
    val name: String,
    val price: Double,
    @SerializedName("store_id") val storeId: Int? = null
)

data class AdditionRequest(
    val name: String,
    val price: Double
)