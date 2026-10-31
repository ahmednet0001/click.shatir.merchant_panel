package click.shatir.merchant_panel.ui.screens.products

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.data.repo.CategoryRepository
import click.shatir.merchant_panel.data.repo.ProductRepository
import click.shatir.merchant_panel.data.repo.SettingsRepository
import click.shatir.merchant_panel.data.repo.StoreRepository
import click.shatir.merchant_panel.model.category.Category
import click.shatir.merchant_panel.model.product.Product
import click.shatir.merchant_panel.model.product.ProductRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


data class ProductsUiState(
    val isLoading: Boolean = true,
    val items: List<Product> = emptyList(),
    val storeId: Int? = null,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val categoriesLoading: Boolean = true,
    val categoriesError: String? = null,
    val isSaving: Boolean = false,
    val hasAdvancedProductFeatures: Boolean = false,
    /** تسمية الأحجام من إعدادات المتجر (مفرد/جمع) */
    val variantLabelSingular: String = "حجم",
    val variantLabelPlural: String = "أحجام"
)

class ProductsViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenManager = TokenManager(app)
    private val repository = ProductRepository(tokenManager, app.applicationContext)
    private val categoryRepository = CategoryRepository(tokenManager)
    private val storeRepository = StoreRepository(tokenManager)
    private val settingsRepository = SettingsRepository(tokenManager, app.applicationContext)

    private val _state = MutableStateFlow(ProductsUiState())
    val state: StateFlow<ProductsUiState> = _state

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar

    private var loadGeneration = 0
    private var loadingStoreContextId: Int? = null
    private var loadedStoreContextId: Int? = null

    init {
        viewModelScope.launch {
            tokenManager.selectedStoreIdFlow().collect {
                refreshForSelectedStore()
            }
        }
        viewModelScope.launch {
            tokenManager.categoriesRevisionFlow().collect {
                val storeId = tokenManager.getStoreId()
                if (storeId != null && loadedStoreContextId == storeId) {
                    loadCategories()
                }
            }
        }
    }

    /** Reloads store-scoped products, categories, and package capabilities for the selected store. */
    fun refreshForSelectedStore() {
        val storeId = tokenManager.getStoreId()
        if (storeId == null) {
            loadGeneration++
            loadingStoreContextId = null
            loadedStoreContextId = null
            _state.value = ProductsUiState(
                isLoading = false,
                categoriesLoading = false,
                error = "لا يوجد متجر محدد",
                categoriesError = "لا يوجد متجر محدد"
            )
            return
        }
        if (_state.value.storeId == storeId &&
            (loadedStoreContextId == storeId || loadingStoreContextId == storeId)
        ) return

        val generation = if (_state.value.storeId == storeId) {
            loadGeneration
        } else {
            ++loadGeneration
        }
        if (_state.value.storeId != storeId) {
            loadedStoreContextId = null
            loadingStoreContextId = null
            _state.value = ProductsUiState(
                isLoading = true,
                storeId = storeId,
                categoriesLoading = true
            )
        }
        loadingStoreContextId = storeId

        viewModelScope.launch {
            storeRepository.getDashboard(storeId).fold(
                onSuccess = { dashboard ->
                    if (loadingStoreContextId == storeId) loadingStoreContextId = null
                    if (!isCurrentStore(storeId, generation)) return@fold
                    if (dashboard.store.id != storeId) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            categoriesLoading = false,
                            error = "بيانات المتجر المستلمة لا تطابق المتجر المحدد",
                            categoriesError = "بيانات المتجر المستلمة لا تطابق المتجر المحدد"
                        )
                        return@fold
                    }

                    loadedStoreContextId = storeId
                    val packageType = dashboard.packageInfo.type.trim()
                    tokenManager.savePackageType(packageType)
                    _state.value = _state.value.copy(
                        hasAdvancedProductFeatures =
                            !packageType.equals("simple", ignoreCase = true)
                    )
                    loadProductsFor(storeId, generation)
                    loadCategoriesFor(storeId, generation)
                    loadVariantLabelsFor(storeId, generation)
                },
                onFailure = { error ->
                    if (loadingStoreContextId == storeId) loadingStoreContextId = null
                    if (isCurrentStore(storeId, generation)) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            categoriesLoading = false,
                            error = error.message ?: "تعذر تحميل بيانات المتجر",
                            categoriesError = error.message ?: "تعذر تحميل بيانات المتجر"
                        )
                    }
                }
            )
        }
    }

    fun load() {
        val storeId = tokenManager.getStoreId() ?: return
        if (_state.value.storeId != storeId || loadedStoreContextId != storeId) {
            refreshForSelectedStore()
            return
        }
        val generation = loadGeneration
        loadProductsFor(storeId, generation)
    }

    private fun loadProductsFor(storeId: Int, generation: Int) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            repository.getProducts(storeId).fold(
                onSuccess = { list ->
                    if (isCurrentStore(storeId, generation)) {
                        _state.value = _state.value.copy(isLoading = false, items = list)
                    }
                },
                onFailure = { e ->
                    if (isCurrentStore(storeId, generation)) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.message ?: "فشل تحميل المنتجات"
                        )
                    }
                }
            )
        }
    }

    /** يُستدعى عند العودة للشاشة ليلتقط أي تغيير في تسمية الأحجام من الإعدادات. */
    fun refreshVariantLabels() {
        val storeId = tokenManager.getStoreId() ?: return
        if (_state.value.storeId != storeId || loadedStoreContextId != storeId) return
        loadVariantLabelsFor(storeId, loadGeneration)
    }

    /** محاولة صامتة: عند الفشل تبقى التسمية الافتراضية "حجم/أحجام". */
    private fun loadVariantLabelsFor(storeId: Int, generation: Int) {
        viewModelScope.launch {
            settingsRepository.getSettings(storeId).onSuccess { data ->
                if (isCurrentStore(storeId, generation)) {
                    _state.value = _state.value.copy(
                        variantLabelSingular = data.variantLabelSingular
                            ?.trim().orEmpty().ifBlank { "حجم" },
                        variantLabelPlural = data.variantLabelPlural
                            ?.trim().orEmpty().ifBlank { "أحجام" }
                    )
                }
            }
        }
    }

    fun loadCategories() {
        val storeId = tokenManager.getStoreId() ?: return
        if (_state.value.storeId != storeId || loadedStoreContextId != storeId) {
            refreshForSelectedStore()
            return
        }
        val generation = loadGeneration
        loadCategoriesFor(storeId, generation)
    }

    private fun loadCategoriesFor(storeId: Int, generation: Int) {
        _state.value = _state.value.copy(categoriesLoading = true, categoriesError = null)
        viewModelScope.launch {
            categoryRepository.getCategories(storeId).fold(
                onSuccess = { categories ->
                    if (isCurrentStore(storeId, generation)) {
                        _state.value = _state.value.copy(
                            categoriesLoading = false,
                            categories = categories
                        )
                    }
                },
                onFailure = { error ->
                    if (isCurrentStore(storeId, generation)) {
                        _state.value = _state.value.copy(
                            categoriesLoading = false,
                            categoriesError = error.message ?: "فشل تحميل التصنيفات"
                        )
                    }
                }
            )
        }
    }

    private fun isCurrentStore(storeId: Int, generation: Int): Boolean =
        generation == loadGeneration &&
            _state.value.storeId == storeId &&
            tokenManager.getStoreId() == storeId

    fun create(request: ProductRequest, imageUri: Uri?, onDone: () -> Unit) {
        val storeId = tokenManager.getStoreId() ?: run {
            _snackbar.value = "لا يوجد متجر محدد"
            return
        }
        if (_state.value.isSaving) return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            repository.createProduct(
                storeId,
                request,
                imageUri.takeIf { _state.value.hasAdvancedProductFeatures }
            ).fold(
                onSuccess = { product ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        items = (_state.value.items + product).sortedBy { it.name }
                    )
                    _snackbar.value = "تمت إضافة المنتج"
                    onDone()
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(isSaving = false)
                    _snackbar.value = error.message ?: "فشلت إضافة المنتج"
                }
            )
        }
    }

    fun update(
        id: Int,
        request: ProductRequest,
        imageUri: Uri?,
        removeImage: Boolean,
        onDone: () -> Unit
    ) {
        val storeId = tokenManager.getStoreId() ?: run {
            _snackbar.value = "لا يوجد متجر محدد"
            return
        }
        if (_state.value.isSaving) return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            repository.updateProduct(
                storeId,
                id,
                request,
                imageUri.takeIf { _state.value.hasAdvancedProductFeatures },
                removeImage && _state.value.hasAdvancedProductFeatures
            ).fold(
                onSuccess = { product ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        items = _state.value.items
                            .map { if (it.id == product.id) product else it }
                            .sortedBy { it.name }
                    )
                    _snackbar.value = "تم تحديث المنتج"
                    onDone()
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(isSaving = false)
                    _snackbar.value = error.message ?: "فشل تحديث المنتج"
                }
            )
        }
    }

    fun delete(id: Int) {
        val storeId = tokenManager.getStoreId() ?: return
        viewModelScope.launch {
            repository.deleteProduct(storeId, id).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        items = _state.value.items.filterNot { it.id == id }
                    )
                    _snackbar.value = "تم الحذف"
                },
                onFailure = { e -> _snackbar.value = e.message ?: "فشل الحذف" }
            )
        }
    }

    fun onSnackbarShown() {
        _snackbar.value = null
    }
}