package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.model.IndianFoodItem
import com.example.model.MealIngredient
import com.example.model.MealParsedResult
import com.example.model.MealType
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

private const val TAG = "AiFoodService"

class AiNutritionException(
    override val message: String,
    val rawQuery: String = "",
    cause: Throwable? = null
) : Exception(message, cause)

class AiFoodService {

    // Lazy initialization of Gemini model to prevent early initialization before App Check
    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }

    suspend fun analyzeMeal(
        userInput: String,
        bitmap: Bitmap?,
        contextNotes: String,
        preferredMealType: MealType
    ): MealParsedResult {
        val rawInput = userInput.trim()
        val combinedPrompt = buildString {
            append("You are an expert clinical nutrition scientist and Indian dietitian adhering strictly to the Indian Food Composition Tables (IFCT 2017) published by the ICMR - National Institute of Nutrition (NIN).\n")
            append("Analyze the following meal entry with an accurate, itemized ingredient and macronutrient breakdown:\n")
            if (rawInput.isNotBlank()) {
                append("Meal text: \"$rawInput\"\n")
            }
            if (contextNotes.isNotBlank()) {
                append("User context notes: \"$contextNotes\"\n")
            }
            append("Default meal category: ${preferredMealType.displayName}\n\n")

            append("CRITICAL INSTRUCTIONS FOR NUTRITIONAL ACCURACY & ITEMIZATION:\n")
            append("1. Break down compound meals into separate items/ingredients (e.g. '2 roti and dal' MUST contain separate entries for 'Roti (Plain)' with quantity 2.0, and 'Dal Tadka' with quantity 1.0).\n")
            append("2. Adhere strictly to verified ICMR-NIN IFCT portion benchmarks:\n")
            append("   - Steamed Idli (1 piece / 40g): 60 kcal, 2.0g protein, 12g carbs, 0.2g fat, 0.6g fiber.\n")
            append("   - Sambar / Sambhar (1 medium katori / 150ml): 110 kcal, 4.2g protein, 15g carbs, 3.5g fat, 3.8g fiber.\n")
            append("   - Coconut Chutney (2 tbsp / 30g): 75 kcal, 0.9g protein, 2.5g carbs, 7.0g fat, 1.2g fiber.\n")
            append("   - Standard Phulka/Roti (whole wheat, 35g dough, no oil/ghee): 85 kcal, 3g protein, 17.5g carbs, 0.4g fat, 2.5g fiber.\n")
            append("   - Roti with 1 tsp ghee: 115 kcal, 3g protein, 17.5g carbs, 3.8g fat, 2.5g fiber.\n")
            append("   - Dal Tadka (cooked yellow dal, 1 medium katori / 150g): 165 kcal, 7.5g protein, 23g carbs, 5g fat, 4.5g fiber.\n")
            append("   - Dal Makhani (150g katori): 260 kcal, 8.5g protein, 26g carbs, 14g fat, 5g fiber.\n")
            append("   - Steamed Basmati Rice (1 medium katori / 150g cooked): 195 kcal, 4g protein, 43g carbs, 0.5g fat, 1g fiber.\n")
            append("   - Paneer Butter Masala (150g katori): 340 kcal, 12g protein, 14g carbs, 26g fat, 2g fiber.\n")
            append("   - Palak Paneer (150g katori): 230 kcal, 12.5g protein, 10g carbs, 16g fat, 4.2g fiber.\n")
            append("   - Aloo Gobi Matar (130g katori): 160 kcal, 3.5g protein, 22g carbs, 6.5g fat, 4.5g fiber.\n")
            append("   - Curd / Dahi (100g katori): 70 kcal, 3.5g protein, 4.5g carbs, 4g fat, 0g fiber.\n")
            append("   - Masala Chai with whole milk & 1 tsp sugar (150ml cup): 90 kcal, 2.5g protein, 12g carbs, 3.5g fat.\n")
            append("   - Boiled Whole Egg (1 egg): 70 kcal, 6.3g protein, 0.5g carbs, 5g fat.\n")
            append("3. Sum all ingredient calories and macros to compute the top-level meal total.\n")
            append("4. Return a valid JSON object matching this schema:\n")
            append("{\n")
            append("  \"name\": \"Full Meal Name (e.g. 2 Roti with Dal Tadka)\",\n")
            append("  \"mealType\": \"BREAKFAST\" | \"LUNCH\" | \"SNACK\" | \"DINNER\",\n")
            append("  \"calories\": number,\n")
            append("  \"protein\": number,\n")
            append("  \"carbs\": number,\n")
            append("  \"fat\": number,\n")
            append("  \"fiber\": number,\n")
            append("  \"servings\": 1.0,\n")
            append("  \"notes\": \"ICMR-NIN IFCT standard benchmark notes\",\n")
            append("  \"confidence\": \"High\" | \"Medium\" | \"Low\",\n")
            append("  \"ingredients\": [\n")
            append("    {\n")
            append("      \"name\": \"Item name (e.g. Roti (Plain))\",\n")
            append("      \"portion\": \"1 piece (35g)\",\n")
            append("      \"quantity\": 2.0,\n")
            append("      \"unitCalories\": 85.0,\n")
            append("      \"unitProtein\": 3.0,\n")
            append("      \"unitCarbs\": 17.5,\n")
            append("      \"unitFat\": 0.4,\n")
            append("      \"unitFiber\": 2.5\n")
            append("    }\n")
            append("  ]\n")
            append("}\n")
        }

        var imageBase64String: String? = null
        if (bitmap != null) {
            try {
                val stream = ByteArrayOutputStream()
                thumbnailOf(bitmap).compress(Bitmap.CompressFormat.JPEG, 60, stream)
                val byteArray = stream.toByteArray()
                imageBase64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to encode bitmap", e)
            }
        }

        try {
            val response = withTimeout(35_000L) {
                if (bitmap != null) {
                    model.generateContent(
                        content {
                            text(combinedPrompt)
                            image(bitmap)
                        }
                    )
                } else {
                    model.generateContent(combinedPrompt)
                }
            }

            val jsonText = response.text
            if (!jsonText.isNullOrBlank()) {
                val cleanedJson = jsonText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val json = JSONObject(cleanedJson)
                val name = json.optString("name", rawInput.ifBlank { "Indian Meal" })
                val mealTypeStr = json.optString("mealType", preferredMealType.name)
                val mealType = try {
                    MealType.valueOf(mealTypeStr.uppercase())
                } catch (e: Exception) {
                    preferredMealType
                }

                // Parse ingredients array
                val ingredientsList = mutableListOf<MealIngredient>()
                val ingredientsArray = json.optJSONArray("ingredients")
                if (ingredientsArray != null && ingredientsArray.length() > 0) {
                    for (i in 0 until ingredientsArray.length()) {
                        val itemObj = ingredientsArray.getJSONObject(i)
                        ingredientsList.add(
                            MealIngredient(
                                name = itemObj.optString("name", "Item ${i + 1}"),
                                portion = itemObj.optString("portion", "1 serving"),
                                quantity = itemObj.optDouble("quantity", 1.0),
                                unitCalories = itemObj.optDouble("unitCalories", 0.0),
                                unitProtein = itemObj.optDouble("unitProtein", 0.0),
                                unitCarbs = itemObj.optDouble("unitCarbs", 0.0),
                                unitFat = itemObj.optDouble("unitFat", 0.0),
                                unitFiber = itemObj.optDouble("unitFiber", 0.0)
                            )
                        )
                    }
                }

                // If ingredients are parsed, derive totals from ingredients for guaranteed mathematical consistency
                val totalCalories = if (ingredientsList.isNotEmpty()) {
                    ingredientsList.sumOf { it.totalCalories }
                } else {
                    json.optDouble("calories", 0.0)
                }

                val totalProtein = if (ingredientsList.isNotEmpty()) {
                    ingredientsList.sumOf { it.totalProtein }
                } else {
                    json.optDouble("protein", 0.0)
                }

                val totalCarbs = if (ingredientsList.isNotEmpty()) {
                    ingredientsList.sumOf { it.totalCarbs }
                } else {
                    json.optDouble("carbs", 0.0)
                }

                val totalFat = if (ingredientsList.isNotEmpty()) {
                    ingredientsList.sumOf { it.totalFat }
                } else {
                    json.optDouble("fat", 0.0)
                }

                val totalFiber = if (ingredientsList.isNotEmpty()) {
                    ingredientsList.sumOf { it.totalFiber }
                } else {
                    json.optDouble("fiber", 0.0)
                }

                val servings = json.optDouble("servings", 1.0)
                val notes = json.optString("notes", contextNotes)
                val confidence = json.optString("confidence", "High")

                return MealParsedResult(
                    name = name,
                    mealType = mealType,
                    calories = totalCalories,
                    protein = totalProtein,
                    carbs = totalCarbs,
                    fat = totalFat,
                    fiber = totalFiber,
                    servings = servings,
                    notes = notes,
                    confidence = confidence,
                    imageBase64 = imageBase64String,
                    ingredients = ingredientsList,
                    isAiGenerated = true,
                    rawQuery = rawInput
                )
            } else {
                throw AiNutritionException(
                    "Gemini AI returned an empty response. Please try again.",
                    rawQuery = rawInput
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini generation timed out", e)
            throw AiNutritionException(
                "AI nutrition analysis timed out after 35 seconds. Please check your connection or switch to verified Indian food database.",
                rawQuery = rawInput,
                cause = e
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: AiNutritionException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed: ${e.message}", e)
            val friendlyMsg = when {
                e.message?.contains("RESOURCE_EXHAUSTED", ignoreCase = true) == true ->
                    "Gemini quota exceeded for this account. You can use the verified Indian food database fallback."
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    "AI service permission denied or App Check error. You can use the verified Indian food database."
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("ConnectException", ignoreCase = true) == true ->
                    "Network error connecting to Gemini AI. Check internet connection or use Indian food database."
                else ->
                    "AI estimation could not complete: ${e.localizedMessage ?: "Unknown connection error"}. You can retry or use verified Indian database."
            }
            throw AiNutritionException(friendlyMsg, rawQuery = rawInput, cause = e)
        }
    }

    /**
     * Parses meals purely through verified ICMR-NIN Indian food database with multi-item support.
     * Guarantees 0 random numbers and 100% itemized transparency.
     */
    fun analyzeMealUsingIndianDatabase(
        query: String,
        preferredMealType: MealType
    ): MealParsedResult {
        val cleaned = query.trim()
        val parsedItems = IndianFoodDatabase.parseMultiItems(cleaned)

        if (parsedItems.isEmpty()) {
            throw AiNutritionException(
                "Could not identify Indian food items from: \"$cleaned\". Try specifying items like '2 roti and dal', 'poha', or 'biryani'.",
                rawQuery = cleaned
            )
        }

        val ingredients = parsedItems.map { (qty, item) ->
            MealIngredient(
                name = item.name,
                portion = item.portion,
                quantity = qty,
                unitCalories = item.calories,
                unitProtein = item.protein,
                unitCarbs = item.carbs,
                unitFat = item.fat,
                unitFiber = item.fiber
            )
        }

        val totalCalories = ingredients.sumOf { it.totalCalories }
        val totalProtein = ingredients.sumOf { it.totalProtein }
        val totalCarbs = ingredients.sumOf { it.totalCarbs }
        val totalFat = ingredients.sumOf { it.totalFat }
        val totalFiber = ingredients.sumOf { it.totalFiber }

        val mealName = if (ingredients.size == 1) {
            val ing = ingredients.first()
            if (ing.quantity != 1.0) "${ing.quantity.toInt()}x ${ing.name}" else ing.name
        } else {
            ingredients.joinToString(" + ") {
                if (it.quantity != 1.0) "${it.quantity.toInt()} ${it.name}" else it.name
            }
        }

        return MealParsedResult(
            name = mealName,
            mealType = parsedItems.firstOrNull()?.second?.category ?: preferredMealType,
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fat = totalFat,
            fiber = totalFiber,
            servings = 1.0,
            notes = "Standard verified Indian portions (ICMR-NIN database)",
            confidence = "High (Verified Indian Database)",
            imageBase64 = null,
            ingredients = ingredients,
            isAiGenerated = false,
            rawQuery = cleaned
        )
    }

    /** Small copy of the photo that is stored with the meal (Firestore documents are limited in size). */
    private fun thumbnailOf(src: Bitmap, maxSide: Int = 256): Bitmap {
        val longest = maxOf(src.width, src.height)
        if (longest <= maxSide) return src
        val scale = maxSide.toFloat() / longest
        return Bitmap.createScaledBitmap(
            src,
            (src.width * scale).toInt().coerceAtLeast(1),
            (src.height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    /**
     * Applies a plain-language change ("make it 3 rotis", "no ghee") to a meal.
     * Returns the updated meal; totals are always recomputed from the items.
     */
    suspend fun refineMeal(current: MealParsedResult, instruction: String): MealParsedResult {
        val items = JSONArray()
        current.ingredients.forEach {
            items.put(
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
        val prompt = buildString {
            append("You edit a logged meal for an Indian calorie tracker. Use Indian Food Composition Tables (IFCT 2017) values.\n")
            append("Current meal name: \"${current.name}\"\n")
            append("Current items (unit values are per 1 quantity): $items\n")
            append("User request: \"${instruction.trim()}\"\n\n")
            append("Apply only what the user asked. Keep every other item exactly as it is. ")
            append("You may add, remove or change items and quantities. Do not invent items the user did not mention.\n")
            append("Return JSON only: {\"name\": string, \"reply\": short sentence describing what you changed, ")
            append("\"ingredients\": [{\"name\": string, \"portion\": string, \"quantity\": number, ")
            append("\"unitCalories\": number, \"unitProtein\": number, \"unitCarbs\": number, \"unitFat\": number, \"unitFiber\": number}]}\n")
        }
        try {
            val response = withTimeout(25_000L) { model.generateContent(prompt) }
            val text = response.text
            if (text.isNullOrBlank()) {
                throw AiNutritionException("The AI returned an empty answer. Please try again.")
            }
            val cleaned = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val json = JSONObject(cleaned)
            val items2 = IngredientJson.fromArray(json.optJSONArray("ingredients"))
            if (items2.isEmpty()) {
                throw AiNutritionException("I couldn't apply that change. Try rephrasing it.")
            }
            return current.copy(
                name = json.optString("name", current.name).ifBlank { current.name },
                calories = items2.sumOf { it.totalCalories },
                protein = items2.sumOf { it.totalProtein },
                carbs = items2.sumOf { it.totalCarbs },
                fat = items2.sumOf { it.totalFat },
                fiber = items2.sumOf { it.totalFiber },
                ingredients = items2,
                notes = json.optString("reply", "")
            )
        } catch (e: TimeoutCancellationException) {
            throw AiNutritionException("That took too long. Please try again.", cause = e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AiNutritionException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "refineMeal failed: ${e.message}", e)
            throw AiNutritionException("I couldn't apply that change: ${e.localizedMessage ?: "unknown error"}", cause = e)
        }
    }

    suspend fun getIndianSuggestionsForRemainingMacros(
        remainingCalories: Double,
        remainingProtein: Double,
        remainingCarbs: Double,
        remainingFat: Double,
        mealType: MealType
    ): List<IndianFoodItem> {
        val prompt = buildString {
            append("Suggest 3 distinct Indian food items available in India for someone with remaining daily budget: ")
            append("${remainingCalories.toInt()} kcal, ${remainingProtein.toInt()}g protein, ${remainingCarbs.toInt()}g carbs, ${remainingFat.toInt()}g fat. ")
            append("Current meal: ${mealType.displayName}. ")
            append("Output JSON array format: [{\"name\": string, \"portion\": string, \"calories\": number, \"protein\": number, \"carbs\": number, \"fat\": number, \"fiber\": number}]")
        }

        try {
            val response = withTimeout(20_000L) {
                model.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                val cleaned = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val jsonArr = org.json.JSONArray(cleaned)
                val list = mutableListOf<IndianFoodItem>()
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    list.add(
                        IndianFoodItem(
                            name = obj.getString("name"),
                            portion = obj.optString("portion", "1 serving"),
                            calories = obj.optDouble("calories", 150.0),
                            protein = obj.optDouble("protein", 5.0),
                            carbs = obj.optDouble("carbs", 20.0),
                            fat = obj.optDouble("fat", 4.0),
                            fiber = obj.optDouble("fiber", 2.0),
                            category = mealType
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    return list
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Fallback to local Indian suggestions: ${e.message}")
        }

        return IndianFoodDatabase.getSuggestedItemsForMacros(
            remainingCalories, remainingProtein, remainingCarbs, remainingFat, mealType
        )
    }
}
