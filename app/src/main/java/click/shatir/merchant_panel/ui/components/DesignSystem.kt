package click.shatir.merchant_panel.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import click.shatir.merchant_panel.R
import click.shatir.merchant_panel.ui.theme.BrandNavy
import click.shatir.merchant_panel.ui.theme.BrandOrangeDark
import click.shatir.merchant_panel.ui.theme.BrandOrangeLight

/**
 * ألوان دلالية موحّدة. القيم نفسها التي كانت مكتوبة داخل الشاشات (لم تتغير ألوان الهوية).
 */
object AppColors {
    val ScreenBackground = Color(0xFFF5F7FA)
    val TextPrimary = Color(0xFF1B1B1B)
    val TextSecondary = Color(0xFF6B7280)
    val Hint = Color(0xFFB0B7C3)
    val Border = Color(0xFFE6EAF0)
    val Track = Color(0xFFE0E0E0)
    val SwitchOff = Color(0xFFBDBDBD)
    val Success = Color(0xFF2E7D32)
    val SuccessOnDark = Color(0xFF66BB6A)
    val Danger = Color(0xFFD32F2F)
    val Info = Color(0xFF1976D2)
    val Muted = Color(0xFF757575)
}

// ======================================================================
// هيكل الشاشة
// ======================================================================

/** هيكل موحّد: شريط علوي + Snackbar بهوية التطبيق + زر إضافة عائم موسّع. */
@Composable
fun ScreenScaffold(
    title: String,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    fabLabel: String? = null,
    onFabClick: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title) },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = BrandNavy,
                    contentColor = Color.White,
                    actionColor = BrandOrangeLight
                )
            }
        },
        floatingActionButton = {
            if (fabLabel != null) AppExtendedFab(label = fabLabel, onClick = onFabClick)
        },
        bottomBar = bottomBar,
        containerColor = AppColors.ScreenBackground,
        content = content
    )
}

@Composable
fun AppExtendedFab(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        containerColor = BrandOrangeDark,
        contentColor = Color.White,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        },
        text = { Text(label, style = MaterialTheme.typography.labelLarge) }
    )
}

// ======================================================================
// البطاقات والعناصر البصرية
// ======================================================================

/** بطاقة بيضاء بحد خفيف. [accent] يرسم شريطاً ملوّناً على الحافة الأمامية. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accent: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val border = BorderStroke(1.dp, AppColors.Border)
    val inner: @Composable () -> Unit = {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            if (accent != null) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(accent)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(contentPadding),
                content = content
            )
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = Color.White,
            border = border
        ) { inner() }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = Color.White,
            border = border
        ) { inner() }
    }
}

/** بطاقة بعنوان وأيقونة اختيارية (تُستخدم في الرئيسية والإعدادات). */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                IconBadge(icon = icon, size = 36.dp, iconSize = 20.dp)
                Spacer(Modifier.width(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
fun IconBadge(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    tint: Color = BrandNavy,
    background: Color = tint.copy(alpha = 0.10f),
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    circle: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(if (circle) CircleShape else RoundedCornerShape(12.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun StatusChip(label: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = label,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 2.dp)
    )
}

@Composable
fun appSwitchColors(): SwitchColors = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = BrandOrangeDark,
    checkedBorderColor = Color.Transparent,
    uncheckedThumbColor = Color.White,
    uncheckedTrackColor = AppColors.SwitchOff,
    uncheckedBorderColor = Color.Transparent
)

/** صف مفتاح تبديل: كامل الصف قابل للضغط (هدف لمس أكبر). */
@Composable
fun SwitchRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
    labelColor: Color = AppColors.TextPrimary,
    descriptionColor: Color = AppColors.TextSecondary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                enabled = enabled && !loading,
                role = Role.Switch,
                onValueChange = { onToggle() }
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = labelColor)
            if (description != null) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = descriptionColor
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = BrandOrangeDark
            )
        } else {
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                colors = appSwitchColors()
            )
        }
    }
}

// ======================================================================
// الأزرار
// ======================================================================

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    @DrawableRes icon: Int? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandOrangeDark,
            contentColor = Color.White,
            disabledContainerColor = if (loading) BrandOrangeDark else BrandOrangeDark.copy(alpha = 0.4f),
            disabledContentColor = Color.White
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            if (icon != null) {
                Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    contentColor: Color = BrandNavy
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, contentColor.copy(alpha = if (enabled) 0.35f else 0.15f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** أزرار التعديل والحذف على شكل أيقونات (بدل نصوص "تعديل/حذف" الطويلة). */
@Composable
fun RowActions(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    vertical: Boolean = false
) {
    val buttons: @Composable () -> Unit = {
        IconButton(onClick = onEdit, enabled = enabled, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = "تعديل",
                tint = BrandNavy,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onDelete, enabled = enabled, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = "حذف",
                tint = AppColors.Danger,
                modifier = Modifier.size(20.dp)
            )
        }
    }
    if (vertical) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) { buttons() }
    } else {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) { buttons() }
    }
}

/** شريحة تصفية/اختيار بهوية التطبيق. */
@Composable
fun AppFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedColor: Color = BrandNavy,
    showCheck: Boolean = false
) {
    val colors = FilterChipDefaults.filterChipColors(
        containerColor = Color.White,
        labelColor = AppColors.TextPrimary,
        selectedContainerColor = selectedColor,
        selectedLabelColor = Color.White,
        selectedLeadingIconColor = Color.White
    )
    if (showCheck && selected) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            shape = RoundedCornerShape(50),
            colors = colors
        )
    } else {
        FilterChip(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            shape = RoundedCornerShape(50),
            colors = colors
        )
    }
}

