package com.skinlens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skinlens.ui.HomeScreen
import com.skinlens.ui.PrivacyScreen
import com.skinlens.ui.AnalysisScreen
import com.skinlens.ui.ResultScreen
import com.skinlens.ui.ProgressScreen
import com.skinlens.ui.IngredientsScreen
import com.skinlens.ui.theme.SkinLensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkinLensTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(navController = navController)
                        }
                        composable("privacy") {
                            PrivacyScreen(navController = navController)
                        }
                        composable("analysis") {
                            AnalysisScreen(navController = navController)
                        }
                        composable("result/{photoUri}") { backStackEntry ->
                            val photoUri = backStackEntry.arguments?.getString("photoUri") ?: ""
                            ResultScreen(navController = navController, photoUri = photoUri)
                        }
                        composable("progress") {
                            ProgressScreen(navController = navController)
                        }
                        composable("ingredients") {
                            IngredientsScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
