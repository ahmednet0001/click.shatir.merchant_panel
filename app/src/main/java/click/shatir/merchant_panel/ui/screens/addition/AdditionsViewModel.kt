package click.shatir.merchant_panel.ui.screens.additions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import click.shatir.merchant_panel.data.repo.AdditionRepository
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.model.addition.Addition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdditionsUiState(
    val isLoading: Boolean = true,
    val additions: List<Addition> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false
)

class AdditionsViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenManager = TokenManager(app)
    private val repository = AdditionRepository(tokenManager)

    private val _state = MutableStateFlow(AdditionsUiState())
    val state: StateFlow<AdditionsUiState> = _state

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
            repository.getAdditions(storeId).fold(
                onSuccess = { list ->
                    _state.value = _state.value.copy(isLoading = false, additions = list)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "فشل تحميل الإضافات"
                    )
                }
            )
        }
    }

    fun create(name: String, price: Double, onDone: () -> Unit) {
        val storeId = tokenManager.getStoreId() ?: return
        if (_state.value.isSaving) return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            repository.createAddition(storeId, name.trim(), price).fold(
                onSuccess = { newItem ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        additions = _state.value.additions + newItem
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

    fun update(id: Int, name: String, price: Double, onDone: () -> Unit) {
        val storeId = tokenManager.getStoreId() ?: return
        if (_state.value.isSaving) return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            repository.updateAddition(storeId, id, name.trim(), price).fold(
                onSuccess = { updated ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        additions = _state.value.additions.map {
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
            repository.deleteAddition(storeId, id).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        additions = _state.value.additions.filterNot { it.id == id }
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