package click.shatir.merchant_panel.ui.screens.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.data.repo.SettingsRepository
import click.shatir.merchant_panel.model.settings.SettingsRequest
import click.shatir.merchant_panel.model.settings.StoreSettingsData
import click.shatir.merchant_panel.model.settings.WorkingDayRequest
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

val WEEK_DAYS = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

val FULFILLMENT_METHODS = listOf(
    "takeaway" to "تيك أوي",
    "table" to "طاولة",
    "delivery" to "دليفري"
)

data class WorkingDayForm(
    val day: String,
    val isOpen: Boolean = true,
    val from: String = "09:00",
    val to: String = "22:00"
)

data class SettingsForm(
    val whatsappNumber: String = "",
    val contactPhone: String = "",
    val subtitle: String = "",
    val description: String = "",
    val welcomeMessage: String = "",
    val variantLabelSingular: String = "",
    val variantLabelPlural: String = "",
    val methods: Set<String> = FULFILLMENT_METHODS.map { it.first }.toSet(),
    val workingHours: List<WorkingDayForm> = WEEK_DAYS.map { WorkingDayForm(it) }
)

data class SettingsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val form: SettingsForm = SettingsForm(),
    val logoPath: String? = null,
    val newLogoUri: Uri? = null,
    val removeLogo: Boolean = false,
    val isSaving: Boolean = false,
    /** هل هناك تعديلات غير محفوظة؟ */
    val dirty: Boolean = false
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenManager = TokenManager(app)
    private val repository = SettingsRepository(tokenManager, app.applicationContext)

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar

    private var refreshing = false

    init {
        load()
    }

    fun load() {
        val storeId = tokenManager.getStoreId() ?: run {
            _state.value = _state.value.copy(isLoading = false, error = "لا يوجد متجر محدد")
            return
        }
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.getSettings(storeId).fold(
                onSuccess = { data -> _state.value = fromServer(data) },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "فشل تحميل الإعدادات"
                    )
                }
            )
        }
    }

    /** عند العودة للشاشة: حدّث بصمت فقط إن لم تكن هناك تعديلات غير محفوظة. */
    fun refresh() {
        val s = _state.value
        if (s.isLoading || s.isSaving || s.dirty || refreshing) return
        if (s.error != null) {
            load()
            return
        }
        val storeId = tokenManager.getStoreId() ?: return
        refreshing = true
        viewModelScope.launch {
            repository.getSettings(storeId).onSuccess { data ->
                val now = _state.value
                if (tokenManager.getStoreId() == storeId && !now.dirty && !now.isSaving) {
                    _state.value = fromServer(data)
                }
            }
            refreshing = false
        }
    }

    // ---------- تعديل الحقول ----------

    fun edit(transform: (SettingsForm) -> SettingsForm) {
        val s = _state.value
        _state.value = s.copy(form = transform(s.form), dirty = true)
    }

    fun toggleMethod(key: String) = edit { f ->
        val next = if (key in f.methods) f.methods - key else f.methods + key
        f.copy(methods = next)
    }

    fun updateDay(index: Int, transform: (WorkingDayForm) -> WorkingDayForm) = edit { f ->
        f.copy(workingHours = f.workingHours.mapIndexed { i, d -> if (i == index) transform(d) else d })
    }

    fun pickLogo(uri: Uri) {
        _state.value = _state.value.copy(newLogoUri = uri, removeLogo = false, dirty = true)
    }

    fun removeLogo() {
        val s = _state.value
        _state.value = s.copy(
            newLogoUri = null,
            removeLogo = s.logoPath != null,
            dirty = true
        )
    }

    // ---------- الحفظ ----------

    fun save() {
        val s = _state.value
        if (s.isSaving || s.isLoading) return
        val storeId = tokenManager.getStoreId() ?: return

        if (s.form.methods.isEmpty()) {
            _snackbar.value = "اختر طريقة استلام واحدة على الأقل"
            return
        }
        s.form.workingHours.firstOrNull { it.isOpen && (it.from.isBlank() || it.to.isBlank()) }?.let {
            _snackbar.value = "حدد وقت الفتح والإغلاق ليوم ${it.day}"
            return
        }

        val f = s.form
        val request = SettingsRequest(
            whatsappNumber = f.whatsappNumber.trim(),
            contactPhone = f.contactPhone.trim(),
            subtitle = f.subtitle.trim(),
            description = f.description.trim(),
            whatsappWelcomeMessage = f.welcomeMessage.trim(),
            variantLabelSingular = f.variantLabelSingular.trim(),
            variantLabelPlural = f.variantLabelPlural.trim(),
            // نفس ترتيب لوحة الويب
            allowedFulfillmentMethods = FULFILLMENT_METHODS.map { it.first }.filter { it in f.methods },
            workingHours = f.workingHours.map {
                WorkingDayRequest(it.day, it.isOpen, it.from, it.to)
            }
        )

        _state.value = s.copy(isSaving = true)
        viewModelScope.launch {
            repository.saveSettings(storeId, request, s.newLogoUri, s.removeLogo).fold(
                onSuccess = { data ->
                    _state.value = fromServer(data)
                    _snackbar.value = "تم حفظ إعدادات المتجر"
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isSaving = false)
                    _snackbar.value = e.message ?: "فشل الحفظ"
                }
            )
        }
    }

    fun showMessage(message: String) {
        _snackbar.value = message
    }

    fun onSnackbarShown() {
        _snackbar.value = null
    }

    // ---------- التحويل من رد السيرفر ----------

    private fun fromServer(data: StoreSettingsData): SettingsUiState {
        val methods = data.allowedFulfillmentMethods
            ?.filter { key -> FULFILLMENT_METHODS.any { it.first == key } }
            ?.toSet()
            ?.takeIf { it.isNotEmpty() }
            ?: FULFILLMENT_METHODS.map { it.first }.toSet()

        return SettingsUiState(
            isLoading = false,
            form = SettingsForm(
                whatsappNumber = data.whatsappNumber.orEmpty(),
                contactPhone = data.contactPhone.orEmpty(),
                subtitle = data.subtitle.orEmpty(),
                description = data.description.orEmpty(),
                welcomeMessage = data.whatsappWelcomeMessage.orEmpty(),
                variantLabelSingular = data.variantLabelSingular.orEmpty(),
                variantLabelPlural = data.variantLabelPlural.orEmpty(),
                methods = methods,
                workingHours = normalizeWorkingHours(data.workingHours)
            ),
            logoPath = data.logo?.takeIf { it.isNotBlank() }
        )
    }

    /** نفس منطق normalizeWorkingHours في لوحة الويب: 7 أيام بترتيب ثابت وقيم افتراضية. */
    private fun normalizeWorkingHours(element: JsonElement?): List<WorkingDayForm> {
        val byDay = mutableMapOf<String, JsonObject>()
        if (element != null && element.isJsonArray) {
            element.asJsonArray.forEach { item ->
                if (item.isJsonObject) {
                    val obj = item.asJsonObject
                    val day = obj.getString("day")
                    if (day != null) byDay[day] = obj
                }
            }
        }
        return WEEK_DAYS.map { day ->
            val obj = byDay[day]
            WorkingDayForm(
                day = day,
                isOpen = obj?.getBool("is_open") ?: true,
                from = obj?.getString("from")?.take(5)?.takeIf { it.isNotBlank() } ?: "09:00",
                to = obj?.getString("to")?.take(5)?.takeIf { it.isNotBlank() } ?: "22:00"
            )
        }
    }

    private fun JsonObject.getString(key: String): String? {
        val e = get(key) ?: return null
        return if (e.isJsonPrimitive) e.asString else null
    }

    /** القيمة قد تكون true أو 1 أو "1" أو "true" حسب مصدر الحفظ. */
    private fun JsonObject.getBool(key: String): Boolean? {
        val e = get(key) ?: return null
        if (!e.isJsonPrimitive) return null
        val p = e.asJsonPrimitive
        return when {
            p.isBoolean -> p.asBoolean
            p.isNumber -> p.asInt != 0
            else -> p.asString.trim().lowercase().let { it == "1" || it == "true" }
        }
    }
}
