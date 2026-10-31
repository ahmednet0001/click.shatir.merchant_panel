package click.shatir.merchant_panel.ui.screens.store


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.data.repo.StoreRepository
import click.shatir.merchant_panel.model.store.Store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StoreSelectionUiState(
    val isLoading: Boolean = true,
    val stores: List<Store> = emptyList(),
    val error: String? = null
)

class StoreSelectionViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenManager = TokenManager(app)
    private val repository = StoreRepository(tokenManager)

    private val _state = MutableStateFlow(StoreSelectionUiState())
    val state: StateFlow<StoreSelectionUiState> = _state

    init {
        loadStores()
    }

    fun loadStores() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.getStores().fold(
                onSuccess = { list ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        stores = list,
                        error = if (list.isEmpty()) "لا توجد متاجر مرتبطة بحسابك" else null
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "فشل تحميل المتاجر"
                    )
                }
            )
        }
    }



    fun selectStore(store: Store) {
        tokenManager.saveStoreId(store.id)
    }
}
