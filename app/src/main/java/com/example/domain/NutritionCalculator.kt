package com.example.domain

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

enum class Sex { MALE, FEMALE }

enum class ActivityLevel(val multiplier: Double) {
    LIGHT(1.4),
    MODERATE(1.55),
    ACTIVE(1.7)
}

enum class GoalType { LOSE, MAINTAIN, GAIN }

data class BodyProfile(
    val sex: Sex,
    val ageYears: Int,
    val heightCm: Double,
    val weightKg: Double,
    val activity: ActivityLevel
)

data class GoalInput(
    val type: GoalType,
    val goalWeightKg: Double,
    /** Kilograms per week. Ignored for MAINTAIN. */
    val paceKgPerWeek: Double
)

data class Targets(
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int
) {
    /** Calories the macros add up to (4/4/9 kcal per gram). */
    val macroCalories: Int get() = NutritionCalculator.macroCalories(proteinG, carbsG, fatG)
}

/**
 * Pure Kotlin (no Android imports) so it can be unit tested on the JVM.
 * The formulas are defined in docs/SPEC.md section "Calorie model".
 */
object NutritionCalculator {
    const val MIN_CALORIES = 1200
    const val KCAL_PER_KG_PER_WEEK = 1100.0
    const val PROTEIN_G_PER_KG = 1.6
    const val FAT_SHARE_OF_CALORIES = 0.25
    private const val ROUND_TO = 50

    /** Mifflin-St Jeor basal metabolic rate in kcal per day. */
    fun bmr(p: BodyProfile): Double {
        val base = 10.0 * p.weightKg + 6.25 * p.heightCm - 5.0 * p.ageYears
        return base + if (p.sex == Sex.MALE) 5.0 else -161.0
    }

    /** Calories needed to hold the current weight. */
    fun maintenanceCalories(p: BodyProfile): Double = bmr(p) * p.activity.multiplier

    /** Daily calorie target: maintenance +/- pace, rounded to 50, never below [MIN_CALORIES]. */
    fun dailyCalories(p: BodyProfile, goal: GoalInput): Int {
        val adjustment = when (goal.type) {
            GoalType.LOSE -> -goal.paceKgPerWeek * KCAL_PER_KG_PER_WEEK
            GoalType.GAIN -> goal.paceKgPerWeek * KCAL_PER_KG_PER_WEEK
            GoalType.MAINTAIN -> 0.0
        }
        val raw = maintenanceCalories(p) + adjustment
        val rounded = (raw / ROUND_TO).roundToInt() * ROUND_TO
        return max(MIN_CALORIES, rounded)
    }

    /** Protein 1.6 g per kg, fat 25% of calories, carbs fill the rest. */
    fun macrosFor(calories: Int, weightKg: Double): Targets {
        val protein = (PROTEIN_G_PER_KG * weightKg).roundToInt()
        val fat = (calories * FAT_SHARE_OF_CALORIES / 9.0).roundToInt()
        val carbs = max(0, ((calories - protein * 4 - fat * 9) / 4.0).roundToInt())
        return Targets(calories, protein, carbs, fat)
    }

    fun targets(p: BodyProfile, goal: GoalInput): Targets =
        macrosFor(dailyCalories(p, goal), p.weightKg)

    fun macroCalories(proteinG: Int, carbsG: Int, fatG: Int): Int =
        proteinG * 4 + carbsG * 4 + fatG * 9

    /** Whole weeks to reach the goal weight at the given pace. 0 if there is nothing to do. */
    fun weeksToGoal(currentKg: Double, goalKg: Double, paceKgPerWeek: Double): Int {
        if (paceKgPerWeek <= 0.0) return 0
        val weeks = abs(currentKg - goalKg) / paceKgPerWeek
        return ceil(weeks - 1e-9).toInt().coerceAtLeast(0)
    }
}
