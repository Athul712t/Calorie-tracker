package com.example.ui.nav

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.CalorieTrackerViewModel
import com.example.ui.capture.AnalysingScreen
import com.example.ui.capture.CaptureScreen
import com.example.ui.home.HomeScreen
import com.example.ui.meal.MealDetailScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.profile.EditGoalScreen
import com.example.ui.profile.EditProfileScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.AppColors
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

/** Set to [Routes.SCREEN_INDEX] to click through every route while developing. */
private const val START_DESTINATION = Routes.GATE

@Composable
fun AppNavHost(
    viewModel: CalorieTrackerViewModel,
    navController: NavHostController = rememberNavController()
) {
    val back: () -> Unit = { navController.popBackStack() }

    // Follow the Firebase sign-in state so the ViewModel always reads the right user's data.
    var signedIn by remember { mutableStateOf(Firebase.auth.currentUser != null) }
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            signedIn = user != null
            if (user != null) viewModel.initializeUser(user.uid) else viewModel.clearUser()
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    val state by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = START_DESTINATION) {
        // Decides between onboarding and Home once we know who the user is.
        composable(Routes.GATE) {
            LaunchedEffect(signedIn, state.sessionReady, state.profile) {
                val target = when {
                    !signedIn -> Routes.ONBOARDING
                    state.sessionReady && state.profile != null -> Routes.HOME
                    state.sessionReady -> Routes.ONBOARDING
                    else -> null
                }
                if (target != null) {
                    navController.navigate(target) { popUpTo(Routes.GATE) { inclusive = true } }
                }
            }
            Box(Modifier.fillMaxSize().background(AppColors.Background), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Green)
            }
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(viewModel = viewModel, onFinished = {
                navController.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
            })
        }

        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onOpenProgress = { navController.navigate(Routes.PROGRESS) },
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenCapture = { navController.navigate(Routes.CAPTURE) },
                onOpenMeal = { id ->
                    if (viewModel.openSavedMeal(id)) navController.navigate(Routes.mealDetail(id))
                },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenAddFood = { navController.navigate(Routes.ADD_FOOD) },
                onSubmitText = { text ->
                    viewModel.analyzeMealInput(text, null, "")
                    navController.navigate(Routes.ANALYSING)
                }
            )
        }

        composable(Routes.CAPTURE) {
            CaptureScreen(onBack = back, onPhoto = { bmp: Bitmap ->
                viewModel.analyzeMealInput("", bmp, "")
                navController.navigate(Routes.ANALYSING) { popUpTo(Routes.CAPTURE) { inclusive = true } }
            })
        }

        composable(Routes.ANALYSING) {
            AnalysingScreen(
                viewModel = viewModel,
                onBack = back,
                onReady = {
                    navController.navigate(Routes.mealDetail("new")) { popUpTo(Routes.ANALYSING) { inclusive = true } }
                },
                onSearchFood = { navController.navigate(Routes.ADD_FOOD) }
            )
        }

        composable(Routes.MEAL_DETAIL) {
            MealDetailScreen(
                viewModel = viewModel,
                onClose = {
                    if (navController.currentDestination?.route == Routes.MEAL_DETAIL) navController.popBackStack()
                },
                onDone = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                viewModel = viewModel,
                onBack = back,
                onEditProfile = { navController.navigate(Routes.PROFILE_EDIT) },
                onEditGoal = { navController.navigate(Routes.GOAL_EDIT) },
                onLoggedOut = {
                    navController.navigate(Routes.ONBOARDING) { popUpTo(0) }
                }
            )
        }
        composable(Routes.PROFILE_EDIT) { EditProfileScreen(viewModel = viewModel, onBack = back) }
        composable(Routes.GOAL_EDIT) {
            EditGoalScreen(
                viewModel = viewModel,
                onBack = back,
                onEditProfile = { navController.navigate(Routes.PROFILE_EDIT) }
            )
        }

        composable(Routes.SCREEN_INDEX) {
            ScreenIndex(onOpen = { route -> navController.navigate(route) })
        }

        // Screens that are designed but not built yet.
        listOf(
            Routes.PROGRESS, Routes.PHOTOS, Routes.ADD_FOOD, Routes.HISTORY
        ).forEach { route ->
            val info = ALL_SCREENS.first { it.route == route }
            composable(route) { PlaceholderScreen(info, back) }
        }
    }
}
