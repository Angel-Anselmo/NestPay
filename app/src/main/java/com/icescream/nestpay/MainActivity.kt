package com.icescream.nestpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.icescream.nestpay.ui.screens.ActivityScreen
import com.icescream.nestpay.ui.screens.CreateCommunityScreen
import com.icescream.nestpay.ui.screens.HomeScreen
import com.icescream.nestpay.ui.screens.NotificationScreen
import com.icescream.nestpay.ui.screens.ProfileScreen
import com.icescream.nestpay.ui.screens.WelcomeScreen
import com.icescream.nestpay.ui.theme.NestPayTheme
import com.icescream.nestpay.ui.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NestPayTheme {
                NestPayApp()
            }
        }
    }
}

@Composable
fun NestPayApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "welcome",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("welcome") {
                WelcomeScreen(
                    onContinue = {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    },
                    authViewModel = authViewModel
                )
            }
            composable("home") {
                HomeScreen(
                    onNavigateToActivity = {
                        navController.navigate("activity")
                    },
                    onNavigateToNotifications = {
                        navController.navigate("notifications")
                    },
                    onNavigateToProfile = {
                        navController.navigate("profile")
                    },
                    onCreateCommunity = {
                        navController.navigate("create_community")
                    },
                    authViewModel = authViewModel
                )
            }
            composable("activity") {
                ActivityScreen(
                    onNavigateBack = {
                        navController.navigate("home")
                    },
                    onNavigateToNotifications = {
                        navController.navigate("notifications")
                    },
                    onNavigateToProfile = {
                        navController.navigate("profile")
                    }
                )
            }
            composable("notifications") {
                NotificationScreen(
                    onNavigateToHome = {
                        navController.navigate("home")
                    },
                    onNavigateToActivity = {
                        navController.navigate("activity")
                    },
                    onNavigateToProfile = {
                        navController.navigate("profile")
                    }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onNavigateToHome = {
                        navController.navigate("home")
                    },
                    onNavigateToActivity = {
                        navController.navigate("activity")
                    },
                    onNavigateToNotifications = {
                        navController.navigate("notifications")
                    }
                )
            }
            composable("create_community") {
                CreateCommunityScreen(
                    onNavigateBack = {
                        navController.navigate("home")
                    }
                )
            }
        }
    }
}