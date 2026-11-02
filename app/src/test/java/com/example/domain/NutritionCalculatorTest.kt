package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionCalculatorTest {

    private val male = BodyProfile(Sex.MALE, 30, 175.0, 72.4, ActivityLevel.LIGHT)
    private val female = male.copy(sex = Sex.FEMALE)

    @Test
    fun bmr_male() {
        assertEquals(1672.75, NutritionCalculator.bmr(male), 0.001)
    }

    @Test
    fun bmr_female_is_166_lower_than_male() {
        assertEquals(1506.75, NutritionCalculator.bmr(female), 0.001)
    }

    @Test
    fun maintenance_uses_activity_multiplier() {
        assertEquals(2341.85, NutritionCalculator.maintenanceCalories(male), 0.001)
        val active = male.copy(activity = ActivityLevel.ACTIVE)
        assertEquals(2843.675, NutritionCalculator.maintenanceCalories(active), 0.001)
    }

    @Test
    fun lose_half_kg_a_week() {
        val goal = GoalInput(GoalType.LOSE, 68.0, 0.5)
        assertEquals(1800, NutritionCalculator.dailyCalories(male, goal))
    }

    @Test
    fun female_lose_half_kg_a_week() {
        val goal = GoalInput(GoalType.LOSE, 68.0, 0.5)
        assertEquals(1550, NutritionCalculator.dailyCalories(female, goal))
    }

    @Test
    fun maintain_ignores_pace() {
        val goal = GoalInput(GoalType.MAINTAIN, 72.4, 0.75)
        assertEquals(2350, NutritionCalculator.dailyCalories(male, goal))
    }

    @Test
    fun gain_adds_calories() {
        val goal = GoalInput(GoalType.GAIN, 75.0, 0.25)
        assertEquals(2600, NutritionCalculator.dailyCalories(male, goal))
    }

    @Test
    fun active_lifestyle_raises_target() {
        val active = male.copy(activity = ActivityLevel.ACTIVE)
        val goal = GoalInput(GoalType.LOSE, 68.0, 0.5)
        assertEquals(2300, NutritionCalculator.dailyCalories(active, goal))
    }

    @Test
    fun calories_never_drop_below_floor() {
        val small = BodyProfile(Sex.FEMALE, 50, 150.0, 45.0, ActivityLevel.LIGHT)
        val goal = GoalInput(GoalType.LOSE, 40.0, 0.75)
        assertEquals(1200, NutritionCalculator.dailyCalories(small, goal))
    }

    @Test
    fun macros_for_1800_kcal() {
        val t = NutritionCalculator.macrosFor(1800, 72.4)
        assertEquals(1800, t.calories)
        assertEquals(116, t.proteinG)
        assertEquals(222, t.carbsG)
        assertEquals(50, t.fatG)
    }

    @Test
    fun macros_for_1550_kcal() {
        val t = NutritionCalculator.macrosFor(1550, 72.4)
        assertEquals(116, t.proteinG)
        assertEquals(175, t.carbsG)
        assertEquals(43, t.fatG)
    }

    @Test
    fun targets_combines_calories_and_macros() {
        val t = NutritionCalculator.targets(male, GoalInput(GoalType.LOSE, 68.0, 0.5))
        assertEquals(Targets(1800, 116, 222, 50), t)
    }

    @Test
    fun macro_calories_use_4_4_9() {
        assertEquals(1802, NutritionCalculator.macroCalories(116, 222, 50))
    }

    @Test
    fun weeks_to_goal_rounds_up() {
        assertEquals(9, NutritionCalculator.weeksToGoal(72.4, 68.0, 0.5))
        assertEquals(11, NutritionCalculator.weeksToGoal(72.4, 75.0, 0.25))
        assertEquals(14, NutritionCalculator.weeksToGoal(80.0, 70.0, 0.75))
    }

    @Test
    fun weeks_to_goal_is_zero_when_nothing_to_do() {
        assertEquals(0, NutritionCalculator.weeksToGoal(72.4, 72.4, 0.5))
        assertEquals(0, NutritionCalculator.weeksToGoal(72.4, 68.0, 0.0))
    }
}
