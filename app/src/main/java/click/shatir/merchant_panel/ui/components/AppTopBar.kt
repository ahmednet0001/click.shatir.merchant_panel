package click.shatir.merchant_panel.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import click.shatir.merchant_panel.R
import click.shatir.merchant_panel.ui.theme.BrandNavy

/**
 * إجراءات الجلسة المشتركة بين كل الشاشات (تغيير المتجر / تسجيل الخروج).
 * تُوفَّر من AppNavigation عبر CompositionLocal.
 */
class SessionActions(
    val onSwitchStore: () -> Unit,
    val onLogout: () -> Unit,
    val isLoggingOut: Boolean = false
)

val LocalSessionActions = staticCompositionLocalOf { SessionActions({}, {}) }

/** الشريط العلوي الموحّد لكل الصفحات. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String) {
    val session = LocalSessionActions.current
    var menuOpen by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vert_24px),
                        contentDescription = "خيارات",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("تغيير المتجر") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_swap),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            menuOpen = false
                            session.onSwitchStore()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("تسجيل الخروج", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_logout),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            menuOpen = false
                            confirmLogout = true
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandNavy)
    )

    if (confirmLogout) {
        LogoutConfirmDialog(
            loggingOut = session.isLoggingOut,
            onConfirm = { session.onLogout() },
            onDismiss = { confirmLogout = false }
        )
    }
}

/** زر تسجيل الخروج المستقل (يُستخدم في شاشة اختيار المتجر التي ليس لها شريط علوي). */
@Composable
fun LogoutTextButton(modifier: Modifier = Modifier, color: Color = Color.White) {
    val session = LocalSessionActions.current
    var confirm by remember { mutableStateOf(false) }

    TextButton(onClick = { confirm = true }, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_logout),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text("تسجيل الخروج", color = color)
    }

    if (confirm) {
        LogoutConfirmDialog(
            loggingOut = session.isLoggingOut,
            onConfirm = { session.onLogout() },
            onDismiss = { confirm = false }
        )
    }
}

@Composable
private fun LogoutConfirmDialog(
    loggingOut: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!loggingOut) onDismiss() },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        icon = {
            IconBadge(
                icon = R.drawable.ic_logout,
                tint = AppColors.Danger,
                size = 56.dp,
                iconSize = 28.dp,
                circle = true
            )
        },
        title = {
            Text(
                "تسجيل الخروج",
                color = AppColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                "هل تريد تسجيل الخروج من حسابك؟",
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !loggingOut,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Danger,
                    contentColor = Color.White,
                    disabledContainerColor = AppColors.Danger.copy(alpha = 0.6f),
                    disabledContentColor = Color.White
                )
            ) {
                if (loggingOut) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("جارٍ الخروج...")
                    }
                } else {
                    Text("خروج")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !loggingOut) {
                Text("إلغاء", color = BrandNavy)
            }
        }
    )
}
