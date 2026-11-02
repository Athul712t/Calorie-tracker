package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiFoodService
import com.example.data.CalorieRepository
import com.example.data.IndianFoodDatabase
import com.example.model.IndianFoodItem
import com.example.model.Meal
import com.example.model.MenuItem
import com.example.model.MealParsedResult
import com.example.model.MealType
import com.example.model.UserGoal
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalorieUiState(
    val selectedDate: String = getTodayDateString(),
    val meals: List<Meal> = emptyList(),
    val menuItems: List<MenuItem> = emptyList(),
    val goal: UserGoal = UserGoal(),
    val isLoading: Boolean = false,
    val isAnalyzing: Boolean = false,
    val analyzingMessage: String = "",
    val errorMessage: String? = null,
    val failedAiQuery: String? = null,
    val failedAiBitmap: Bitmap? = null,
    val failedAiNotes: String = "",
    val pendingMeal: MealParsedResult? = null,
    val indianSuggestions: List<IndianFoodItem> = emptyList(),
    val isGeneratingSuggestions: Boolean = false
) {
    val totalCalories: Double get() = meals.sumOf { it.calories }
    val totalProtein: Double get() = meals.sumOf { it.protein }
    val totalCarbs: Double get() = meals.sumOf { it.carbs }
    val totalFat: Double get() = meals.sumOf { it.fat }
    val totalFiber: Double get() = meals.sumOf { it.fiber }

    val remainingCalories: Double get() = (goal.calorieGoal.toDouble() - totalCalories).coerceAtLeast(0.0)
    val remainingProtein: Double get() = (goal.proteinGoal.toDouble() - totalProtein).coerceAtLeast(0.0)
    val remainingCarbs: Double get() = (goal.carbsGoal.toDouble() - totalCarbs).coerceAtLeast(0.0)
    val remainingFat: Double get() = (goal.fatGoal.toDouble() - totalFat).coerceAtLeast(0.0)

    val calorieProgress: Float
        get() = if (goal.calorieGoal > 0) (totalCalories / goal.calorieGoal).toFloat().coerceIn(0f, 1f) else 0f
    val proteinProgress: Float
        get() = if (goal.proteinGoal > 0) (totalProtein / goal.proteinGoal).toFloat().coerceIn(0f, 1f) else 0f
    val carbsProgress: Float
        get() = if (goal.carbsGoal > 0) (totalCarbs / goal.carbsGoal).toFloat().coerceIn(0f, 1f) else 0f
    val fatProgress: Float
        get() = if (goal.fatGoal > 0) (totalFat / goal.fatGoal).toFloat().coerceIn(0f, 1f) else 0f
}

fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

class CalorieTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CalorieRepository(application.applicationContext)
    private val aiService = AiFoodService()

    private val _uiState = MutableStateFlow(CalorieUiState())
    val uiState: StateFlow<CalorieUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    fun initializeUser(userId: String) {
        if (currentUserId == userId) return
        currentUserId = userId
        observeData(userId, _uiState.value.selectedDate)
        refreshIndianSuggestions()
    }

    private fun observeData(userId: String, dateString: String) {
        viewModelScope.launch {
            repository.observeMeals(userId, dateString)
                .catch { emit(emptyList()) }
                .collect { mealsList ->
                    _uiState.value = _uiState.value.copy(meals = mealsList)
                    refreshIndianSuggestions()
                }
        }

        viewModelScope.launch {
            repository.observeMenuItems(userId)
                .catch { emit(emptyList()) }
                .collect { menuList ->
                    _uiState.value = _uiState.value.copy(menuItems = menuList)
                }
        }

        viewModelScope.launch {
            repository.observeUserGoal(userId)
                .catch { emit(null) }
                .collect { savedGoal ->
                    if (savedGoal != null) {
                        _uiState.value = _uiState.value.copy(goal = savedGoal)
                        refreshIndianSuggestions()
                    }
                }
        }
    }

    fun selectDate(newDate: String) {
        _uiState.value = _uiState.value.copy(selectedDate = newDate)
        currentUserId?.let { uid ->
            observeData(uid, newDate)
        }
    }

    fun analyzeMealInput(text: String, bitmap: Bitmap?, notes: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                analyzingMessage = if (bitmap != null) "Analyzing photo & context with AI..." else "Estimating nutrition via ICMR-NIN IFCT standards...",
                errorMessage = null
            )

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val currentMealType = MealType.fromHour(currentHour)

            try {
                val parsed = aiService.analyzeMeal(
                    userInput = text,
                    bitmap = bitmap,
                    contextNotes = notes,
                    preferredMealType = currentMealType
                )
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    pendingMeal = parsed,
                    failedAiQuery = null,
                    failedAiBitmap = null,
                    failedAiNotes = "",
                    errorMessage = null
                )
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "AI estimation failed"
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    errorMessage = errorMsg,
                    failedAiQuery = text.ifBlank { notes },
                    failedAiBitmap = bitmap,
                    failedAiNotes = notes
                )
            }
        }
    }

    fun retryAiAnalysis() {
        val query = _uiState.value.failedAiQuery ?: return
        val bitmap = _uiState.value.failedAiBitmap
        val notes = _uiState.value.failedAiNotes
        analyzeMealInput(query, bitmap, notes)
    }

    fun useIndianDatabaseFallback(queryOverride: String? = null) {
        val query = queryOverride ?: _uiState.value.failedAiQuery
        if (query.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter food description first (e.g. '2 roti and dal')")
            return
        }

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMealType = MealType.fromHour(currentHour)

        try {
            val result = aiService.analyzeMealUsingIndianDatabase(query, currentMealType)
            _uiState.value = _uiState.value.copy(
                pendingMeal = result,
                errorMessage = null,
                failedAiQuery = null,
                failedAiBitmap = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = e.localizedMessage ?: "Could not match items in Indian database"
            )
        }
    }

    fun updateIngredientQuantity(index: Int, newQuantity: Double) {
        val current = _uiState.value.pendingMeal ?: return
        if (index !in current.ingredients.indices) return

        val updatedIngredients = current.ingredients.toMutableList()
        val currentIng = updatedIngredients[index]
        updatedIngredients[index] = currentIng.copy(quantity = newQuantity.coerceAtLeast(0.1))

        val totalCalories = updatedIngredients.sumOf { it.totalCalories }
        val totalProtein = updatedIngredients.sumOf { it.totalProtein }
        val totalCarbs = updatedIngredients.sumOf { it.totalCarbs }
        val totalFat = updatedIngredients.sumOf { it.totalFat }
        val totalFiber = updatedIngredients.sumOf { it.totalFiber }

        _uiState.value = _uiState.value.copy(
            pendingMeal = current.copy(
                ingredients = updatedIngredients,
                calories = totalCalories,
                protein = totalProtein,
                carbs = totalCarbs,
                fat = totalFat,
                fiber = totalFiber
            )
        )
    }

    fun removeIngredient(index: Int) {
        val current = _uiState.value.pendingMeal ?: return
        if (index !in current.ingredients.indices) return

        val updatedIngredients = current.ingredients.toMutableList()
        updatedIngredients.removeAt(index)

        if (updatedIngredients.isEmpty()) {
            _uiState.value = _uiState.value.copy(pendingMeal = null)
            return
        }

        val totalCalories = updatedIngredients.sumOf { it.totalCalories }
        val totalProtein = updatedIngredients.sumOf { it.totalProtein }
        val totalCarbs = updatedIngredients.sumOf { it.totalCarbs }
        val totalFat = updatedIngredients.sumOf { it.totalFat }
        val totalFiber = updatedIngredients.sumOf { it.totalFiber }

        _uiState.value = _uiState.value.copy(
            pendingMeal = current.copy(
                ingredients = updatedIngredients,
                calories = totalCalories,
                protein = totalProtein,
                carbs = totalCarbs,
                fat = totalFat,
                fiber = totalFiber
            )
        )
    }

    fun confirmAndSaveMeal(result: MealParsedResult) {
        val uid = currentUserId ?: Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toLong()

            val summary = if (result.ingredients.isNotEmpty()) {
                result.ingredients.joinToString(", ") {
                    val qtyStr = if (it.quantity % 1.0 == 0.0) it.quantity.toInt().toString() else String.format("%.1f", it.quantity)
                    "$qtyStr x ${it.name} (${it.totalCalories.toInt()} kcal)"
                }
            } else ""

            val meal = Meal(
                name = result.name,
                mealType = result.mealType.name,
                calories = result.calories,
                protein = result.protein,
                carbs = result.carbs,
                fat = result.fat,
                fiber = result.fiber,
                servings = result.servings,
                contextNotes = result.notes,
                imageUrl = result.imageBase64 ?: "",
                dateString = _uiState.value.selectedDate,
                hourOfDay = currentHour,
                userId = uid,
                ingredientsSummary = summary
            )

            try {
                repository.addMeal(meal, uid)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pendingMeal = null,
                    failedAiQuery = null,
                    failedAiBitmap = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to save meal: ${e.localizedMessage}"
                )
            }
        }
    }

    fun dismissPendingMeal() {
        _uiState.value = _uiState.value.copy(pendingMeal = null)
    }

    fun quickAddFromMenuItem(item: MenuItem, servingsMultiplier: Double = 1.0) {
        val uid = currentUserId ?: Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toLong()
            val meal = Meal(
                name = if (servingsMultiplier != 1.0) "${servingsMultiplier.toInt()}x ${item.name}" else item.name,
                mealType = item.category,
                calories = item.calories * servingsMultiplier,
                protein = item.protein * servingsMultiplier,
                carbs = item.carbs * servingsMultiplier,
                fat = item.fat * servingsMultiplier,
                fiber = item.fiber * servingsMultiplier,
                servings = servingsMultiplier,
                contextNotes = "Quick added from saved menu",
                dateString = _uiState.value.selectedDate,
                hourOfDay = currentHour,
                userId = uid
            )
            try {
                repository.addMeal(meal, uid)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Quick add failed: ${e.localizedMessage}")
            }
        }
    }

    fun quickAddIndianFood(item: IndianFoodItem, servingsMultiplier: Double = 1.0) {
        val uid = currentUserId ?: Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toLong()
            val meal = Meal(
                name = if (servingsMultiplier != 1.0) "${servingsMultiplier.toInt()}x ${item.name}" else item.name,
                mealType = item.category.name,
                calories = item.calories * servingsMultiplier,
                protein = item.protein * servingsMultiplier,
                carbs = item.carbs * servingsMultiplier,
                fat = item.fat * servingsMultiplier,
                fiber = item.fiber * servingsMultiplier,
                servings = servingsMultiplier,
                contextNotes = "Standard Indian portion: ${item.portion}",
                dateString = _uiState.value.selectedDate,
                hourOfDay = currentHour,
                userId = uid
            )
            try {
                repository.addMeal(meal, uid)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Quick add failed: ${e.localizedMessage}")
            }
        }
    }

    fun deleteMeal(mealId: String) {
        val uid = currentUserId ?: Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.deleteMeal(mealId, uid)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to delete: ${e.localizedMessage}")
            }
        }
    }

    fun updateGoals(calorie: Long, protein: Long, carbs: Long, fat: Long) {
        val uid = currentUserId ?: Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val updated = UserGoal(
                id = "current",
                calorieGoal = calorie,
                proteinGoal = protein,
                carbsGoal = carbs,
                fatGoal = fat,
                userId = uid
            )
            try {
                repository.saveUserGoal(updated, uid)
                _uiState.value = _uiState.value.copy(goal = updated)
                refreshIndianSuggestions()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to update goals: ${e.localizedMessage}")
            }
        }
    }

    fun addCustomMenuItem(name: String, calories: Double, protein: Double, carbs: Double, fat: Double, portion: String, category: MealType) {
        val uid = currentUserId ?: Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val item = MenuItem(
                name = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                defaultPortion = portion,
                category = category.name,
                userId = uid
            )
            try {
                repository.addCustomMenuItem(item, uid)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to add menu item: ${e.localizedMessage}")
            }
        }
    }

    fun refreshIndianSuggestions() {
        val state = _uiState.value
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMealType = MealType.fromHour(currentHour)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingSuggestions = true)
            val suggestions = aiService.getIndianSuggestionsForRemainingMacros(
                remainingCalories = state.remainingCalories,
                remainingProtein = state.remainingProtein,
                remainingCarbs = state.remainingCarbs,
                remainingFat = state.remainingFat,
                mealType = currentMealType
            )
            _uiState.value = _uiState.value.copy(
                indianSuggestions = suggestions,
                isGeneratingSuggestions = false
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
