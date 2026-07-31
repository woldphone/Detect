package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.TrackerTab
import com.example.ui.TrackerViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.IncidentsScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.WhitelistScreen
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SentinelBackground
import com.example.ui.theme.SentinelPurplePrimary
import com.example.ui.theme.SentinelTextMuted

data class NavTabItem(
    val tab: TrackerTab,
    val title: String,
    val icon: ImageVector
)

val NAV_TABS = listOf(
    NavTabItem(TrackerTab.DASHBOARD, "Dashboard", Icons.Default.Radar),
    NavTabItem(TrackerTab.MAP, "Locations Map", Icons.Default.Map),
    NavTabItem(TrackerTab.WHITELIST, "Whitelist", Icons.Default.Block),
    NavTabItem(TrackerTab.INCIDENTS, "Audit Logs", Icons.Default.History)
)

class MainActivity : ComponentActivity() {

    private val viewModel: TrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                MainAppLayout(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: TrackerViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        containerColor = SentinelBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkCardBg,
                modifier = Modifier.testTag("bottom_navigation")
            ) {
                NAV_TABS.forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item.tab,
                        onClick = { viewModel.setTab(item.tab) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SentinelPurplePrimary,
                            selectedTextColor = SentinelPurplePrimary,
                            unselectedIconColor = SentinelTextMuted,
                            unselectedTextColor = SentinelTextMuted,
                            indicatorColor = SentinelBackground
                        ),
                        modifier = Modifier.testTag("tab_${item.tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                TrackerTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                TrackerTab.MAP -> MapScreen(viewModel = viewModel)
                TrackerTab.WHITELIST -> WhitelistScreen(viewModel = viewModel)
                TrackerTab.INCIDENTS -> IncidentsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}
