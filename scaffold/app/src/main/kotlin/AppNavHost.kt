package {{PACKAGE_NAME}}

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import {{PACKAGE_NAME}}.core.designsystem.theme.AppTheme
import {{PACKAGE_NAME}}.feature.home.api.HomeNavKey
import {{PACKAGE_NAME}}.feature.home.impl.HomeScreen
import {{PACKAGE_NAME}}.feature.settings.impl.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
object SettingsNavKey

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    AppTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute<HomeNavKey>() == true,
                        onClick = {
                            navController.navigate(HomeNavKey) {
                                popUpTo(HomeNavKey) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute<SettingsNavKey>() == true,
                        onClick = {
                            navController.navigate(SettingsNavKey) {
                                popUpTo(HomeNavKey)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                    )
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = HomeNavKey,
                modifier = Modifier.padding(padding),
            ) {
                composable<HomeNavKey> { HomeScreen() }
                composable<SettingsNavKey> { SettingsScreen() }
            }
        }
    }
}
