package click.shatir.merchant_panel.ui.screens.offers

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import click.shatir.merchant_panel.R
import click.shatir.merchant_panel.model.offer.Offer
import click.shatir.merchant_panel.model.offer.OfferRequest
import click.shatir.merchant_panel.ui.components.AppCard
import click.shatir.merchant_panel.ui.components.AppColors
import click.shatir.merchant_panel.ui.components.AppFilterChip
import click.shatir.merchant_panel.ui.components.AppTextField
import click.shatir.merchant_panel.ui.components.ConfirmDeleteDialog
import click.shatir.merchant_panel.ui.components.EmptyState
import click.shatir.merchant_panel.ui.components.ErrorState
import click.shatir.merchant_panel.ui.components.FormDialog
import click.shatir.merchant_panel.ui.components.ListCountLabel
import click.shatir.merchant_panel.ui.components.PickerField
import click.shatir.merchant_panel.ui.components.RowActions
import click.shatir.merchant_panel.ui.components.ScreenScaffold
import click.shatir.merchant_panel.ui.components.SkeletonList
import click.shatir.merchant_panel.ui.components.StatusChip
import click.shatir.merchant_panel.ui.components.appSwitchColors
import click.shatir.merchant_panel.ui.components.autoFocus
import click.shatir.merchant_panel.ui.theme.BrandNavy
import click.shatir.merchant_panel.ui.theme.BrandOrangeDark
import click.shatir.merchant_panel.util.ServerTime

