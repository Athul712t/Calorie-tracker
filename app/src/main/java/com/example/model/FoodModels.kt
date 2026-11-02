package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    SNACK("Snacks & Tea"),
    DINNER("Dinner");

    companion object {
        fun fromHour(hour: Int): MealType = when (hour) {
            in 5..11 -> BREAKFAST
            in 12..16 -> LUNCH
            in 17..19 -> SNACK
            else -> DINNER
        }
    }
}

@IgnoreExtraProperties
data class Meal(
    @DocumentId val id: String = "",
    val name: String = "",
    val mealType: String = MealType.LUNCH.name,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val servings: Double = 1.0,
    val contextNotes: String = "",
    val imageUrl: String = "",
    val dateString: String = "",
    val hourOfDay: Long = 12L,
    val userId: String = "",
    val ingredientsSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MenuItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val defaultPortion: String = "1 serving",
    val category: String = MealType.LUNCH.name,
    val frequencyCount: Long = 1L,
    @ServerTimestamp val lastLoggedAt: Timestamp? = null,
    val userId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserGoal(
    @DocumentId val id: String = "",
    val calorieGoal: Long = 2000L,
    val proteinGoal: Long = 100L,
    val carbsGoal: Long = 220L,
    val fatGoal: Long = 55L,
    val userId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class IndianFoodItem(
    val name: String,
    val portion: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 2.0,
    val category: MealType,
    val tags: List<String> = emptyList()
)

data class MealIngredient(
    val name: String,
    val portion: String = "1 serving",
    val quantity: Double = 1.0,
    val unitCalories: Double = 0.0,
    val unitProtein: Double = 0.0,
    val unitCarbs: Double = 0.0,
    val unitFat: Double = 0.0,
    val unitFiber: Double = 0.0
) {
    val totalCalories: Double get() = unitCalories * quantity
    val totalProtein: Double get() = unitProtein * quantity
    val totalCarbs: Double get() = unitCarbs * quantity
    val totalFat: Double get() = unitFat * quantity
    val totalFiber: Double get() = unitFiber * quantity
}

data class MealParsedResult(
    val name: String,
    val mealType: MealType,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val servings: Double = 1.0,
    val notes: String = "",
    val confidence: String = "High",
    val imageBase64: String? = null,
    val ingredients: List<MealIngredient> = emptyList(),
    val isAiGenerated: Boolean = true,
    val rawQuery: String = ""
)
