package com.example.ui.nav

/**
 * Every screen in the app. Each route maps to a design file in docs/design/reference.
 * See the navigation map in docs/SPEC.md.
 */
object Routes {
    const val SCREEN_INDEX = "screens"          // developer index, not part of the product
    const val ONBOARDING = "onboarding"         // Onboarding.dc.html
    const val HOME = "home"                     // Main.dc.html, EmptyHome.dc.html (empty state)
    const val PROGRESS = "progress"             // Progress.dc.html
    const val PHOTOS = "photos"                 // Photos.dc.html
    const val PROFILE = "profile"               // ProfileSimple.dc.html
    const val PROFILE_EDIT = "profile/edit"     // ProfileEdit.dc.html
    const val GOAL_EDIT = "goal/edit"           // GoalEdit.dc.html
    const val CAPTURE = "capture"               // CaptureAuto.dc.html
    const val ANALYSING = "analysing"           // not designed yet
    const val MEAL_DETAIL = "meal/{mealId}"     // MealDetail.dc.html + MealChat.dc.html
    const val ADD_FOOD = "food/add"             // AddFood.dc.html
    const val HISTORY = "history"               // DayHistory.dc.html

    fun mealDetail(mealId: String) = "meal/$mealId"
}

/** Used by the placeholder screens and the developer index. */
data class ScreenInfo(val route: String, val title: String, val designFile: String)

val ALL_SCREENS = listOf(
    ScreenInfo(Routes.ONBOARDING, "Onboarding", "Onboarding.dc.html"),
    ScreenInfo(Routes.HOME, "Home", "Main.dc.html / EmptyHome.dc.html"),
    ScreenInfo(Routes.PROGRESS, "Progress", "Progress.dc.html"),
    ScreenInfo(Routes.PHOTOS, "Progress photos", "Photos.dc.html"),
    ScreenInfo(Routes.PROFILE, "Profile", "ProfileSimple.dc.html"),
    ScreenInfo(Routes.PROFILE_EDIT, "Edit profile", "ProfileEdit.dc.html"),
    ScreenInfo(Routes.GOAL_EDIT, "Edit goal", "GoalEdit.dc.html"),
    ScreenInfo(Routes.CAPTURE, "Capture", "CaptureAuto.dc.html"),
    ScreenInfo(Routes.ANALYSING, "Analysing", "(not designed yet)"),
    ScreenInfo(Routes.mealDetail("sample"), "Meal detail", "MealDetail.dc.html / MealChat.dc.html"),
    ScreenInfo(Routes.ADD_FOOD, "Add food", "AddFood.dc.html"),
    ScreenInfo(Routes.HISTORY, "History", "DayHistory.dc.html")
)
