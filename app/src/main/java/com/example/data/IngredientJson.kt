package com.example.data

import com.example.model.MealIngredient
import org.json.JSONArray
import org.json.JSONObject

/** Stores a meal's items as text inside the meal document so they can be edited later. */
object IngredientJson {
    fun toJson(items: List<MealIngredient>): String {
        val arr = JSONArray()
        items.forEach {
            arr.put(
                JSONObject()
                    .put("name", it.name)
                    .put("portion", it.portion)
                    .put("quantity", it.quantity)
                    .put("unitCalories", it.unitCalories)
                    .put("unitProtein", it.unitProtein)
                    .put("unitCarbs", it.unitCarbs)
                    .put("unitFat", it.unitFat)
                    .put("unitFiber", it.unitFiber)
            )
        }
        return arr.toString()
    }

    fun fromJson(text: String): List<MealIngredient> {
        if (text.isBlank()) return emptyList()
        return try {
            fromArray(JSONArray(text))
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fromArray(arr: JSONArray?): List<MealIngredient> {
        if (arr == null) return emptyList()
        val out = mutableListOf<MealIngredient>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(
                MealIngredient(
                    name = o.optString("name", "Item ${i + 1}"),
                    portion = o.optString("portion", "1 serving"),
                    quantity = o.optDouble("quantity", 1.0),
                    unitCalories = o.optDouble("unitCalories", 0.0),
                    unitProtein = o.optDouble("unitProtein", 0.0),
                    unitCarbs = o.optDouble("unitCarbs", 0.0),
                    unitFat = o.optDouble("unitFat", 0.0),
                    unitFiber = o.optDouble("unitFiber", 0.0)
                )
            )
        }
        return out
    }
}
