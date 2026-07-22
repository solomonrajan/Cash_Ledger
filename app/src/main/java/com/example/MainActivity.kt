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
import com.example.ui.ExpenseViewModel
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: ExpenseViewModel = viewModel()
                    
                    NavHost(navController = navController, startDestination = "dashboard") {
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
                                onNavigateToCategories = { navController.navigate("categories") }
                            )
                        }
                    }
                }
            }
        }
    }
}
