package com.example.eugene

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.di.*
import com.example.di.viewmodel.*
import com.example.eugene.ui.screen.*
import com.example.eugene.ui.components.EugeneTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

import com.example.eugene.ui.system.DebugMenu
import com.example.eugene.ui.system.SessionExpiryDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    KoinContext {
        EugeneTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                // We display bottom bar and FAB on the 3 top-level main screens
                val isTopLevelRoute = currentRoute in listOf("home", "explore", "profile")

                val coroutineScope = rememberCoroutineScope()
                SessionExpiryDialog(
                    coroutineScope = coroutineScope,
                    onSignOutComplete = {
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
                DebugMenu()

                Scaffold(
                    modifier = Modifier.testTag("app_scaffold"),
                    bottomBar = {
                        if (isTopLevelRoute) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    modifier = Modifier.testTag("nav_item_home")
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "explore",
                                    onClick = {
                                        navController.navigate("explore") {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Explore, contentDescription = "Explore") },
                                    label = { Text("Explore") },
                                    modifier = Modifier.testTag("nav_item_explore")
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "profile",
                                    onClick = {
                                        navController.navigate("profile") {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                    label = { Text("Profile") },
                                    modifier = Modifier.testTag("nav_item_profile")
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (isTopLevelRoute) {
                            FloatingActionButton(
                                onClick = { navController.navigate("create") },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier
                                    .testTag("app_create_fab")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Create Prediction")
                            }
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(
                            route = "home",
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() }
                        ) {
                            val viewModel: HomeFeedViewModel = koinViewModel()
                            HomeFeedScreen(
                                viewModel = viewModel,
                                onPredictionClick = { id -> navController.navigate("prediction/$id") },
                                onProfileClick = { uid ->
                                    if (uid != null) {
                                        navController.navigate("profile/$uid")
                                    } else {
                                        navController.navigate("profile")
                                    }
                                },
                                onNotificationsClick = { navController.navigate("notifications") },
                                onCreatePredictionClick = { navController.navigate("create") }
                            )
                        }

                        composable(
                            route = "explore",
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() }
                        ) {
                            val viewModel: ExploreViewModel = koinViewModel()
                            ExploreScreen(
                                viewModel = viewModel,
                                onPredictionClick = { id -> navController.navigate("prediction/$id") },
                                onProfileClick = { uid -> navController.navigate("profile/$uid") },
                                onLeaderboardClick = { navController.navigate("leaderboard") }
                            )
                        }

                        composable(
                            route = "profile",
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() }
                        ) {
                            val viewModel: ProfileViewModel = koinViewModel()
                            // Bind own profile (set isCurrentUser)
                            viewModel.setTargetUid(null)
                            ProfileScreen(
                                viewModel = viewModel,
                                onPredictionClick = { id -> navController.navigate("prediction/$id") },
                                onSettingsClick = { navController.navigate("settings") }
                            )
                        }

                        composable(
                            route = "profile/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType }),
                            enterTransition = { slideInHorizontally { it } + fadeIn() },
                            exitTransition = { slideOutHorizontally { it } + fadeOut() }
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            val viewModel: ProfileViewModel = koinViewModel()
                            viewModel.setTargetUid(userId)
                            ProfileScreen(
                                viewModel = viewModel,
                                onPredictionClick = { id -> navController.navigate("prediction/$id") },
                                onSettingsClick = { navController.navigate("settings") },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "prediction/{predictionId}",
                            arguments = listOf(navArgument("predictionId") { type = NavType.StringType }),
                            enterTransition = { slideInHorizontally { it } + fadeIn() },
                            exitTransition = { slideOutHorizontally { it } + fadeOut() }
                        ) { backStackEntry ->
                            val predictionId = backStackEntry.arguments?.getString("predictionId") ?: ""
                            val viewModel: PredictionDetailViewModel = koinViewModel()
                            viewModel.setPredictionId(predictionId)
                            PredictionDetailScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onProfileClick = { uid -> navController.navigate("profile/$uid") }
                            )
                        }

                        composable(
                            route = "create",
                            enterTransition = { slideInVertically { it } + fadeIn() },
                            exitTransition = { slideOutVertically { it } + fadeOut() }
                        ) {
                            val viewModel: CreatePredictionViewModel = koinViewModel()
                            CreatePredictionScreen(
                                viewModel = viewModel,
                                onDismiss = { navController.popBackStack() }
                            )
                        }

                        composable("notifications") {
                            val viewModel: NotificationsViewModel = koinViewModel()
                            NotificationsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onPredictionClick = { id -> navController.navigate("prediction/$id") }
                            )
                        }

                        composable("settings") {
                            val viewModel: SettingsViewModel = koinViewModel()
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onSignOut = {
                                    navController.navigate("home") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("leaderboard") {
                            val viewModel: LeaderboardViewModel = koinViewModel()
                            LeaderboardScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onProfileClick = { uid ->
                                    navController.navigate("profile/$uid")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
