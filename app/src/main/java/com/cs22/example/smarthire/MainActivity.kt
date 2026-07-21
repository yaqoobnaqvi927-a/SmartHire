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
import com.cs22.example.smarthire.ui.auth.*
import com.cs22.example.smarthire.ui.seeker.SeekerDashboard
import com.cs22.example.smarthire.ui.seeker.JobDetailScreen
import com.cs22.example.smarthire.ui.recruiter.RecruiterDashboard
import com.cs22.example.smarthire.ui.recruiter.CandidateProfileScreen
import com.cs22.example.smarthire.ui.components.ChatScreen
import com.cs22.example.smarthire.ui.components.InterviewScheduleScreen
import com.cs22.example.smarthire.ui.components.SeekerInterviewScreen
import com.cs22.example.smarthire.ui.components.RecruiterInterviewScreen
import com.cs22.example.smarthire.ui.seeker.CvVaultScreen
import com.cs22.example.smarthire.ui.seeker.SkillGapReportScreen
import com.cs22.example.smarthire.ui.seeker.SettingsScreen
import com.cs22.example.smarthire.ui.recruiter.JobPostingWizardScreen
import com.cs22.example.smarthire.ui.recruiter.AnalyticsDashboardScreen
import com.cs22.example.smarthire.ui.components.VideoCallScreen
import com.cs22.example.smarthire.ui.components.NotificationScreen
import com.cs22.example.smarthire.ui.theme.SmartHireTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Token Persistence
        RetrofitClient.init(applicationContext)

        setContent {
            val navController = rememberNavController()
            val authViewModel: com.cs22.example.smarthire.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

            SmartHireTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("onboarding") {
                            OnboardingScreen(navController = navController)
                        }
                        composable("splash") {
                            SplashScreen(
                                viewModel = authViewModel,
                                navController = navController
                            )
                        }
                        composable("role_selection") {
                            RoleSelectionScreen(
                                onJobSeekerSelected = {
                                    navController.navigate("auth/seeker")
                                },
                                onRecruiterSelected = {
                                    navController.navigate("auth/recruiter")
                                }
                            )
                        }
                        composable(
                            route = "auth/{role}",
                            arguments = listOf(navArgument("role") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "seeker"
                            AuthScreen(
                                viewModel = authViewModel,
                                navController = navController,
                                preSelectedRole = role
                            )
                        }
                        // Default auth fallback just in case
                        composable("auth") {
                            AuthScreen(
                                viewModel = authViewModel,
                                navController = navController
                            )
                        }
                        composable(
                            route = "register/{role}",
                            arguments = listOf(navArgument("role") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "student"
                            RegisterScreen(
                                viewModel = authViewModel,
                                navController = navController,
                                preSelectedRole = role
                            )
                        }
                        composable("profile_setup") {
                            DynamicProfileSetupScreen(
                                navController = navController,
                                viewModel = authViewModel
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
                        composable(
                            route = "job_detail/{jobId}",
                            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val jobId = backStackEntry.arguments?.getString("jobId") ?: return@composable
                            JobDetailScreen(
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                                navController = navController,
                                jobId = jobId
                            )
                        }
                        composable(
                            route = "chat/{applicationId}",
                            arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val appId = backStackEntry.arguments?.getString("applicationId") ?: return@composable
                            ChatScreen(applicationId = appId)
                        }
                        composable("notifications") {
                            NotificationScreen(navController = navController)
                        }
                        composable("cv_vault") {
                            CvVaultScreen(viewModel = androidx.lifecycle.viewmodel.compose.viewModel(), navController = navController)
                        }
                        composable(
                            route = "skill_gap/{applicationId}",
                            arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val appId = backStackEntry.arguments?.getString("applicationId") ?: return@composable
                            SkillGapReportScreen(viewModel = androidx.lifecycle.viewmodel.compose.viewModel(), navController = navController, applicationId = appId)
                        }
                        composable("job_wizard") {
                            JobPostingWizardScreen(viewModel = androidx.lifecycle.viewmodel.compose.viewModel(), navController = navController)
                        }
                        composable("analytics") {
                            AnalyticsDashboardScreen(viewModel = androidx.lifecycle.viewmodel.compose.viewModel(), navController = navController)
                        }
                        composable("video_call") {
                            VideoCallScreen(navController = navController)
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = androidx.lifecycle.viewmodel.compose.viewModel(), navController = navController)
                        }
                        composable(
                            route = "candidate_profile/{applicationId}",
                            arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val appId = backStackEntry.arguments?.getString("applicationId") ?: return@composable
                            CandidateProfileScreen(
                                applicationId = appId,
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                                navController = navController
                            )
                        }
                        composable("my_interviews") {
                            SeekerInterviewScreen(
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                                navController = navController
                            )
                        }
                        composable("recruiter_interviews") {
                            RecruiterInterviewScreen(
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}