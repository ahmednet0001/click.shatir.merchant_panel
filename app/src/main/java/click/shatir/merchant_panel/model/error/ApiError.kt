package click.shatir.merchant_panel.model.error

data class ApiError(
    val message: String? = null,
    val errors: Map<String, List<String>>? = null
) {
    fun firstError(): String {
        errors?.values?.firstOrNull()?.firstOrNull()?.let { return it }
        return message ?: "حدث خطأ غير متوقع"
    }
}