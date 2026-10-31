package click.shatir.merchant_panel.ui.screens.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.data.repo.CategoryRepository
import click.shatir.merchant_panel.model.category.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false
)

class CategoriesViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenManager = TokenManager(app)
    private val repository = CategoryRepository(tokenManager)

    private val _state = MutableStateFlow(CategoriesUiState())
    val state: StateFlow<CategoriesUiState> = _state

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
            repository.getCategories(storeId).fold(
                onSuccess = { list ->
                    _state.value = _state.value.copy(isLoading = false, categories = list)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "فشل تحميل التصنيفات"
                    )
                }
            )
        }
    }

    fun create(name: String, onDone: () -> Unit) {
        val storeId = tokenManager.getStoreId() ?: return
        if (name.isBlank()) {
            _snackbar.value = "الاسم مطلوب"
            return
        }
        if (_state.value.isSaving) return

        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            repository.createCategory(storeId, name.trim()).fold(
                onSuccess = { newCat ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        categories = _state.value.categories + newCat
                    )
                    tokenManager.notifyCategoriesChanged()
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

    fun update(id: Int, name: String, onDone: () -> Unit) {
        val storeId = tokenManager.getStoreId() ?: return
        if (name.isBlank()) {
            _snackbar.value = "الاسم مطلوب"
            return
        }
        if (_state.value.isSaving) return

        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            repository.updateCategory(storeId, id, name.trim()).fold(
                onSuccess = { updated ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        categories = _state.value.categories.map {
                            if (it.id == updated.id) updated else it
                        }
                    )
                    tokenManager.notifyCategoriesChanged()
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
            repository.deleteCategory(storeId, id).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        categories = _state.value.categories.filterNot { it.id == id }
                    )
                    tokenManager.notifyCategoriesChanged()
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