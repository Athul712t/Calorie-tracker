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
    val ingredientsJson: String = "",
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
    /** LOSE, MAINTAIN or GAIN. */
    val goalType: String = "LOSE",
    val goalWeightKg: Double = 0.0,
    val startWeightKg: Double = 0.0,
    val paceKgPerWeek: Double = 0.5,
    /** True when the user typed their own calories and macros instead of the calculated ones. */
    val manual: Boolean = false,
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


@IgnoreExtraProperties
data class UserProfile(
    val name: String = "",
    /** MALE or FEMALE. */
    val sex: String = "MALE",
    val age: Long = 30L,
    val heightCm: Double = 170.0,
    /** LIGHT, MODERATE or ACTIVE. */
    val activity: String = "LIGHT",
    /** Diet and allergies, comma separated, for example "Vegetarian,Peanuts". */
    val dietTags: String = "",
    val userId: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class WeightLog(
    @DocumentId val id: String = "",
    val dateString: String = "",
    val kg: Double = 0.0,
    val userId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

/** One progress photo. [angle] is FRONT, LEFT, RIGHT or BACK. One photo per angle per day. */
@IgnoreExtraProperties
data class ProgressPhoto(
    @DocumentId val id: String = "",
    val angle: String = "FRONT",
    val dateString: String = "",
    /** Weight at the time of the check-in, 0 if unknown. */
    val weightKg: Double = 0.0,
    val storagePath: String = "",
    val userId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
