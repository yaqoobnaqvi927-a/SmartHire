package com.cs22.example.smarthire

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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.cs22.example.smarthire.network.RetrofitClient
import com.cs22.example.smarthire.ui.auth.SplashScreen
import com.cs22.example.smarthire.ui.auth.AuthScreen
import com.cs22.example.smarthire.ui.auth.RegisterScreen
import com.cs22.example.smarthire.ui.auth.RoleSelectionScreen
import com.cs22.example.smarthire.ui.seeker.SeekerDashboard
import com.cs22.example.smarthire.ui.recruiter.RecruiterDashboard
import com.cs22.example.smarthire.ui.components.ChatScreen
import com.cs22.example.smarthire.ui.seeker.NotificationsScreen
import com.cs22.example.smarthire.ui.auth.SeekerSetupScreen
import com.cs22.example.smarthire.ui.auth.RecruiterSetupScreen
import com.cs22.example.smarthire.ui.theme.SmartHireTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Token Persistence
        RetrofitClient.init(applicationContext)

        setContent {
            val navController = rememberNavController()

            SmartHireTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(onTimeout = {
                                navController.navigate("auth") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            })
                        }
                        composable("auth") {
                            AuthScreen(
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                                navController = navController
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                                navController = navController
                            )
                        }
                        composable("role_selection") {
                            RoleSelectionScreen(
                                onJobSeekerSelected = {
                                    navController.navigate("job_seeker_flow") {
                                        popUpTo("role_selection") { inclusive = true }
                                    }
                                },
                                onRecruiterSelected = {
                                    navController.navigate("recruiter_flow") {
                                        popUpTo("role_selection") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("job_seeker_flow") {
                            SeekerDashboard(
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                                navController = navController
                            )
                        }
                        composable("recruiter_flow") {
                            RecruiterDashboard(
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                                navController = navController
                            )
                        }
                        composable("seeker_setup") {
                            SeekerSetupScreen(navController = navController)
                        }
                        composable("recruiter_setup") {
                            RecruiterSetupScreen(navController = navController)
                        }
                        composable(
                            route = "chat/{applicationId}",
                            arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val appId = backStackEntry.arguments?.getString("applicationId") ?: return@composable
                            ChatScreen(applicationId = appId)
                        }
                        composable("notifications") {
                            NotificationsScreen()
                        }
                    }
                }
            }
        }
    }
}