package click.shatir.merchant_panel.ui.screens.additions

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import click.shatir.merchant_panel.R
import click.shatir.merchant_panel.model.addition.Addition
import click.shatir.merchant_panel.ui.components.AppCard
import click.shatir.merchant_panel.ui.components.AppColors
import click.shatir.merchant_panel.ui.components.AppSearchField
import click.shatir.merchant_panel.ui.components.AppTextField
import click.shatir.merchant_panel.ui.components.ConfirmDeleteDialog
import click.shatir.merchant_panel.ui.components.EmptyState
import click.shatir.merchant_panel.ui.components.ErrorState
import click.shatir.merchant_panel.ui.components.FormDialog
import click.shatir.merchant_panel.ui.components.IconBadge
import click.shatir.merchant_panel.ui.components.ListCountLabel
import click.shatir.merchant_panel.ui.components.RowActions
import click.shatir.merchant_panel.ui.components.ScreenScaffold
import click.shatir.merchant_panel.ui.components.SkeletonList
import click.shatir.merchant_panel.ui.components.autoFocus
import click.shatir.merchant_panel.ui.theme.BrandNavy
import click.shatir.merchant_panel.ui.theme.BrandOrangeDark

/** عدد العناصر الذي يظهر عنده حقل البحث. */
private const val SEARCH_THRESHOLD = 6

@Composable
fun AdditionsScreen(
    viewModel: AdditionsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarMsg by viewModel.snackbar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var editing by remember { mutableStateOf<Addition?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    val openAdd = {
        editing = null
        showDialog = true
    }

    ScreenScaffold(
        title = "الإضافات",
        snackbarHostState = snackbarHostState,
        fabLabel = "إضافة جديدة",
        onFabClick = openAdd
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> SkeletonList(itemHeight = 72.dp)

                state.error != null -> ErrorState(
                    message = state.error!!,
                    onRetry = { viewModel.load() }
                )

                state.additions.isEmpty() -> EmptyState(
                    icon = R.drawable.add_circle_24px,
                    title = "لا توجد إضافات بعد",
                    message = "الإضافات خيارات يمكن للعميل إضافتها لطلبه، مثل الجبن الزائد أو الصوص.",
                    actionLabel = "إضافة أول إضافة",
                    onAction = openAdd
                )

                else -> {
                    val filtered = remember(state.additions, query) {
                        val q = query.trim()
                        if (q.isEmpty()) state.additions
                        else state.additions.filter { it.name.contains(q, ignoreCase = true) }
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (state.additions.size >= SEARCH_THRESHOLD) {
                            AppSearchField(
                                value = query,
                                onValueChange = { query = it },
                                placeholder = "ابحث في الإضافات",
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
                            )
                        }
                        if (filtered.isEmpty()) {
                            EmptyState(
                                icon = R.drawable.ic_search,
                                title = "لا توجد نتائج",
                                message = "لا يوجد ما يطابق \"${query.trim()}\""
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                // مساحة سفلية حتى لا يغطي زر الإضافة آخر عنصر
                                contentPadding = PaddingValues(
                                    start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item { ListCountLabel("${filtered.size} إضافة") }
                                items(filtered, key = { it.id }) { item ->
                                    ItemRow(
                                        item = item,
                                        onEdit = {
                                            editing = item
                                            showDialog = true
                                        },
                                        onDelete = { viewModel.delete(item.id) }
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
        ItemDialog(
            initial = editing,
            isSaving = state.isSaving,
            onDismiss = { showDialog = false },
            onConfirm = { name, price ->
                if (editing == null) {
                    viewModel.create(name, price) { showDialog = false }
                } else {
                    viewModel.update(editing!!.id, name, price) { showDialog = false }
                }
            }
        )
    }
}

@Composable
private fun ItemRow(
    item: Addition,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }

    AppCard(contentPadding = PaddingValues(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = R.drawable.add_circle_24px)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    formatPrice(item.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandOrangeDark,
                    fontWeight = FontWeight.Bold
                )
            }
            RowActions(onEdit = onEdit, onDelete = { confirmDelete = true })
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            itemName = item.name,
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun ItemDialog(
    initial: Addition?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var priceText by remember {
        mutableStateOf(initial?.price?.let { formatPriceNumber(it) } ?: "")
    }

    val price = priceText.toDoubleOrNull()
    val canSave = !isSaving && name.isNotBlank() && price != null && price >= 0

    FormDialog(
        title = if (initial == null) "إضافة جديدة" else "تعديل الإضافة",
        isSaving = isSaving,
        canSave = canSave,
        onDismiss = onDismiss,
        onSave = { onConfirm(name, price ?: 0.0) }
    ) {
        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = "اسم الإضافة",
            enabled = !isSaving,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.autoFocus(enabled = initial == null)
        )
        AppTextField(
            value = priceText,
            onValueChange = { input ->
                // السماح بأرقام ونقطة عشرية واحدة فقط
                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                    priceText = input
                }
            },
            label = "السعر",
            suffix = "ج.م",
            enabled = !isSaving,
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { if (canSave) onConfirm(name, price ?: 0.0) })
        )
    }
}

// ========== دوال مساعدة ==========

@SuppressLint("DefaultLocale")
private fun formatPrice(price: Double): String {
    return if (price % 1.0 == 0.0) {
        "${price.toInt()} ج.م"
    } else {
        String.format("%.2f ج.م", price)
    }
}

private fun formatPriceNumber(price: Double): String {
    return if (price % 1.0 == 0.0) price.toInt().toString()
    else price.toString()
}