@Composable
fun OffersScreen(
    viewModel: OffersViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarMsg by viewModel.snackbar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var editing by remember { mutableStateOf<Offer?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var statusFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    val openAdd = {
        editing = null
        showDialog = true
    }

    ScreenScaffold(
        title = "العروض",
        snackbarHostState = snackbarHostState,
        fabLabel = "إضافة عرض",
        onFabClick = openAdd
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> SkeletonList(itemHeight = 140.dp, count = 4)

                state.error != null -> ErrorState(
                    message = state.error!!,
                    onRetry = { viewModel.load() }
                )

                state.items.isEmpty() -> EmptyState(
                    icon = R.drawable.ic_local_offer,
                    title = "لا توجد عروض بعد",
                    message = "أنشئ عرضاً بخصم على منتج أو على المتجر كله لتجذب عملاءً أكثر.",
                    actionLabel = "إضافة أول عرض",
                    onAction = openAdd
                )

                else -> {
                    // حالات العروض الموجودة فعلاً مع عدد كل حالة
                    val grouped = state.items.groupBy { statusOf(it).label }
                    val statusOrder = listOf("نشط الآن", "مجدول", "منتهي", "متوقف")
                    val filtered = if (statusFilter == null) state.items
                    else state.items.filter { statusOf(it).label == statusFilter }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppFilterChip(
                                label = "الكل (${state.items.size})",
                                selected = statusFilter == null,
                                onClick = { statusFilter = null }
                            )
                            statusOrder.forEach { label ->
                                val count = grouped[label]?.size ?: 0
                                if (count > 0) {
                                    AppFilterChip(
                                        label = "$label ($count)",
                                        selected = statusFilter == label,
                                        onClick = {
                                            statusFilter = if (statusFilter == label) null else label
                                        }
                                    )
                                }
                            }
                        }

                        if (filtered.isEmpty()) {
                            EmptyState(
                                icon = R.drawable.ic_local_offer,
                                title = "لا توجد عروض بهذه الحالة"
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                // مساحة سفلية حتى لا يغطي زر + آخر عنصر
                                contentPadding = PaddingValues(
                                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item { ListCountLabel("${filtered.size} عرض") }
                                items(filtered, key = { it.id }) { offer ->
                                    OfferCard(
                                        offer = offer,
                                        toggling = state.togglingIds.contains(offer.id),
                                        onToggle = { viewModel.toggleActive(offer) },
                                        onEdit = {
                                            editing = offer
                                            showDialog = true
                                        },
                                        onDelete = { viewModel.delete(offer.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        OfferDialog(
            initial = editing,
            state = state,
            onNeedProducts = { viewModel.loadProductsIfNeeded() },
            onDismiss = { showDialog = false },
            onConfirm = { request ->
                val current = editing
                if (current == null) {
                    viewModel.create(request) { showDialog = false }
                } else {
                    viewModel.update(current.id, request) { showDialog = false }
                }
            }
        )
    }
}

// ========== بطاقة العرض ==========

private class OfferStatus(val label: String, val color: Color)

private fun statusOf(offer: Offer): OfferStatus {
    val now = System.currentTimeMillis()
    val start = ServerTime.parse(offer.startsAt)
    val end = ServerTime.parse(offer.endsAt)
    return when {
        !offer.isActive -> OfferStatus("متوقف", Color(0xFF757575))
        end != null && now >= end -> OfferStatus("منتهي", Color(0xFFD32F2F))
        start != null && now < start -> OfferStatus("مجدول", Color(0xFF1976D2))
        else -> OfferStatus("نشط الآن", Color(0xFF2E7D32))
    }
}

/** نص مختصر للوقت المتبقي على انتهاء العرض النشط. */
private fun remainingText(offer: Offer): String? {
    val end = ServerTime.parse(offer.endsAt) ?: return null
    val left = end - System.currentTimeMillis()
    if (left <= 0) return null
    val days = left / (24L * 60 * 60 * 1000)
    val hours = left / (60L * 60 * 1000)
    return when {
        days >= 1 -> "ينتهي خلال $days يوم"
        hours >= 1 -> "ينتهي خلال $hours ساعة"
        else -> "ينتهي خلال أقل من ساعة"
    }
}

@Composable
private fun OfferCard(
    offer: Offer,
    toggling: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val status = statusOf(offer)

    AppCard(
        accent = status.color,
        contentPadding = PaddingValues(start = 14.dp, end = 6.dp, top = 14.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    offer.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_local_offer),
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (offer.isProductScope) {
                            "على منتج: ${offer.product?.name ?: "—"}"
                        } else {
                            "عام للمتجر"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                discountLabel(offer),
                color = BrandOrangeDark,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandOrangeDark.copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = null,
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "من ${ServerTime.displayIso(offer.startsAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
                Text(
                    "إلى ${ServerTime.displayIso(offer.endsAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(label = status.label, color = status.color)
            if (status.label == "نشط الآن") {
                remainingText(offer)?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (toggling) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = BrandOrangeDark
                )
                Spacer(Modifier.width(16.dp))
            } else {
                Switch(
                    checked = offer.isActive,
                    onCheckedChange = { onToggle() },
                    colors = appSwitchColors()
                )
            }
            RowActions(onEdit = onEdit, onDelete = { confirmDelete = true })
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            itemName = offer.title,
            title = "حذف العرض",
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

// ========== نافذة إضافة/تعديل عرض ==========

@Composable
private fun OfferDialog(
    initial: Offer?,
    state: OffersUiState,
    onNeedProducts: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (OfferRequest) -> Unit
) {
    val isSaving = state.isSaving

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var scope by remember { mutableStateOf(initial?.scope ?: Offer.SCOPE_STORE) }
    var productId by remember { mutableStateOf(initial?.productId) }
    var discountType by remember { mutableStateOf(initial?.discountType ?: Offer.TYPE_PERCENTAGE) }
    var valueText by remember {
        mutableStateOf(initial?.discountValue?.let { formatNumber(it) } ?: "")
    }
    var startMillis by remember {
        mutableLongStateOf(
            initial?.let { ServerTime.parse(it.startsAt) } ?: ServerTime.nowMillis()
        )
    }
    var endMillis by remember {
        mutableLongStateOf(
            initial?.let { ServerTime.parse(it.endsAt) }
                ?: (ServerTime.nowMillis() + 7L * 24 * 60 * 60 * 1000)
        )
    }
    var isActive by remember { mutableStateOf(initial?.isActive ?: false) }

    LaunchedEffect(scope) {
        if (scope == Offer.SCOPE_PRODUCT) onNeedProducts()
    }

    val value = valueText.toDoubleOrNull()
    val validationError: String? = when {
        title.isBlank() -> null // لا نزعج المستخدم قبل أن يكتب
        scope == Offer.SCOPE_PRODUCT && productId == null -> null
        value != null && value <= 0 -> "قيمة الخصم يجب أن تكون أكبر من صفر"
        discountType == Offer.TYPE_PERCENTAGE && value != null && value > 100 ->
            "نسبة الخصم لا يمكن أن تتجاوز 100%"
        endMillis <= startMillis -> "يجب أن تكون نهاية العرض بعد بدايته"
        else -> null
    }
    val canSave = !isSaving &&
            title.isNotBlank() &&
            (scope == Offer.SCOPE_STORE || productId != null) &&
            value != null && value > 0 &&
            !(discountType == Offer.TYPE_PERCENTAGE && value > 100) &&
            endMillis > startMillis

    FormDialog(
        title = if (initial == null) "إضافة عرض" else "تعديل العرض",
        isSaving = isSaving,
        canSave = canSave,
        errorText = validationError,
        onDismiss = onDismiss,
        onSave = {
            onConfirm(
                OfferRequest(
                    title = title.trim(),
                    scope = scope,
                    productId = if (scope == Offer.SCOPE_PRODUCT) productId else null,
                    discountType = discountType,
                    discountValue = value ?: 0.0,
                    startsAt = ServerTime.toRequest(startMillis),
                    endsAt = ServerTime.toRequest(endMillis),
                    isActive = isActive
                )
            )
        }
    ) {
        AppTextField(
            value = title,
            onValueChange = { if (it.length <= 255) title = it },
            label = "اسم العرض",
            enabled = !isSaving,
            modifier = Modifier.autoFocus(enabled = initial == null)
        )

        SectionLabel("نوع العرض")
        ChipsRow(
            options = listOf(
                Offer.SCOPE_STORE to "عام للمتجر",
                Offer.SCOPE_PRODUCT to "على منتج"
            ),
            selected = scope,
            enabled = !isSaving,
            onSelect = {
                scope = it
                if (it == Offer.SCOPE_STORE) productId = null
            }
        )

        if (scope == Offer.SCOPE_PRODUCT) {
            ProductPicker(
                state = state,
                selectedId = productId,
                fallbackName = initial?.takeIf { it.productId == productId }?.product?.name,
                enabled = !isSaving,
                onRetry = onNeedProducts,
                onSelect = { productId = it }
            )
        }

        SectionLabel("طريقة الخصم")
        ChipsRow(
            options = listOf(
                Offer.TYPE_PERCENTAGE to "نسبة مئوية",
                Offer.TYPE_FIXED to "مبلغ ثابت"
            ),
            selected = discountType,
            enabled = !isSaving,
            onSelect = { discountType = it }
        )

        AppTextField(
            value = valueText,
            onValueChange = { input ->
                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    valueText = input
                }
            },
            label = "قيمة الخصم",
            suffix = if (discountType == Offer.TYPE_PERCENTAGE) "%" else "ج.م",
            enabled = !isSaving,
            keyboardType = KeyboardType.Decimal
        )

        SectionLabel("مدة العرض")
        DateTimeField(
            label = "يبدأ في",
            millis = startMillis,
            enabled = !isSaving,
            onChange = { startMillis = it }
        )
        DateTimeField(
            label = "ينتهي في",
            millis = endMillis,
            enabled = !isSaving,
            onChange = { endMillis = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("تفعيل العرض", style = MaterialTheme.typography.bodyLarge, color = AppColors.TextPrimary)
                Text(
                    "يمكنك تفعيله أو إيقافه لاحقاً من القائمة",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
            Switch(
                checked = isActive,
                onCheckedChange = { isActive = it },
                enabled = !isSaving,
                colors = appSwitchColors()
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = BrandNavy)
}

@Composable
private fun ChipsRow(
    options: List<Pair<String, String>>,
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (key, label) ->
            AppFilterChip(
                label = label,
                selected = selected == key,
                onClick = { onSelect(key) },
                enabled = enabled,
                selectedColor = BrandOrangeDark,
                showCheck = true
            )
        }
    }
}

@Composable
private fun ProductPicker(
    state: OffersUiState,
    selectedId: Int?,
    fallbackName: String?,
    enabled: Boolean,
    onRetry: () -> Unit,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = state.products.firstOrNull { it.id == selectedId }?.name ?: fallbackName

    Column {
        SectionLabel("المنتج")
        Spacer(Modifier.height(6.dp))
        when {
            state.productsLoading -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = BrandOrangeDark
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("جارٍ تحميل المنتجات...", style = MaterialTheme.typography.bodySmall)
                }
            }

            state.productsError != null -> {
                Text(
                    state.productsError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onRetry) { Text("إعادة المحاولة") }
            }

            else -> {
                Box {
                    PickerField(
                        label = "المنتج",
                        value = selectedName ?: "اختر المنتج",
                        isPlaceholder = selectedName == null,
                        onClick = { expanded = true },
                        enabled = enabled,
                        icon = R.drawable.shopping_bag_24px,
                        showArrow = true
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        if (state.products.isEmpty()) {
                            DropdownMenuItem(text = { Text("لا توجد منتجات") }, onClick = { expanded = false })
                        }
                        state.products.forEach { product ->
                            DropdownMenuItem(
                                text = { Text(product.name) },
                                onClick = {
                                    expanded = false
                                    onSelect(product.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========== حقل التاريخ والوقت ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeField(
    label: String,
    millis: Long,
    enabled: Boolean,
    onChange: (Long) -> Unit
) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) }

    PickerField(
        label = label,
        value = ServerTime.display(millis),
        onClick = { showDate = true },
        enabled = enabled,
        icon = R.drawable.ic_calendar
    )

    if (showDate) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = ServerTime.toPickerMillis(millis)
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(
                    enabled = dateState.selectedDateMillis != null,
                    onClick = {
                        dateState.selectedDateMillis?.let {
                            pickedDate = ServerTime.fromPickerMillis(it)
                        }
                        showDate = false
                        showTime = true
                    }
                ) { Text("التالي") }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) { Text("إلغاء") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTime) {
        val parts = ServerTime.parts(millis)
        val timeState = rememberTimePickerState(
            initialHour = parts.hour,
            initialMinute = parts.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text(label) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val d = pickedDate
                    if (d != null) {
                        onChange(
                            ServerTime.build(d.first, d.second, d.third, timeState.hour, timeState.minute)
                        )
                    }
                    showTime = false
                }) { Text("تم") }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }) { Text("إلغاء") }
            }
        )
    }
}

// ========== دوال مساعدة ==========

private fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

private fun discountLabel(offer: Offer): String =
    if (offer.isPercentage) "${formatNumber(offer.discountValue)}%"
    else "${formatNumber(offer.discountValue)} ج.م"
