package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.example.data.local.UserPreferencesManager
import com.example.ui.ExpenseViewModel
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InitialSetupScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefsManager = remember(context) { UserPreferencesManager(context) }
            val themeMode by prefsManager.themeModeFlow.collectAsState()
            val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemInDark
            }

            AppTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: ExpenseViewModel = viewModel()

                    val startDestination = if (prefsManager.isSetupCompleted) "dashboard" else "setup"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("setup") {
                            InitialSetupScreen(
                                viewModel = viewModel,
                                onSetupComplete = {
                                    navController.navigate("dashboard") {
                                        popUpTo("setup") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToAdd = { navController.navigate("add") },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToCategories = { navController.navigate("categories") }
                            )
                        }
                        composable("add") {
                            AddTransactionScreen(
                                viewModel = viewModel,
                                onBack = { navController.navigateUp() }
                            )
                        }
                        composable("categories") {
                            CategoriesScreen(
                                viewModel = viewModel,
                                onBack = { navController.navigateUp() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.navigateUp() },
                                onNavigateToCategories = { navController.navigate("categories") },
                                onReRunSetup = {
                                    prefsManager.isSetupCompleted = false
                                    navController.navigate("setup") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
