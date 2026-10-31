package click.shatir.merchant_panel.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import click.shatir.merchant_panel.R
import click.shatir.merchant_panel.ui.components.AppColors
import click.shatir.merchant_panel.ui.components.AppFilterChip
import click.shatir.merchant_panel.ui.components.AppTextField
import click.shatir.merchant_panel.ui.components.ErrorState
import click.shatir.merchant_panel.ui.components.PickerField
import click.shatir.merchant_panel.ui.components.PrimaryButton
import click.shatir.merchant_panel.ui.components.ScreenScaffold
import click.shatir.merchant_panel.ui.components.SecondaryButton
import click.shatir.merchant_panel.ui.components.SectionCard
import click.shatir.merchant_panel.ui.components.SkeletonList
import click.shatir.merchant_panel.ui.components.SwitchRow
import click.shatir.merchant_panel.ui.theme.BrandNavy
import click.shatir.merchant_panel.ui.theme.BrandOrangeDark
import click.shatir.merchant_panel.ui.theme.BrandOrangeLight
import click.shatir.merchant_panel.util.Constants
import coil3.compose.AsyncImage

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarMsg by viewModel.snackbar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarShown()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.pickLogo(it) } }

    ScreenScaffold(
        title = "إعدادات المتجر",
        snackbarHostState = snackbarHostState,
        bottomBar = {
            if (!state.isLoading && state.error == null) {
                SaveBar(
                    dirty = state.dirty,
                    isSaving = state.isSaving,
                    onSave = { viewModel.save() }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> SkeletonList(count = 4, itemHeight = 120.dp)

                state.error != null -> ErrorState(
                    message = state.error!!,
                    onRetry = { viewModel.load() }
                )

                else -> {
                    SettingsContent(
                        state = state,
                        viewModel = viewModel,
                        onPickLogo = { logoPicker.launch("image/*") }
                    )
                }
            }
        }
    }
}

