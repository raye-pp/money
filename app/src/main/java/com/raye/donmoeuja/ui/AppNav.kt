package com.raye.donmoeuja.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.raye.donmoeuja.ui.detail.DetailScreen
import com.raye.donmoeuja.ui.edit.EditScreen
import com.raye.donmoeuja.ui.home.HomeScreen
import com.raye.donmoeuja.ui.settings.SettingsScreen
import com.raye.donmoeuja.ui.stats.StatsScreen
import com.raye.donmoeuja.ui.theme.AccentSoft

object Routes {
    const val HOME = "home"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val EDIT = "edit?entryId={entryId}"
    const val DETAIL = "detail/{entryId}"

    fun edit(entryId: Long? = null) = if (entryId == null) "edit" else "edit?entryId=$entryId"
    fun detail(entryId: Long) = "detail/$entryId"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "홈", Icons.Filled.Home),
    Tab(Routes.STATS, "통계", Icons.AutoMirrored.Filled.List),
    Tab(Routes.SETTINGS, "설정", Icons.Filled.Settings),
)

@Composable
fun DonMoeujaApp(navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = AccentSoft),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(bottom = if (showBottomBar) padding.calculateBottomPadding() else 0.dp),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onAdd = { navController.navigate(Routes.edit()) },
                    onOpenEntry = { navController.navigate(Routes.detail(it)) },
                )
            }
            composable(Routes.STATS) { StatsScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(
                Routes.EDIT,
                arguments = listOf(navArgument("entryId") { type = NavType.LongType; defaultValue = -1L }),
            ) {
                EditScreen(onClose = { navController.popBackStack() })
            }
            composable(
                Routes.DETAIL,
                arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
            ) {
                DetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.edit(it)) },
                )
            }
        }
    }
}
