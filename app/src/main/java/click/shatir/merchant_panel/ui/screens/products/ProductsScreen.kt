package click.shatir.merchant_panel.ui.screens.products

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import click.shatir.merchant_panel.R
import click.shatir.merchant_panel.model.category.Category
import click.shatir.merchant_panel.model.product.Product
import click.shatir.merchant_panel.model.product.ProductRequest
import click.shatir.merchant_panel.model.product.ProductVariantRequest
import click.shatir.merchant_panel.ui.components.AppCard
import click.shatir.merchant_panel.ui.components.AppColors
import click.shatir.merchant_panel.ui.components.AppFilterChip
import click.shatir.merchant_panel.ui.components.AppSearchField
import click.shatir.merchant_panel.ui.components.AppTextField
import click.shatir.merchant_panel.ui.components.ConfirmDeleteDialog
import click.shatir.merchant_panel.ui.components.EmptyState
import click.shatir.merchant_panel.ui.components.ErrorState
import click.shatir.merchant_panel.ui.components.FormDialog
import click.shatir.merchant_panel.ui.components.ListCountLabel
import click.shatir.merchant_panel.ui.components.RowActions
import click.shatir.merchant_panel.ui.components.ScreenScaffold
import click.shatir.merchant_panel.ui.components.SecondaryButton
import click.shatir.merchant_panel.ui.components.SkeletonList
import click.shatir.merchant_panel.ui.components.StatusChip
import click.shatir.merchant_panel.ui.components.autoFocus
import click.shatir.merchant_panel.ui.theme.BrandNavy
import click.shatir.merchant_panel.ui.theme.BrandOrangeDark
import click.shatir.merchant_panel.util.Constants
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarMsg by viewModel.snackbar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showProductForm by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var removeProductImage by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            pickedImageUri = uri
            removeProductImage = false
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshVariantLabels()
    }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    val openAdd = {
        editingProduct = null
        pickedImageUri = null
        removeProductImage = false
        showProductForm = true
    }

    ScreenScaffold(
        title = "المنتجات",
        snackbarHostState = snackbarHostState,
        fabLabel = "إضافة منتج",
        onFabClick = openAdd
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> SkeletonList(itemHeight = 92.dp)

                state.error != null -> ErrorState(
                    message = state.error!!,
                    onRetry = { viewModel.load() }
                )

                state.items.isEmpty() -> EmptyState(
                    icon = R.drawable.shopping_bag_24px,
                    title = "لا توجد منتجات بعد",
                    message = "أضف منتجاتك ليتمكن العملاء من تصفحها وطلبها.",
                    actionLabel = "إضافة أول منتج",
                    onAction = openAdd
                )

                else -> {
                    // التصنيفات المتاحة للتصفية مأخوذة من المنتجات الموجودة فعلاً
                    val filterCategories = remember(state.items) {
                        state.items.mapNotNull { it.category }.distinctBy { it.id }
                    }
                    val filtered = remember(state.items, query, selectedCategoryId) {
                        val q = query.trim()
                        state.items.filter { product ->
                            (selectedCategoryId == null || product.categoryId == selectedCategoryId) &&
                                (q.isEmpty() || product.name.contains(q, ignoreCase = true))
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        AppSearchField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = "ابحث عن منتج",
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
                        )
                        if (filterCategories.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppFilterChip(
                                    label = "الكل",
                                    selected = selectedCategoryId == null,
                                    onClick = { selectedCategoryId = null }
                                )
                                filterCategories.forEach { category ->
                                    AppFilterChip(
                                        label = category.name,
                                        selected = selectedCategoryId == category.id,
                                        onClick = {
                                            selectedCategoryId =
                                                if (selectedCategoryId == category.id) null else category.id
                                        }
                                    )
                                }
                            }
                        }

                        if (filtered.isEmpty()) {
                            EmptyState(
                                icon = R.drawable.ic_search,
                                title = "لا توجد نتائج",
                                message = "جرّب كلمة بحث مختلفة أو اختر تصنيفاً آخر."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                // مساحة سفلية حتى لا يغطي زر الإضافة آخر عنصر
                                contentPadding = PaddingValues(
                                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item { ListCountLabel("${filtered.size} منتج") }
                                items(filtered, key = { it.id }) { product ->
                                    ProductRow(
                                        product = product,
                                        showAdvancedFeatures = state.hasAdvancedProductFeatures,
                                        variantLabelPlural = state.variantLabelPlural,
                                        onEdit = {
                                            editingProduct = product
                                            pickedImageUri = null
                                            removeProductImage = false
                                            showProductForm = true
                                        },
                                        onDelete = { viewModel.delete(product.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProductForm) {
        ProductFormDialog(
            initial = editingProduct,
            categories = state.categories,
            categoriesLoading = state.categoriesLoading,
            categoriesError = state.categoriesError,
            showAdvancedFeatures = state.hasAdvancedProductFeatures,
            variantLabelSingular = state.variantLabelSingular,
            variantLabelPlural = state.variantLabelPlural,
            pickedImageUri = pickedImageUri,
            removeImage = removeProductImage,
            isSaving = state.isSaving,
            onRetryCategories = viewModel::loadCategories,
            onPickImage = { imagePicker.launch("image/*") },
            onRemoveImage = {
                if (removeProductImage) {
                    removeProductImage = false
                } else {
                    pickedImageUri = null
                    removeProductImage = editingProduct?.image != null
                }
            },
            onDismiss = { if (!state.isSaving) showProductForm = false },
            onConfirm = { request, imageUri, removeImage ->
                val product = editingProduct
                if (product == null) {
                    viewModel.create(request, imageUri) { showProductForm = false }
                } else {
                    viewModel.update(product.id, request, imageUri, removeImage) {
                        showProductForm = false
                    }
                }
            }
        )
    }
}

@Composable
private fun ProductRow(
    product: Product,
    showAdvancedFeatures: Boolean,
    variantLabelPlural: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }

    AppCard(contentPadding = PaddingValues(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showAdvancedFeatures) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEEF1F5)),
                    contentAlignment = Alignment.Center
                ) {
                    if (product.image != null) {
                        AsyncImage(
                            model = Constants.STORAGE_URL + product.image,
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_image),
                            contentDescription = null,
                            tint = AppColors.Hint,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.category?.name ?: "بدون تصنيف",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!product.isActiveBoolean) {
                        Spacer(Modifier.width(6.dp))
                        StatusChip(label = "متوقف", color = AppColors.Danger)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val price = product.displayPrice
                    if (price != null) {
                        Text(
                            formatPrice(price),
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrandOrangeDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (showAdvancedFeatures && product.variants.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${product.variants.size} $variantLabelPlural",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }

            RowActions(
                onEdit = onEdit,
                onDelete = { confirmDelete = true },
                vertical = true
            )
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            itemName = product.name,
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

private data class ProductVariantDraft(
    val size: String = "",
    val price: String = ""
)

@Composable
private fun FormSectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = BrandNavy
    )
}

@Composable
private fun ProductFormDialog(
    initial: Product?,
    categories: List<Category>,
    categoriesLoading: Boolean,
    categoriesError: String?,
    showAdvancedFeatures: Boolean,
    variantLabelSingular: String,
    variantLabelPlural: String,
    pickedImageUri: Uri?,
    removeImage: Boolean,
    isSaving: Boolean,
    onRetryCategories: () -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (ProductRequest, Uri?, Boolean) -> Unit
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var priceText by remember(initial?.id) {
        mutableStateOf(initial?.price?.let(::formatPriceInput).orEmpty())
    }
    var description by remember(initial?.id) {
        mutableStateOf(initial?.description.orEmpty())
    }
    var selectedCategoryId by remember(initial?.id, categories) {
        mutableStateOf(
            initial?.categoryId?.takeIf { id -> categories.any { it.id == id } }
                ?: categories.firstOrNull()?.id
        )
    }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val variantDrafts = remember(initial?.id, showAdvancedFeatures) {
        mutableStateListOf<ProductVariantDraft>().apply {
            if (showAdvancedFeatures) initial?.variants?.forEach { variant ->
                add(ProductVariantDraft(variant.size, formatPriceInput(variant.priceDouble)))
            }
        }
    }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    var validationError by remember(initial?.id) { mutableStateOf<String?>(null) }

    val parsedPrice = parsePriceInput(priceText)
    val parsedVariantPrices = variantDrafts.map { parsePriceInput(it.price) }
    val hasVariants = showAdvancedFeatures && variantDrafts.isNotEmpty()
    val fallbackVariantPrice = if (hasVariants) {
        parsedVariantPrices.firstOrNull { it != null }
    } else {
        null
    }
    val effectivePrice = if (hasVariants) fallbackVariantPrice else parsedPrice
    val hasInvalidVariant = showAdvancedFeatures && variantDrafts.indices.any { index ->
        variantDrafts[index].size.isBlank() ||
            (parsedVariantPrices[index]?.let { it.isFinite() && it >= 0.0 } != true)
    }

    FormDialog(
        title = if (initial == null) "إضافة منتج" else "تعديل المنتج",
        fullHeight = true,
        isSaving = isSaving,
        scrollState = scrollState,
        errorText = validationError,
        onDismiss = onDismiss,
        onSave = {
            val error = when {
                name.isBlank() -> "اكتب اسم المنتج أولًا."
                selectedCategoryId == null ->
                    if (categoriesLoading) {
                        "انتظر تحميل التصنيفات."
                    } else {
                        "اختر تصنيفًا للمنتج."
                    }
                hasInvalidVariant ->
                    "أكمل اسم وسعر كل $variantLabelSingular أو احذفه."
                effectivePrice == null ||
                    !effectivePrice.isFinite() ||
                    effectivePrice < 0.0 ->
                    "اكتب سعرًا صحيحًا للمنتج."
                else -> null
            }
            if (error != null) {
                validationError = error
            } else {
                validationError = null
                val request = ProductRequest(
                    categoryId = selectedCategoryId!!,
                    name = name.trim(),
                    price = effectivePrice!!,
                    description = description.trim(),
                    variants = if (showAdvancedFeatures) {
                        variantDrafts.mapIndexed { index, draft ->
                            ProductVariantRequest(
                                size = draft.size.trim(),
                                price = parsedVariantPrices[index]!!
                            )
                        }
                    } else {
                        null
                    }
                )
                onConfirm(
                    request,
                    pickedImageUri.takeIf { showAdvancedFeatures },
                    removeImage && showAdvancedFeatures
                )
            }
        }
    ) {
        AppTextField(
            value = name,
            onValueChange = { name = it.take(255) },
            label = "اسم المنتج",
            enabled = !isSaving,
            modifier = Modifier.autoFocus(enabled = initial == null)
        )

        Box {
            val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
            AppTextField(
                value = selectedCategory?.name.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = "التصنيف",
                placeholder = "اختر تصنيفًا",
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_expand_more),
                        contentDescription = null,
                        tint = BrandNavy
                    )
                },
                enabled = !isSaving && categories.isNotEmpty()
            )
            // TextField.readOnly consumes taps itself; this transparent layer
            // makes the dropdown reliably open across Material3 versions.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = !isSaving && categories.isNotEmpty()) {
                        categoryMenuExpanded = true
                    }
            )
            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false },
                modifier = Modifier.heightIn(max = 280.dp)
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            selectedCategoryId = category.id
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
        }

        if (categoriesLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = BrandOrangeDark
            )
        } else if (categoriesError != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    categoriesError,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onRetryCategories, enabled = !isSaving) {
                    Text("إعادة المحاولة")
                }
            }
        } else if (categories.isEmpty()) {
            Column {
                Text(
                    "لا توجد تصنيفات. أضف تصنيفًا أولًا ثم أعد تحميل القائمة.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onRetryCategories, enabled = !isSaving) {
                    Text("إعادة تحميل التصنيفات")
                }
            }
        }

        if (!hasVariants) {
            AppTextField(
                value = priceText,
                onValueChange = { input ->
                    priceText = normalizePriceInput(input)
                },
                label = if (showAdvancedFeatures) {
                    "السعر الأساسي (عند عدم وجود $variantLabelPlural)"
                } else {
                    "السعر"
                },
                suffix = "ج.م",
                enabled = !isSaving,
                keyboardType = KeyboardType.Decimal
            )
        } else {
            Text(
                "السعر الأساسي مخفي لوجود $variantLabelPlural؛ سيُستخدم سعر أول $variantLabelSingular.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }

        AppTextField(
            value = description,
            onValueChange = { description = it.take(2000) },
            label = "الوصف (اختياري)",
            enabled = !isSaving,
            singleLine = false,
            minLines = 2,
            maxLines = 4
        )

        if (showAdvancedFeatures) {
            ProductImagePickerSection(
                initialImage = initial?.image,
                pickedImageUri = pickedImageUri,
                removeImage = removeImage,
                isSaving = isSaving,
                onPickImage = onPickImage,
                onRemoveImage = onRemoveImage
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FormSectionTitle("$variantLabelPlural والأسعار")
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        variantDrafts.add(ProductVariantDraft())
                        scope.launch {
                            withFrameNanos { }
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    },
                    enabled = !isSaving
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("إضافة $variantLabelSingular")
                }
            }

            variantDrafts.forEachIndexed { index, draft ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.ScreenBackground)
                        .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTextField(
                        value = draft.size,
                        onValueChange = { value ->
                            variantDrafts[index] = draft.copy(size = value.take(50))
                        },
                        label = variantLabelSingular,
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f)
                    )
                    AppTextField(
                        value = draft.price,
                        onValueChange = { value ->
                            variantDrafts[index] = draft.copy(
                                price = normalizePriceInput(value)
                            )
                        },
                        label = "السعر",
                        enabled = !isSaving,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { variantDrafts.removeAt(index) },
                        enabled = !isSaving
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "حذف $variantLabelSingular",
                            tint = AppColors.Danger,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductImagePickerSection(
    initialImage: String?,
    pickedImageUri: Uri?,
    removeImage: Boolean,
    isSaving: Boolean,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit
) {
    val previewModel: Any? = when {
        pickedImageUri != null -> pickedImageUri
        initialImage != null && !removeImage -> Constants.STORAGE_URL + initialImage
        else -> null
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FormSectionTitle("صورة المنتج")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEEF1F5)),
                contentAlignment = Alignment.Center
            ) {
                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "صورة المنتج",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (removeImage) {
                    Text(
                        "ستُحذف",
                        color = AppColors.Danger,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_image),
                        contentDescription = null,
                        tint = AppColors.Hint,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SecondaryButton(
                    text = if (previewModel == null) "اختيار صورة" else "تغيير الصورة",
                    onClick = onPickImage,
                    enabled = !isSaving,
                    icon = R.drawable.ic_image,
                    modifier = Modifier.fillMaxWidth()
                )
                if (initialImage != null || pickedImageUri != null) {
                    TextButton(onClick = onRemoveImage, enabled = !isSaving) {
                        Text(
                            if (removeImage) "التراجع عن الحذف" else "إزالة الصورة",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        Text(
            "سيتم ضغط الصورة تلقائيًا لتناسب حد الخادم (512 كيلوبايت).",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary
        )
    }
}

private fun normalizePriceInput(input: String): String {
    val output = StringBuilder()
    var decimalPointSeen = false

    input.forEach { character ->
        val normalized = when (character) {
            in '\u0660'..'\u0669' -> ('0'.code + (character.code - '\u0660'.code)).toChar()
            in '\u06F0'..'\u06F9' -> ('0'.code + (character.code - '\u06F0'.code)).toChar()
            '\u066B', ',' -> '.'
            else -> character
        }
        when {
            normalized in '0'..'9' -> output.append(normalized)
            normalized == '.' && !decimalPointSeen -> {
                output.append(normalized)
                decimalPointSeen = true
            }
        }
    }
    return output.toString()
}

private fun parsePriceInput(input: String): Double? =
    normalizePriceInput(input).toDoubleOrNull()?.takeIf { it.isFinite() }

@SuppressLint("DefaultLocale")
private fun formatPriceInput(price: Double): String {
    return if (price % 1.0 == 0.0) price.toInt().toString()
    else String.format("%.2f", price)
}

private fun formatPrice(price: Double): String {
    return if (price % 1.0 == 0.0) "${price.toInt()} ج.م"
    else String.format("%.2f ج.م", price)
}