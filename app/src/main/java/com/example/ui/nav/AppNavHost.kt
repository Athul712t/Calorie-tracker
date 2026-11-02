package com.example.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.CalorieTrackerScreen
import com.example.ui.CalorieTrackerViewModel

/** Change this to [Routes.SCREEN_INDEX] to click through every route while developing. */
private const val START_DESTINATION = Routes.HOME

@Composable
fun AppNavHost(
    viewModel: CalorieTrackerViewModel,
    navController: NavHostController = rememberNavController()
) {
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = START_DESTINATION) {
        // TEMPORARY: the old single-screen app. Replaced by the new Home in the next step.
        composable(Routes.HOME) { CalorieTrackerScreen(viewModel = viewModel) }

        composable(Routes.SCREEN_INDEX) {
            ScreenIndex(onOpen = { route -> navController.navigate(route) })
        }

        // Screens that are designed but not built yet.
        listOf(
            Routes.ONBOARDING, Routes.PROGRESS, Routes.PHOTOS, Routes.PROFILE, Routes.PROFILE_EDIT,
            Routes.GOAL_EDIT, Routes.CAPTURE, Routes.ANALYSING, Routes.ADD_FOOD, Routes.HISTORY
        ).forEach { route ->
            val info = ALL_SCREENS.first { it.route == route }
            composable(route) { PlaceholderScreen(info, back) }
        }
        composable(Routes.MEAL_DETAIL) {
            PlaceholderScreen(ALL_SCREENS.first { it.title == "Meal detail" }, back)
        }
    }
}
