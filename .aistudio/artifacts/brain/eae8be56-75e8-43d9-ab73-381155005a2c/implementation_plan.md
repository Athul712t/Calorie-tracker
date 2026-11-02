# Fix AI Calorie Accuracy & Itemized Indian Nutrition Breakdown

Eliminated silent error fallbacks that produced inaccurate calorie counts and random meals by introducing transparent AI failure reporting, strict Indian Food Composition Tables (IFCT) nutrition prompting, and itemized multi-item meal breakdowns.

---

### Implementation Status: COMPLETE (Verified & Compiled)

All planned enhancements have been implemented and verified with a clean build:
1. **Accurate Clinical Indian Nutrition Prompting**:
   - Upgraded `AiFoodService.kt` to benchmark strictly against the **Indian Food Composition Tables (IFCT 2017)** by ICMR - National Institute of Nutrition (NIN).
   - Enforced itemized JSON output parsing that extracts each dish component (e.g., *Roti (Plain)*: 85 kcal / 3g P / 17.5g C, *Dal Tadka*: 165 kcal / 7.5g P / 23g C, *Steamed Basmati Rice*: 195 kcal / 4g P / 43g C).
2. **Transparent Error Handling & Zero Silent Fallbacks**:
   - Replaced silent `catch` block that was previously swallowing Gemini exceptions and substituting hardcoded 250 kcal dummy *"Desi Meal"* objects.
   - Throws specific `AiNutritionException` with contextual error details (quota limits, network disconnects, or App Check attestation issues).
   - Added an inline error recovery banner in `CalorieTrackerScreen.kt` with **"Retry AI"** and **"Use Indian DB"** fallback actions.
3. **Multi-Item Local Indian Food Parser**:
   - Enhanced `IndianFoodDatabase.kt` with `parseMultiItems()` to parse compound inputs (e.g., *"2 roti and dal with dahi"*) into multiple verified food items even when offline or using local fallback.
4. **Interactive Itemized Meal Verification Dialog**:
   - Redesigned `MealConfirmationDialog.kt` to display each itemized ingredient with:
     - Portion size and unit calorie/protein information.
     - `+` and `-` quantity steppers (e.g. adjust from 2 to 3 rotis).
     - Individual component delete button.
     - Live auto-summed calories and macronutrient totals.
     - Manual override toggle allowing direct typing when desired.
5. **Logged Meal Ingredient Transparency**:
   - Updated `MealListSection.kt` and `FoodModels.kt` to record and display the ingredient breakdown summary in the daily meal log timeline.
