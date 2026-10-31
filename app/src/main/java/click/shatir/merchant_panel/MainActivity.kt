package click.shatir.merchant_panel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import click.shatir.merchant_panel.ui.navigation.AppNavigation
import click.shatir.merchant_panel.ui.theme.ControlPanelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ControlPanelTheme  {
                AppNavigation()
            }
        }
    }
}