package com.example.familyledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.familyledger.ui.budget.BudgetScreen
import com.example.familyledger.ui.capture.CaptureScreen
import com.example.familyledger.ui.detail.DetailScreen
import com.example.familyledger.ui.edit.EditRecordScreen
import com.example.familyledger.ui.home.HomeScreen
import com.example.familyledger.ui.manual.ManualEntryScreen
import com.example.familyledger.ui.product.ProductScreen
import com.example.familyledger.ui.screens.SearchPlaceholder
import com.example.familyledger.ui.screens.SettingsScreen
import com.example.familyledger.ui.theme.FamilyLedgerTheme
import dagger.hilt.android.AndroidEntryPoint

private data class TopLevel(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val topLevelTabs = listOf(
    TopLevel("home", "首页", Icons.Filled.Home),
    TopLevel("search", "搜索", Icons.AutoMirrored.Filled.List),
    TopLevel("settings", "设置", Icons.Filled.Settings)
)

private val topLevelRoutes = topLevelTabs.map { it.route }.toSet()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilyLedgerTheme {
                val navController = rememberNavController()
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route ?: "home"
                val showBottomBar = currentRoute in topLevelRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                topLevelTabs.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentRoute == tab.route,
                                        onClick = {
                                            navController.navigate(tab.route) {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(padding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                onAddClick = { navController.navigate("manual") },
                                onCaptureClick = { navController.navigate("capture") },
                                onRecordClick = { id -> navController.navigate("detail/$id") },
                                onBudgetClick = { navController.navigate("budget") }
                            )
                        }
                        composable("search") { SearchPlaceholder() }
                        composable("settings") {
                            SettingsScreen(onBudgetClick = { navController.navigate("budget") })
                        }
                        composable("budget") {
                            BudgetScreen(onBack = { navController.popBackStack() })
                        }
                        composable("manual") {
                            ManualEntryScreen(
                                onBack = { navController.popBackStack() },
                                onSaved = { navController.popBackStack("home", false) }
                            )
                        }
                        composable("capture") {
                            CaptureScreen(
                                onBack = { navController.popBackStack() },
                                onSaved = { navController.popBackStack("home", false) }
                            )
                        }
                        composable(
                            route = "detail/{recordId}",
                            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
                        ) { entry ->
                            val id = entry.arguments?.getLong("recordId") ?: -1L
                            DetailScreen(
                                onBack = { navController.popBackStack() },
                                onEdit = { rid -> navController.navigate("edit/$rid") },
                                onDeleted = { navController.popBackStack("home", false) },
                                onProduct = { rid -> navController.navigate("product/$rid") }
                            )
                        }
                        composable(
                            route = "edit/{recordId}",
                            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
                        ) {
                            EditRecordScreen(
                                onBack = { navController.popBackStack() },
                                onSaved = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "product/{recordId}",
                            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
                        ) { entry ->
                            val id = entry.arguments?.getLong("recordId") ?: -1L
                            ProductScreen(
                                recordId = id,
                                onBack = { navController.popBackStack() },
                                onRecordClick = { rid -> navController.navigate("detail/$rid") }
                            )
                        }
                    }
                }
            }
        }
    }
}