// ======================================================================
// حقول الإدخال
// ======================================================================

/** يضع المؤشر تلقائياً في الحقل عند فتح النافذة (يوفّر ضغطة على المستخدم). */
@Composable
fun Modifier.autoFocus(enabled: Boolean = true): Modifier {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(enabled) {
        if (enabled) runCatching { focusRequester.requestFocus() }
    }
    return this.focusRequester(focusRequester)
}

@Composable
private fun appFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    focusedBorderColor = BrandNavy,
    unfocusedBorderColor = Color(0xFFCBD2DC),
    focusedLabelColor = BrandNavy,
    focusedTextColor = AppColors.TextPrimary,
    unfocusedTextColor = AppColors.TextPrimary,
    disabledTextColor = AppColors.TextSecondary,
    cursorColor = BrandNavy
)

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    placeholder: String? = null,
    supportingText: String? = null,
    suffix: String? = null,
    isError: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = readOnly,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = AppColors.Hint) } },
        supportingText = supportingText?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } },
        trailingIcon = trailingIcon,
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(14.dp),
        colors = appFieldColors()
    )
}

@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = AppColors.Hint) },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = AppColors.TextSecondary
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "مسح البحث",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = appFieldColors()
    )
}

/** حقل اختيار قابل للضغط (قائمة، تاريخ، وقت) بنفس مظهر حقول النص. */
@Composable
fun PickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    isPlaceholder: Boolean = false,
    showArrow: Boolean = false
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFCBD2DC))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = BrandNavy,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPlaceholder) AppColors.Hint else AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            if (showArrow) {
                Icon(
                    painter = painterResource(R.drawable.ic_expand_more),
                    contentDescription = null,
                    tint = BrandNavy
                )
            }
        }
    }
}

// ======================================================================
// حالات الشاشة: تحميل / خطأ / فارغ
// ======================================================================

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x = transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1300, easing = LinearEasing)),
        label = "shimmerX"
    )
    return Brush.linearGradient(
        colors = listOf(Color(0xFFE9EDF2), Color(0xFFF7F9FB), Color(0xFFE9EDF2)),
        start = Offset(x.value, 0f),
        end = Offset(x.value + 400f, 0f)
    )
}

/** هيكل تحميل (Skeleton) بدل الدائرة الدوّارة: يعطي إحساساً أسرع ويُظهر شكل المحتوى القادم. */
@Composable
fun SkeletonList(
    modifier: Modifier = Modifier,
    count: Int = 6,
    itemHeight: Dp = 76.dp
) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconBadge(
            icon = R.drawable.ic_error,
            tint = AppColors.Danger,
            size = 72.dp,
            iconSize = 36.dp,
            circle = true
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "تعذّر تحميل البيانات",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = "إعادة المحاولة",
            onClick = onRetry,
            modifier = Modifier.width(200.dp)
        )
    }
}

@Composable
fun EmptyState(
    @DrawableRes icon: Int,
    title: String,
    message: String? = null,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconBadge(
            icon = icon,
            tint = BrandNavy,
            size = 84.dp,
            iconSize = 40.dp,
            circle = true
        )
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        if (message != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = actionLabel,
                onClick = onAction,
                icon = R.drawable.ic_add,
                modifier = Modifier.width(220.dp)
            )
        }
    }
}

/** سطر عدّاد صغير أعلى القوائم، مثل: "12 منتج". */
@Composable
fun ListCountLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = AppColors.TextSecondary,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

// ======================================================================
// النوافذ
// ======================================================================

/**
 * نافذة نموذج موحّدة (إضافة/تعديل): عنوان + محتوى قابل للتمرير + أزرار ثابتة بالأسفل.
 * [errorText] يظهر فوق الأزرار مباشرة ليكون مرئياً دائماً حتى لو كان المحتوى طويلاً.
 */
@Composable
fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
    canSave: Boolean = true,
    saveLabel: String = "حفظ",
    fullHeight: Boolean = false,
    errorText: String? = null,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit
) {
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.9f

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .then(
                    if (fullHeight) Modifier.fillMaxHeight(0.9f)
                    else Modifier.heightIn(max = maxHeight)
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, enabled = !isSaving) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "إغلاق",
                            tint = AppColors.TextSecondary
                        )
                    }
                }
                HorizontalDivider(color = AppColors.Border)

                Column(
                    modifier = Modifier
                        .weight(1f, fill = fullHeight)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = content
                )

                HorizontalDivider(color = AppColors.Border)
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    if (errorText != null) {
                        Text(
                            text = errorText,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton(
                            text = "إلغاء",
                            onClick = onDismiss,
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = saveLabel,
                            onClick = onSave,
                            enabled = canSave,
                            loading = isSaving,
                            modifier = Modifier.weight(1.4f)
                        )
                    }
                }
            }
        }
    }
}

/** نافذة تأكيد الحذف الموحّدة. */
@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "تأكيد الحذف"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        icon = {
            IconBadge(
                icon = R.drawable.ic_delete,
                tint = AppColors.Danger,
                size = 56.dp,
                iconSize = 28.dp,
                circle = true
            )
        },
        title = {
            Text(
                title,
                color = AppColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                "هل أنت متأكد من حذف \"$itemName\"؟ لا يمكن التراجع عن هذا الإجراء.",
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Danger,
                    contentColor = Color.White
                )
            ) { Text("حذف") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = BrandNavy) }
        }
    )
}
