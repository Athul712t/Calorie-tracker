package com.example

import com.example.data.AiFoodService
import com.example.data.IndianFoodDatabase
import com.example.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testTwoIdliWithSambharAndChutney_databaseParsing() {
    val input = "2 idli with sambhar and chutney"
    val parsed = IndianFoodDatabase.parseMultiItems(input)
    
    println("--- Testing input: '$input' ---")
    assertEquals("Should detect 3 items: idli, sambhar, and chutney", 3, parsed.size)

    val idli = parsed[0]
    assertEquals(2.0, idli.first, 0.01)
    assertEquals("Idli (Steamed)", idli.second.name)
    assertEquals(60.0, idli.second.calories, 0.01)

    val sambhar = parsed[1]
    assertEquals(1.0, sambhar.first, 0.01)
    assertEquals("Sambar (Lentil & Veg Stew)", sambhar.second.name)
    assertEquals(110.0, sambhar.second.calories, 0.01)

    val chutney = parsed[2]
    assertEquals(1.0, chutney.first, 0.01)
    assertEquals("Coconut Chutney", chutney.second.name)
    assertEquals(75.0, chutney.second.calories, 0.01)

    // Now test through AiFoodService using the verified Indian database
    val service = AiFoodService()
    val mealResult = service.analyzeMealUsingIndianDatabase(input, MealType.BREAKFAST)

    println("Meal Name: ${mealResult.name}")
    println("Total Calories: ${mealResult.calories} kcal")
    println("Total Protein: ${mealResult.protein}g")
    println("Total Carbs: ${mealResult.carbs}g")
    println("Total Fat: ${mealResult.fat}g")
    println("Total Fiber: ${mealResult.fiber}g")
    println("Itemized Ingredients count: ${mealResult.ingredients.size}")
    mealResult.ingredients.forEachIndexed { index, ing ->
      println("  ${index + 1}. ${ing.name} (Qty: ${ing.quantity}, Portion: ${ing.portion}): ${ing.totalCalories} kcal | ${ing.totalProtein}g P | ${ing.totalCarbs}g C | ${ing.totalFat}g F | ${ing.totalFiber}g Fib")
    }

    // Assertions
    assertEquals(305.0, mealResult.calories, 0.01)
    assertEquals(9.1, mealResult.protein, 0.01)
    assertEquals(41.5, mealResult.carbs, 0.01)
    assertEquals(10.9, mealResult.fat, 0.01)
    assertEquals(6.2, mealResult.fiber, 0.01)
  }
}
