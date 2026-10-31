package click.shatir.merchant_panel.ui.screens.offers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import click.shatir.merchant_panel.data.TokenManager
import click.shatir.merchant_panel.data.repo.OfferRepository
import click.shatir.merchant_panel.data.repo.ProductRepository
import click.shatir.merchant_panel.model.offer.Offer
import click.shatir.merchant_panel.model.offer.OfferRequest
import click.shatir.merchant_panel.model.product.Product
import click.shatir.merchant_panel.util.ServerTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OffersUiState(
    val isLoading: Boolean = true,
    val items: List<Offer> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val togglingIds: Set<Int> = emptySet(),
    val products: List<Product> = emptyList(),
    val productsLoading: Boolean = false,
    val productsError: String? = null
)

class OffersViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenManager = TokenManager(app)
    private val repository = OfferRepository(tokenManager)
    private val productRepository = ProductRepository(tokenManager, app.applicationContext)

    private val _state = MutableStateFlow(OffersUiState())
    val state: StateFlow<OffersUiState> = _state

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
            repository.getOffers(storeId).fold(
                onSuccess = { list ->
                    _state.value = _state.value.copy(isLoading = false, items = list)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "فشل تحميل العروض"
                    )
                }
            )
        }
    }

    /** تحديث صامت عند العودة للشاشة (العروض قد تنتهي أو تتغير من لوحة الويب). */
    fun refresh() {
        val s = _state.value
        if (s.isLoading || s.isSaving || refreshing) return
        if (s.error != null) {
            load()
            return
        }
        val storeId = tokenManager.getStoreId() ?: return
        refreshing = true
        viewModelScope.launch {
            repository.getOffers(storeId).onSuccess { list ->
                if (tokenManager.getStoreId() == storeId && !_state.value.isSaving) {
                    _state.value = _state.value.copy(items = list)
                }
            }
            refreshing = false
        }
    }

    /** يحمّل المنتجات مرة واحدة لاختيار منتج العرض. */
    fun loadProductsIfNeeded() {
        val s = _state.value
        if (s.productsLoading || (s.products.isNotEmpty() && s.productsError == null)) return
        val storeId = tokenManager.getStoreId() ?: return
        _state.value = s.copy(productsLoading = true, productsError = null)
        viewModelScope.launch {
            productRepository.getProducts(storeId).fold(
                onSuccess = { list ->
                    _state.value = _state.value.copy(
                        productsLoading = false,
                        products = list.sortedBy { it.name }
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        productsLoading = false,
                        productsError = e.message ?: "فشل تحميل المنتجات"
                    )
                }
            )
        }
    }

    fun create(request: OfferRequest, onDone: () -> Unit) {
        val storeId = tokenManager.getStoreId() ?: return
        if (_state.value.isSaving) return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            repository.createOffer(storeId, request).fold(
                onSuccess = {
                    _state.value = _state.value.copy(isSaving = false)
                    _snackbar.value = "تمت إضافة العرض"
                    onDone()
                    reloadSilently(storeId) // للحفاظ على ترتيب السيرفر (الأحدث أولاً)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isSaving = false)
                    _snackbar.value = e.message ?: "فشل الإضافة"
                }
            )
        }
    }

    fun update(id: Int, request: OfferRequest, onDone: () -> Unit) {
        val storeId = tokenManager.getStoreId() ?: return
        if (_state.value.isSaving) return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            repository.updateOffer(storeId, id, request).fold(
                onSuccess = { updated ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        items = _state.value.items.map { if (it.id == updated.id) updated else it }
                    )
                    _snackbar.value = "تم تحديث العرض"
                    onDone()
                    reloadSilently(storeId)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(isSaving = false)
                    _snackbar.value = e.message ?: "فشل التحديث"
                }
            )
        }
    }

    /** تفعيل/إيقاف سريع من القائمة. */
    fun toggleActive(offer: Offer) {
        val storeId = tokenManager.getStoreId() ?: return
        if (_state.value.togglingIds.contains(offer.id)) return
        _state.value = _state.value.copy(togglingIds = _state.value.togglingIds + offer.id)
        val request = OfferRequest(
            title = offer.title,
            scope = offer.scope,
            productId = if (offer.isProductScope) offer.productId else null,
            discountType = offer.discountType,
            discountValue = offer.discountValue,
            startsAt = ServerTime.parse(offer.startsAt)?.let { ServerTime.toRequest(it) } ?: offer.startsAt,
            endsAt = ServerTime.parse(offer.endsAt)?.let { ServerTime.toRequest(it) } ?: offer.endsAt,
            isActive = !offer.isActive
        )
        viewModelScope.launch {
            repository.updateOffer(storeId, offer.id, request).fold(
                onSuccess = { updated ->
                    _state.value = _state.value.copy(
                        items = _state.value.items.map { if (it.id == updated.id) updated else it }
                    )
                    _snackbar.value = if (updated.isActive) "تم تفعيل العرض" else "تم إيقاف العرض"
                },
                onFailure = { e -> _snackbar.value = e.message ?: "فشل التحديث" }
            )
            _state.value = _state.value.copy(togglingIds = _state.value.togglingIds - offer.id)
        }
    }

    fun delete(id: Int) {
        val storeId = tokenManager.getStoreId() ?: return
        viewModelScope.launch {
            repository.deleteOffer(storeId, id).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        items = _state.value.items.filterNot { it.id == id }
                    )
                    _snackbar.value = "تم حذف العرض"
                },
                onFailure = { e -> _snackbar.value = e.message ?: "فشل الحذف" }
            )
        }
    }

    fun showMessage(message: String) {
        _snackbar.value = message
    }

    fun onSnackbarShown() {
        _snackbar.value = null
    }

    private fun reloadSilently(storeId: Int) {
        viewModelScope.launch {
            repository.getOffers(storeId).onSuccess { list ->
                if (tokenManager.getStoreId() == storeId) {
                    _state.value = _state.value.copy(items = list)
                }
            }
        }
    }
}
