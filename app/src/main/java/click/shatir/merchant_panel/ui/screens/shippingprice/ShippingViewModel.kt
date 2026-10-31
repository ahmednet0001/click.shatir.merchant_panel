package click.shatir.merchant_panel.ui.screens.shippingprice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.data.repo.ShippingPriceRepository
import click.shatir.merchant_panel.model.shippingprice.ShippingPrice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ShippingUiState(
    val isLoading: Boolean = true,
    val items: List<ShippingPrice> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false
)

class ShippingViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenManager = TokenManager(app)
    private val repository = ShippingPriceRepository(tokenManager)

    private val _state = MutableStateFlow(ShippingUiState())
    val state: StateFlow<ShippingUiState> = _state

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar

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
            repository.getShippingPrices(storeId).fold(
                onSuccess = { list ->
                    _state.value = _state.value.copy(isLoading = false, items = list)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "فشل تحميل أسعار الشحن"
                    )
                }
            )
        }
    }

    fun create(city: String, price: Double, onDone: () -> Unit) {
        val storeId = tokenManager.getStoreId() ?: return
        if (_state.value.isSaving) return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            repository.createShippingPrice(storeId, city.trim(), price).fold(
                onSuccess = { newItem ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        items = _state.value.items + newItem
                    )
                    _snackbar.value = "تمت الإضافة"
                    onDone()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isSaving = false)
                    _snackbar.value = e.message ?: "فشل الإضافة"
                }
            )
        }
    }

    fun update(id: Int, city: String, price: Double, onDone: () -> Unit) {
        val storeId = tokenManager.getStoreId() ?: return
        if (_state.value.isSaving) return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            repository.updateShippingPrice(storeId, id, city.trim(), price).fold(
                onSuccess = { updated ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        items = _state.value.items.map {
                            if (it.id == updated.id) updated else it
                        }
                    )
                    _snackbar.value = "تم التحديث"
                    onDone()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isSaving = false)
                    _snackbar.value = e.message ?: "فشل التحديث"
                }
            )
        }
    }

    fun delete(id: Int) {
        val storeId = tokenManager.getStoreId() ?: return
        viewModelScope.launch {
            repository.deleteShippingPrice(storeId, id).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        items = _state.value.items.filterNot { it.id == id }
                    )
                    _snackbar.value = "تم الحذف"
                },
                onFailure = { e ->
                    _snackbar.value = e.message ?: "فشل الحذف"
                }
            )
        }
    }

    fun onSnackbarShown() {
        _snackbar.value = null
    }
}