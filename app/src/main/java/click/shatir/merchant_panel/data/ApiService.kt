package click.shatir.merchant_panel.data


import click.shatir.merchant_panel.model.addition.Addition
import click.shatir.merchant_panel.model.addition.AdditionRequest
import click.shatir.merchant_panel.model.category.Category
import click.shatir.merchant_panel.model.category.CategoryRequest
import click.shatir.merchant_panel.model.category.MessageResponse
import click.shatir.merchant_panel.model.dashboard.DashboardResponse
import click.shatir.merchant_panel.model.dashboard.ToggleMenuResponse
import click.shatir.merchant_panel.model.dashboard.ToggleProductImageResponse
import click.shatir.merchant_panel.model.dashboard.ToggleStatusResponse
import click.shatir.merchant_panel.model.login.LoginRequest
import click.shatir.merchant_panel.model.login.LoginResponse
import click.shatir.merchant_panel.model.offer.Offer
import click.shatir.merchant_panel.model.offer.OfferRequest
import click.shatir.merchant_panel.model.product.Product
import click.shatir.merchant_panel.model.product.ProductRequest

import click.shatir.merchant_panel.model.shippingprice.ShippingPrice
import click.shatir.merchant_panel.model.shippingprice.ShippingPriceRequest
import click.shatir.merchant_panel.model.settings.SettingsRequest
import click.shatir.merchant_panel.model.settings.StoreSettingsData
import click.shatir.merchant_panel.model.store.Store
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // طلب خفيف للتحقق من أن السيرفر متاح
    @GET("ping")
    suspend fun ping()

    // التحقق من صلاحية التوكن — عدّل المسار حسب API لديك
    @GET("validate")
    suspend fun validateToken(
        @Header("Authorization") bearer: String
    )

    @POST("login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") bearer: String
    )


    // يرجّع مصفوفة مباشرة List<Store>
    @GET("stores")
    suspend fun getStores(@Header("Authorization") bearer: String): List<Store>

    @GET("stores/{id}/dashboard")
    suspend fun getDashboard(
        @Header("Authorization") bearer: String,
        @Path("id") storeId: Int
    ): DashboardResponse


    @PATCH("stores/{id}/status")
    suspend fun toggleStatus(
        @Header("Authorization") bearer: String,
        @Path("id") storeId: Int
    ): ToggleStatusResponse

    @PATCH("stores/{id}/menu-mode")
    suspend fun toggleMenuMode(
        @Header("Authorization") bearer: String,
        @Path("id") storeId: Int
    ): ToggleMenuResponse

    @PATCH("stores/{id}/product-image")
    suspend fun toggleProductImage(
        @Header("Authorization") bearer: String,
        @Path("id") storeId: Int
    ): ToggleProductImageResponse



    @GET("stores/{storeId}/categories")
    suspend fun getCategories(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int
    ): List<Category>

    @POST("stores/{storeId}/categories")
    suspend fun createCategory(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Body body: CategoryRequest
    ): Category

    @PUT("stores/{storeId}/categories/{categoryId}")
    suspend fun updateCategory(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Path("categoryId") categoryId: Int,
        @Body body: CategoryRequest
    ): Category

    @DELETE("stores/{storeId}/categories/{categoryId}")
    suspend fun deleteCategory(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Path("categoryId") categoryId: Int
    ): MessageResponse

    @GET("stores/{storeId}/additions")
    suspend fun getAdditions(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int
    ): List<Addition>

    @POST("stores/{storeId}/additions")
    suspend fun createAddition(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Body body: AdditionRequest
    ): Addition

    @PUT("stores/{storeId}/additions/{additionId}")
    suspend fun updateAddition(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Path("additionId") additionId: Int,
        @Body body: AdditionRequest
    ): Addition

    @DELETE("stores/{storeId}/additions/{additionId}")
    suspend fun deleteAddition(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Path("additionId") additionId: Int
    ): MessageResponse




    @GET("stores/{storeId}/shipping-prices")
    suspend fun getShippingPrices(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int
    ): List<ShippingPrice>

    @POST("stores/{storeId}/shipping-prices")
    suspend fun createShippingPrice(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Body body: ShippingPriceRequest
    ): ShippingPrice

    @PUT("stores/{storeId}/shipping-prices/{shippingId}")
    suspend fun updateShippingPrice(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Path("shippingId") shippingId: Int,
        @Body body: ShippingPriceRequest
    ): ShippingPrice

    @DELETE("stores/{storeId}/shipping-prices/{shippingId}")
    suspend fun deleteShippingPrice(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Path("shippingId") shippingId: Int
    ): MessageResponse
    @GET("stores/{storeId}/products")
    suspend fun getProducts(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int
    ): List<Product>

    @POST("stores/{storeId}/products")
    suspend fun createProduct(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Body body: ProductRequest
    ): Product

    @Multipart
    @POST("stores/{storeId}/products")
    suspend fun createProductWithImage(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Part parts: List<MultipartBody.Part>
    ): Product

    @PUT("stores/{storeId}/products/{productId}")
    suspend fun updateProduct(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Path("productId") productId: Int,
        @Body body: ProductRequest
    ): Product

    @Multipart
    @POST("stores/{storeId}/products/{productId}")
    suspend fun updateProductImage(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Path("productId") productId: Int,
        @Part parts: List<MultipartBody.Part>
    ): Product

    @DELETE("stores/{storeId}/products/{productId}")
    suspend fun deleteProduct(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Path("productId") productId: Int
    ): MessageResponse

    // ========== العروض ==========

    @GET("stores/{storeId}/offers")
    suspend fun getOffers(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int
    ): List<Offer>

    @POST("stores/{storeId}/offers")
    suspend fun createOffer(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Body body: OfferRequest
    ): Offer

    @PUT("stores/{storeId}/offers/{offerId}")
    suspend fun updateOffer(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Path("offerId") offerId: Int,
        @Body body: OfferRequest
    ): Offer

    @DELETE("stores/{storeId}/offers/{offerId}")
    suspend fun deleteOffer(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Path("offerId") offerId: Int
    ): MessageResponse

    // ========== إعدادات المتجر ==========

    @GET("stores/{storeId}/settings")
    suspend fun getSettings(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int
    ): StoreSettingsData

    // JSON: للحفاظ على أنواع القيم (boolean/array) في working_hours
    @POST("stores/{storeId}/settings")
    suspend fun updateSettings(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Body body: SettingsRequest
    ): StoreSettingsData

    // Multipart: رفع/حذف الشعار فقط
    @Multipart
    @POST("stores/{storeId}/settings")
    suspend fun updateSettingsLogo(
        @Header("Authorization") bearer: String,
        @Path("storeId") storeId: Int,
        @Part parts: List<MultipartBody.Part>
    ): StoreSettingsData

}
