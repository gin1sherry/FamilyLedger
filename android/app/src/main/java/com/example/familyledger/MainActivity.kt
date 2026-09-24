package com.example.familyledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.familyledger.ui.export.ExportScreen
import com.example.familyledger.ui.home.HomeScreen
import com.example.familyledger.ui.manual.ManualEntryScreen
import com.example.familyledger.ui.product.ProductScreen
import com.example.familyledger.ui.search.SearchScreen
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
private const val ANIM_MS = 220

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
                val isTopLevelNav = currentRoute in topLevelRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                topLevelTabs.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentRoute == tab.route,
                                        onClick = {
                                            if (currentRoute != tab.route) {
                                                navController.navigate(tab.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(padding),
                        enterTransition = {
                            if (isTopLevelNav) {
                                fadeIn(tween(ANIM_MS))
                            } else {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    tween(ANIM_MS)
                                ) + fadeIn(tween(ANIM_MS))
                            }
                        },
                        exitTransition = {
                            if (isTopLevelNav) {
                                fadeOut(tween(ANIM_MS))
                            } else {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    tween(ANIM_MS)
                                ) + fadeOut(tween((ANIM_MS * 0.7).toInt()))
                            }
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                tween(ANIM_MS)
                            ) + fadeIn(tween(ANIM_MS))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                tween(ANIM_MS)
                            ) + fadeOut(tween(ANIM_MS))
                        }
                    ) {
                        composable("home") {
                            HomeScreen(
                                onAddClick = { navController.navigate("manual") },
                                onCaptureClick = { navController.navigate("capture") },
                                onRecordClick = { id -> navController.navigate("detail/$id") },
                                onBudgetClick = { navController.navigate("budget") }
                            )
                        }
                        composable("search") {
                            SearchScreen(onRecordClick = { id -> navController.navigate("detail/$id") })
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBudgetClick = { navController.navigate("budget") },
                                onExportClick = { navController.navigate("export") }
                            )
                        }
                        composable("budget") {
                            BudgetScreen(onBack = { navController.popBackStack() })
                        }
                        composable("export") {
                            ExportScreen(onBack = { navController.popBackStack() })
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
                                onSaved = { navController.popBackStack("home", false) },
                                onOpenSettings = {
                                    navController.navigate("settings") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable(
                            route = "detail/{recordId}",
                            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
                        ) {
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
