package click.shatir.merchant_panel.model.settings

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/** رد GET/POST stores/{id}/settings */
data class StoreSettingsData(
    val id: Int? = null,
    @SerializedName("whatsapp_number") val whatsappNumber: String? = null,
    @SerializedName("contact_phone") val contactPhone: String? = null,
    val logo: String? = null,
    val subtitle: String? = null,
    val description: String? = null,
    @SerializedName("whatsapp_welcome_message") val whatsappWelcomeMessage: String? = null,
    @SerializedName("variant_label_singular") val variantLabelSingular: String? = null,
    @SerializedName("variant_label_plural") val variantLabelPlural: String? = null,
    @SerializedName("allowed_fulfillment_methods") val allowedFulfillmentMethods: List<String>? = null,
    /** نقرؤها كـ JsonElement لأن القيم قد تكون "1" أو true حسب مصدر الحفظ */
    @SerializedName("working_hours") val workingHours: JsonElement? = null
)

data class SettingsRequest(
    @SerializedName("whatsapp_number") val whatsappNumber: String,
    @SerializedName("contact_phone") val contactPhone: String,
    val subtitle: String,
    val description: String,
    @SerializedName("whatsapp_welcome_message") val whatsappWelcomeMessage: String,
    @SerializedName("variant_label_singular") val variantLabelSingular: String,
    @SerializedName("variant_label_plural") val variantLabelPlural: String,
    @SerializedName("allowed_fulfillment_methods") val allowedFulfillmentMethods: List<String>,
    @SerializedName("working_hours") val workingHours: List<WorkingDayRequest>
)

data class WorkingDayRequest(
    val day: String,
    @SerializedName("is_open") val isOpen: Boolean,
    val from: String,
    val to: String
)
