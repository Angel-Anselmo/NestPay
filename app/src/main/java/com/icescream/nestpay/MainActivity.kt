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
import com.icescream.nestpay.ui.screens.LoginScreen
import com.icescream.nestpay.ui.screens.NotificationScreen
import com.icescream.nestpay.ui.screens.ProfileScreen
import com.icescream.nestpay.ui.screens.RegisterScreen
import com.icescream.nestpay.ui.screens.WelcomeScreen
import com.icescream.nestpay.ui.screens.WelcomeSuccessScreen
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

    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("welcome") {
            WelcomeScreen(
                onContinue = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }
        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    // Después del login exitoso, va a welcome_success, luego a home
                    navController.navigate("welcome_success") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToHome = {
                    // Después del registro exitoso, va a welcome_success, luego a home
                    navController.navigate("welcome_success") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        // Nueva pantalla de welcome después del login/registro exitoso
        composable("welcome_success") {
            WelcomeSuccessScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("welcome_success") { inclusive = true }
                    }
                }
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