/** شريط الحفظ الثابت أسفل الشاشة، ويُنبّه عند وجود تعديلات غير محفوظة. */
@Composable
private fun SaveBar(dirty: Boolean, isSaving: Boolean, onSave: () -> Unit) {
    Surface(shadowElevation = 8.dp, color = Color.White) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            if (dirty) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(BrandOrangeLight)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "لديك تعديلات غير محفوظة",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            PrimaryButton(
                text = "حفظ الإعدادات",
                onClick = onSave,
                loading = isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    onPickLogo: () -> Unit
) {
    val form = state.form
    val enabled = !state.isSaving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ----- الشعار -----
        SectionCard(
            title = "شعار المتجر",
            icon = R.drawable.store_24px
        ) {
            val model: Any? = when {
                state.newLogoUri != null -> state.newLogoUri
                state.removeLogo -> null
                state.logoPath != null -> logoUrl(state.logoPath)
                else -> null
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFEFF2F7)),
                    contentAlignment = Alignment.Center
                ) {
                    if (model != null) {
                        AsyncImage(
                            model = model,
                            contentDescription = "الشعار",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_image),
                            contentDescription = null,
                            tint = AppColors.Hint,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    SecondaryButton(
                        text = if (model == null) "اختيار صورة" else "تغيير الصورة",
                        icon = R.drawable.ic_image,
                        onClick = onPickLogo,
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (model != null) {
                        TextButton(onClick = { viewModel.removeLogo() }, enabled = enabled) {
                            Text("حذف الشعار", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            if (state.newLogoUri != null || state.removeLogo) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "سيتم تطبيق تغيير الشعار عند الضغط على حفظ",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }

        // ----- بيانات المتجر -----
        SectionCard(
            title = "بيانات المتجر",
            icon = R.drawable.ic_edit
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsField(
                    value = form.subtitle,
                    label = "الجملة أسفل العنوان",
                    placeholder = "أهلاً بكم في متجرنا",
                    maxLength = 255,
                    enabled = enabled,
                    onChange = { v -> viewModel.edit { it.copy(subtitle = v) } }
                )
                SettingsField(
                    value = form.description,
                    label = "وصف المتجر",
                    maxLength = 2000,
                    minLines = 4,
                    singleLine = false,
                    enabled = enabled,
                    onChange = { v -> viewModel.edit { it.copy(description = v) } }
                )
            }
        }

        // ----- التواصل -----
        SectionCard(
            title = "بيانات التواصل",
            icon = R.drawable.ic_phone
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsField(
                    value = form.whatsappNumber,
                    label = "رقم الواتساب",
                    placeholder = "201xxxxxxxxx",
                    supporting = "اكتب الرقم بصيغة دولية بدون +",
                    keyboardType = KeyboardType.Phone,
                    maxLength = 20,
                    enabled = enabled,
                    onChange = { v -> viewModel.edit { it.copy(whatsappNumber = v) } }
                )
                SettingsField(
                    value = form.contactPhone,
                    label = "رقم اتصل بنا",
                    placeholder = "01xxxxxxxxx",
                    keyboardType = KeyboardType.Phone,
                    maxLength = 20,
                    enabled = enabled,
                    onChange = { v -> viewModel.edit { it.copy(contactPhone = v) } }
                )
            }
        }

        // ----- طرق الاستلام -----
        SectionCard(
            title = "طرق استلام الطلب",
            subtitle = "اختر طريقة واحدة على الأقل ليتمكن العملاء من استلام طلباتهم.",
            icon = R.drawable.local_shipping_24px
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FULFILLMENT_METHODS.forEach { (key, label) ->
                    AppFilterChip(
                        label = label,
                        selected = key in form.methods,
                        onClick = { viewModel.toggleMethod(key) },
                        enabled = enabled,
                        selectedColor = BrandOrangeDark,
                        showCheck = true
                    )
                }
            }
            if (form.methods.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "يجب اختيار طريقة استلام واحدة على الأقل",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // ----- أوقات العمل -----
        SectionCard(
            title = "أوقات العمل",
            subtitle = "حدد أيام العمل وساعات الفتح والإغلاق. أوقف المفتاح بجوار أي يوم إجازة.",
            icon = R.drawable.ic_schedule
        ) {
            val first = form.workingHours.firstOrNull()
            if (first != null && form.workingHours.size > 1) {
                TextButton(
                    onClick = {
                        // نسخ أوقات أول يوم لباقي الأيام بدل ضبط 7 أيام يدوياً
                        for (i in 1 until form.workingHours.size) {
                            viewModel.updateDay(i) {
                                it.copy(isOpen = first.isOpen, from = first.from, to = first.to)
                            }
                        }
                    },
                    enabled = enabled
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("نسخ أوقات ${first.day} لباقي الأيام")
                }
            }
            form.workingHours.forEachIndexed { index, day ->
                WorkingDayRow(
                    day = day,
                    enabled = enabled,
                    onChange = { transform -> viewModel.updateDay(index, transform) }
                )
                if (index < form.workingHours.lastIndex) {
                    HorizontalDivider(color = AppColors.Border)
                }
            }
        }

        // ----- تسمية الأحجام -----
        SectionCard(
            title = "تسمية الأحجام",
            subtitle = "الاسم الذي يظهر لعملائك بدل \"حجم\"، مثل: نوع / أنواع، أو مقاس / مقاسات.",
            icon = R.drawable.ic_edit
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsField(
                    value = form.variantLabelSingular,
                    label = "الاسم بالمفرد",
                    placeholder = "حجم",
                    maxLength = 255,
                    enabled = enabled,
                    onChange = { v -> viewModel.edit { it.copy(variantLabelSingular = v) } }
                )
                SettingsField(
                    value = form.variantLabelPlural,
                    label = "الاسم بالجمع",
                    placeholder = "أحجام",
                    maxLength = 255,
                    enabled = enabled,
                    onChange = { v -> viewModel.edit { it.copy(variantLabelPlural = v) } }
                )
            }
        }

        // ----- رسالة الواتساب -----
        SectionCard(
            title = "رسالة طلب الواتساب",
            icon = R.drawable.ic_message
        ) {
            SettingsField(
                value = form.welcomeMessage,
                label = "جملة الترحيب",
                placeholder = "مرحبًا، شكرًا لطلبك من متجرنا 🌷",
                supporting = "تظهر تلقائيًا عند تجهيز الطلب للإرسال عبر واتساب",
                maxLength = 500,
                minLines = 3,
                singleLine = false,
                enabled = enabled,
                onChange = { v -> viewModel.edit { it.copy(welcomeMessage = v) } }
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ========== صف يوم العمل ==========

@Composable
private fun WorkingDayRow(
    day: WorkingDayForm,
    enabled: Boolean,
    onChange: ((WorkingDayForm) -> WorkingDayForm) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        SwitchRow(
            label = day.day,
            description = if (day.isOpen) "مفتوح" else "إجازة",
            descriptionColor = if (day.isOpen) AppColors.Success else AppColors.TextSecondary,
            labelColor = BrandNavy,
            checked = day.isOpen,
            enabled = enabled,
            onToggle = { onChange { it.copy(isOpen = !day.isOpen) } }
        )
        if (day.isOpen) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeButton(
                    label = "يفتح من",
                    value = day.from,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    onChange = { v -> onChange { it.copy(from = v) } }
                )
                TimeButton(
                    label = "يغلق عند",
                    value = day.to,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    onChange = { v -> onChange { it.copy(to = v) } }
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeButton(
    label: String,
    value: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    var show by remember { mutableStateOf(false) }

    PickerField(
        label = label,
        value = value,
        onClick = { show = true },
        enabled = enabled,
        icon = R.drawable.ic_schedule,
        modifier = modifier
    )

    if (show) {
        val parts = value.split(":")
        val timeState = rememberTimePickerState(
            initialHour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9,
            initialMinute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { show = false },
            title = { Text(label) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    onChange("%02d:%02d".format(timeState.hour, timeState.minute))
                    show = false
                }) { Text("تم") }
            },
            dismissButton = {
                TextButton(onClick = { show = false }) { Text("إلغاء") }
            }
        )
    }
}

// ========== عناصر مساعدة ==========

@Composable
private fun SettingsField(
    value: String,
    label: String,
    onChange: (String) -> Unit,
    enabled: Boolean,
    maxLength: Int,
    placeholder: String? = null,
    supporting: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    AppTextField(
        value = value,
        onValueChange = { if (it.length <= maxLength) onChange(it) },
        label = label,
        placeholder = placeholder,
        // العدّاد يظهر للحقول الطويلة فقط
        supportingText = supporting ?: if (!singleLine) "${value.length}/$maxLength" else null,
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        keyboardType = keyboardType
    )
}

private fun logoUrl(path: String): String =
    if (path.startsWith("http")) path else Constants.STORAGE_URL + path.trimStart('/')
