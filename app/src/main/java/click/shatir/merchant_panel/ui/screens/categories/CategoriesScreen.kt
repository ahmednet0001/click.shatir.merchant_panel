package click.shatir.merchant_panel.ui.screens.categories

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import click.shatir.merchant_panel.R
import click.shatir.merchant_panel.model.category.Category
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

/** عدد العناصر الذي يظهر عنده حقل البحث (تحته القائمة قصيرة ولا تحتاج بحثاً). */
private const val SEARCH_THRESHOLD = 6

@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarMsg by viewModel.snackbar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var editing by remember { mutableStateOf<Category?>(null) }
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
        title = "التصنيفات",
        snackbarHostState = snackbarHostState,
        fabLabel = "إضافة تصنيف",
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

                state.categories.isEmpty() -> EmptyState(
                    icon = R.drawable.category_24px,
                    title = "لا توجد تصنيفات بعد",
                    message = "نظّم منتجاتك في تصنيفات ليسهل على عملائك التصفح.",
                    actionLabel = "إضافة أول تصنيف",
                    onAction = openAdd
                )

                else -> {
                    val filtered = remember(state.categories, query) {
                        val q = query.trim()
                        if (q.isEmpty()) state.categories
                        else state.categories.filter { it.name.contains(q, ignoreCase = true) }
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (state.categories.size >= SEARCH_THRESHOLD) {
                            AppSearchField(
                                value = query,
                                onValueChange = { query = it },
                                placeholder = "ابحث في التصنيفات",
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
                            )
                        }
                        if (filtered.isEmpty()) {
                            EmptyState(
                                icon = R.drawable.ic_search,
                                title = "لا توجد نتائج",
                                message = "لا يوجد تصنيف يطابق \"${query.trim()}\""
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
                                item { ListCountLabel("${filtered.size} تصنيف") }
                                items(filtered, key = { it.id }) { category ->
                                    CategoryRow(
                                        category = category,
                                        onEdit = {
                                            editing = category
                                            showDialog = true
                                        },
                                        onDelete = { viewModel.delete(category.id) }
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
        CategoryDialog(
            initial = editing,
            isSaving = state.isSaving,
            onDismiss = { showDialog = false },
            onConfirm = { name ->
                if (editing == null) {
                    viewModel.create(name) { showDialog = false }
                } else {
                    viewModel.update(editing!!.id, name) { showDialog = false }
                }
            }
        )
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }

    AppCard(contentPadding = PaddingValues(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = R.drawable.category_24px)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    category.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = BrandNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val count = category.productsCount ?: 0
                Text(
                    "$count منتج",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
            RowActions(onEdit = onEdit, onDelete = { confirmDelete = true })
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            itemName = category.name,
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun CategoryDialog(
    initial: Category?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    val canSave = !isSaving && name.isNotBlank()

    FormDialog(
        title = if (initial == null) "إضافة تصنيف" else "تعديل تصنيف",
        isSaving = isSaving,
        canSave = canSave,
        onDismiss = onDismiss,
        onSave = { onConfirm(name) }
    ) {
        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = "اسم التصنيف",
            enabled = !isSaving,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { if (canSave) onConfirm(name) }),
            modifier = Modifier.autoFocus(enabled = initial == null)
        )
    }
}
