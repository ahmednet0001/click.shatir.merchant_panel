package click.shatir.merchant_panel.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.data.repo.StoreRepository
import click.shatir.merchant_panel.model.dashboard.DashboardResponse
import click.shatir.merchant_panel.model.dashboard.StoreSettings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface DashboardState {
    data object Loading : DashboardState
    data object GoToStoreSelection : DashboardState
    data object NoStores : DashboardState
    data class Ready(val data: DashboardResponse) : DashboardState
    data class Error(val message: String) : DashboardState
}

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenManager = TokenManager(app)
    private val repository = StoreRepository(tokenManager)

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state

    // حالة أزرار التبديل (loading مستقل لكل زر)
    private val _toggling = MutableStateFlow<Set<String>>(emptySet())
    val toggling: StateFlow<Set<String>> = _toggling

    // رسالة مؤقتة (snackbar)
    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar

    private var refreshing = false

    init {
        load()
    }

    fun load() {
        _state.value = DashboardState.Loading
        viewModelScope.launch {
            repository.getStores().fold(
                onSuccess = { stores ->
                    when {
                        stores.isEmpty() -> _state.value = DashboardState.NoStores
                        stores.size == 1 -> {
                            tokenManager.saveStoreId(stores.first().id)
                            loadDashboard(stores.first().id)
                        }
                        else -> {
                            val savedId = tokenManager.getStoreId()
                            if (savedId != null && stores.any { it.id == savedId }) {
                                loadDashboard(savedId)
                            } else {
                                _state.value = DashboardState.GoToStoreSelection
                            }
                        }
                    }
                },
                onFailure = { e ->
                    _state.value = DashboardState.Error(e.message ?: "فشل تحميل المتاجر")
                }
            )
        }
    }

    /**
     * تحديث صامت للبيانات (بدون شاشة تحميل) — يُستدعى عند العودة للشاشة الرئيسية
     * لإظهار آخر البيانات بعد أي تعديل في المنتجات/الأصناف/الإضافات/الشحن.
     */
    fun refresh() {
        when (_state.value) {
            is DashboardState.Ready -> {
                if (_toggling.value.isNotEmpty() || refreshing) return
                val storeId = tokenManager.getStoreId() ?: return
                refreshing = true
                viewModelScope.launch {
                    repository.getDashboard(storeId).onSuccess { data ->
                        // تجاهل النتيجة إن تغيّر المتجر أثناء الطلب أو بدأ تبديل جديد
                        if (tokenManager.getStoreId() == storeId && _toggling.value.isEmpty()) {
                            tokenManager.savePackageType(data.packageInfo.type)
                            _state.value = DashboardState.Ready(data)
                        }
                    }
                    refreshing = false
                }
            }
            is DashboardState.Error -> load()
            else -> Unit // Loading: التحميل الأولي جارٍ
        }
    }

    fun switchStore() {
        tokenManager.clearStoreSelection()
    }

    fun onSnackbarShown() {
        _snackbar.value = null
    }

    fun toggleOpen() = toggleAction("open") { storeId ->
        repository.toggleStatus(storeId).map { response ->
            updateSettings { it.copy(isOpen = response.isOpen) }
            response.message
        }
    }

    fun toggleMenu() = toggleAction("menu") { storeId ->
        repository.toggleMenuMode(storeId).map { response ->
            updateSettings { it.copy(isMenu = response.isMenu) }
            response.message
        }
    }

    fun toggleProductImage() = toggleAction("image") { storeId ->
        repository.toggleProductImage(storeId).map { response ->
            updateSettings { it.copy(showProductImage = response.showProductImage) }
            response.message
        }
    }

    private fun updateSettings(transform: (StoreSettings) -> StoreSettings) {
        val current = _state.value
        if (current is DashboardState.Ready) {
            val newData = current.data.copy(settings = transform(current.data.settings))
            _state.value = DashboardState.Ready(newData)
        }
    }

    private fun toggleAction(
        key: String,
        block: suspend (Int) -> Result<String>
    ) {
        val storeId = tokenManager.getStoreId() ?: return
        if (_toggling.value.contains(key)) return

        _toggling.value = _toggling.value + key
        viewModelScope.launch {
            block(storeId).fold(
                onSuccess = { message -> _snackbar.value = message },
                onFailure = { e -> _snackbar.value = e.message ?: "فشل التحديث" }
            )
            _toggling.value = _toggling.value - key
        }
    }

    private fun loadDashboard(storeId: Int) {
        viewModelScope.launch {
            repository.getDashboard(storeId).fold(
                onSuccess = { data ->
                    tokenManager.savePackageType(data.packageInfo.type)
                    _state.value = DashboardState.Ready(data)
                },
                onFailure = { e ->
                    _state.value = DashboardState.Error(e.message ?: "فشل تحميل الداشبورد")
                }
            )
        }
    }
}