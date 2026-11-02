package com.example.data

import com.example.model.IndianFoodItem
import com.example.model.MealType

object IndianFoodDatabase {

    val items: List<IndianFoodItem> = listOf(
        // BREAKFAST
        IndianFoodItem("Roti (Plain)", "1 piece (35g)", 85.0, 3.0, 17.5, 0.4, 2.5, MealType.LUNCH, listOf("chapati", "fulka", "phulka")),
        IndianFoodItem("Roti with Ghee", "1 piece", 115.0, 3.0, 17.5, 3.8, 2.5, MealType.LUNCH, listOf("ghee roti", "chapathi")),
        IndianFoodItem("Poha with Peanuts", "1 medium bowl (150g)", 240.0, 5.5, 42.0, 6.0, 3.0, MealType.BREAKFAST, listOf("poha", "kanda poha", "aval")),
        IndianFoodItem("Idli (Steamed)", "1 piece (40g)", 60.0, 2.0, 12.0, 0.2, 0.6, MealType.BREAKFAST, listOf("idli", "idly", "steamed idli", "plain idli")),
        IndianFoodItem("Sambar (Lentil & Veg Stew)", "1 katori (150ml)", 110.0, 4.2, 15.0, 3.5, 3.8, MealType.BREAKFAST, listOf("sambar", "sambhar", "katori sambar")),
        IndianFoodItem("Coconut Chutney", "2 tbsp (30g)", 75.0, 0.9, 2.5, 7.0, 1.2, MealType.BREAKFAST, listOf("chutney", "coconut chutney", "nariyal chutney", "white chutney")),
        IndianFoodItem("Idli (2 pcs) with Sambar", "2 idlis + 1 katori sambar", 230.0, 8.2, 39.0, 3.9, 5.0, MealType.BREAKFAST, listOf("idli sambar", "idly sambar")),
        IndianFoodItem("Masala Dosa", "1 medium dosa with potato masala", 310.0, 6.0, 48.0, 11.0, 3.5, MealType.BREAKFAST, listOf("dosa", "masala dosa")),
        IndianFoodItem("Plain Dosa", "1 medium dosa", 160.0, 4.0, 28.0, 3.5, 2.0, MealType.BREAKFAST, listOf("dosa", "sada dosa")),
        IndianFoodItem("Upma", "1 medium bowl (150g)", 210.0, 4.8, 35.0, 5.8, 3.2, MealType.BREAKFAST, listOf("upma", "rava upma")),
        IndianFoodItem("Aloo Paratha with Curd", "1 paratha + 1/2 cup curd", 320.0, 7.5, 44.0, 13.0, 3.8, MealType.BREAKFAST, listOf("aloo paratha", "parantha")),
        IndianFoodItem("Paneer Paratha", "1 paratha", 280.0, 11.0, 32.0, 12.0, 3.0, MealType.BREAKFAST, listOf("paneer paratha")),
        IndianFoodItem("Moong Dal Cheela", "2 medium cheelas", 180.0, 11.5, 24.0, 4.5, 4.5, MealType.BREAKFAST, listOf("cheela", "chilla", "puda")),
        IndianFoodItem("Besan Chilla", "2 medium chillas", 195.0, 9.5, 26.0, 6.0, 4.0, MealType.BREAKFAST, listOf("besan chilla")),
        IndianFoodItem("Boiled Eggs (2 pcs)", "2 whole eggs", 140.0, 12.5, 1.0, 9.5, 0.0, MealType.BREAKFAST, listOf("egg", "boiled egg", "anda")),
        IndianFoodItem("Egg Bhurji (2 eggs)", "1 bowl (with onion & tomato)", 210.0, 13.0, 4.0, 15.5, 1.2, MealType.BREAKFAST, listOf("bhurji", "scrambled egg")),
        IndianFoodItem("Masala Chai (with milk & sugar)", "1 cup (150ml)", 90.0, 2.5, 12.0, 3.5, 0.0, MealType.BREAKFAST, listOf("chai", "tea", "tea with sugar")),
        IndianFoodItem("Filter Coffee", "1 cup (150ml)", 110.0, 3.0, 14.0, 4.5, 0.0, MealType.BREAKFAST, listOf("coffee", "kaapi")),

        // LUNCH & DINNER MAINS
        IndianFoodItem("Steamed Basmati Rice", "1 medium bowl (150g cooked)", 195.0, 4.0, 43.0, 0.5, 1.0, MealType.LUNCH, listOf("rice", "chawal", "white rice")),
        IndianFoodItem("Jeera Rice", "1 medium bowl (150g)", 230.0, 4.2, 44.0, 4.5, 1.5, MealType.LUNCH, listOf("jeera rice")),
        IndianFoodItem("Brown Rice", "1 medium bowl (150g cooked)", 180.0, 4.5, 38.0, 1.5, 3.0, MealType.LUNCH, listOf("brown rice")),
        IndianFoodItem("Dal Tadka (Yellow Dal)", "1 medium katori (150g)", 165.0, 7.5, 23.0, 5.0, 4.5, MealType.LUNCH, listOf("dal", "dal tadka", "toor dal", "yellow dal")),
        IndianFoodItem("Dal Makhani", "1 medium katori (150g)", 260.0, 8.5, 26.0, 14.0, 5.0, MealType.DINNER, listOf("dal makhani", "urad dal")),
        IndianFoodItem("Rajma Masala", "1 medium katori (150g)", 210.0, 9.5, 32.0, 4.5, 6.5, MealType.LUNCH, listOf("rajma", "kidney beans")),
        IndianFoodItem("Chole (Punjabi Chickpeas)", "1 medium katori (150g)", 240.0, 9.0, 34.0, 8.0, 7.0, MealType.LUNCH, listOf("chole", "chana masala", "kabuli chana")),
        IndianFoodItem("Paneer Butter Masala", "1 medium katori (150g)", 340.0, 12.0, 14.0, 26.0, 2.0, MealType.DINNER, listOf("paneer butter masala", "makhani")),
        IndianFoodItem("Palak Paneer", "1 medium katori (150g)", 230.0, 12.5, 10.0, 16.0, 4.2, MealType.LUNCH, listOf("palak paneer", "saag")),
        IndianFoodItem("Kadai Paneer", "1 medium katori (150g)", 270.0, 13.0, 11.0, 19.0, 3.0, MealType.DINNER, listOf("kadai paneer")),
        IndianFoodItem("Raw / Roasted Paneer (100g)", "100g cubes", 265.0, 18.0, 3.5, 20.0, 0.0, MealType.SNACK, listOf("paneer", "cottage cheese")),
        IndianFoodItem("Bhindi Masala (Okra)", "1 medium katori (120g)", 130.0, 2.8, 14.0, 7.0, 4.0, MealType.LUNCH, listOf("bhindi", "okra", "ladyfinger")),
        IndianFoodItem("Aloo Gobi Matar", "1 medium katori (130g)", 160.0, 3.5, 22.0, 6.5, 4.5, MealType.LUNCH, listOf("aloo gobi", "gobi")),
        IndianFoodItem("Mix Veg Sabzi", "1 medium katori (130g)", 140.0, 3.2, 18.0, 6.5, 4.0, MealType.LUNCH, listOf("mix veg", "sabzi")),
        IndianFoodItem("Soya Chunks Curry", "1 medium katori (150g)", 190.0, 17.0, 16.0, 6.5, 6.0, MealType.LUNCH, listOf("soya", "soya chunks", "meal maker")),
        IndianFoodItem("Curd (Dahi / Yogurt)", "1 small cup (100g)", 70.0, 3.5, 4.5, 4.0, 0.0, MealType.LUNCH, listOf("curd", "dahi", "yogurt")),
        IndianFoodItem("Curd Rice with Tempering", "1 medium bowl (200g)", 260.0, 6.0, 40.0, 8.5, 1.5, MealType.LUNCH, listOf("curd rice", "thayir sadam")),
        IndianFoodItem("Khichdi (Moong Dal & Rice)", "1 bowl with light ghee (200g)", 240.0, 7.8, 38.0, 6.5, 4.5, MealType.DINNER, listOf("khichdi", "khichri")),
        IndianFoodItem("Veg Biryani", "1 plate (250g)", 360.0, 7.5, 54.0, 13.0, 4.5, MealType.DINNER, listOf("biryani", "veg biryani", "pulao")),
        IndianFoodItem("Chicken Biryani", "1 plate with 2 pcs (300g)", 490.0, 28.0, 52.0, 18.0, 3.0, MealType.DINNER, listOf("chicken biryani", "biryani")),
        IndianFoodItem("Chicken Curry (Home style)", "1 bowl with 2 pcs (180g)", 270.0, 24.0, 6.0, 16.5, 1.5, MealType.DINNER, listOf("chicken curry", "chicken")),

        // SNACKS & BEVERAGES
        IndianFoodItem("Roasted Makhana (Foxnuts)", "1 large bowl (30g)", 115.0, 3.2, 22.0, 1.5, 3.0, MealType.SNACK, listOf("makhana", "lotus seeds")),
        IndianFoodItem("Sprouted Moong Chaat", "1 bowl (120g with lemon & tomato)", 135.0, 9.5, 22.0, 1.2, 5.5, MealType.SNACK, listOf("sprouts", "moong chaat", "salad")),
        IndianFoodItem("Roasted Chana (Bhuna Chana)", "1 small fist (40g)", 140.0, 8.0, 22.0, 2.5, 5.0, MealType.SNACK, listOf("roasted chana", "bhuna chana")),
        IndianFoodItem("Samosa", "1 medium piece (80g)", 260.0, 4.0, 30.0, 14.5, 2.0, MealType.SNACK, listOf("samosa")),
        IndianFoodItem("Dhokla (Khaman)", "2 pieces (80g)", 150.0, 4.5, 24.0, 4.5, 2.0, MealType.SNACK, listOf("dhokla", "khaman")),
        IndianFoodItem("Bhel Puri", "1 small plate (100g)", 180.0, 4.2, 34.0, 3.5, 3.5, MealType.SNACK, listOf("bhel", "bhelpuri")),
        IndianFoodItem("Chaas (Spiced Buttermilk)", "1 tall glass (250ml)", 55.0, 3.0, 4.5, 2.5, 0.0, MealType.SNACK, listOf("chaas", "buttermilk", "mattha")),
        IndianFoodItem("Sweet Lassi", "1 medium glass (200ml)", 190.0, 5.0, 30.0, 6.0, 0.0, MealType.SNACK, listOf("lassi", "sweet lassi")),
        IndianFoodItem("Sattu Drink (Savory)", "1 glass (30g sattu in water)", 125.0, 7.5, 18.0, 2.0, 4.5, MealType.SNACK, listOf("sattu", "sattu sharbat")),
        IndianFoodItem("Cucumber & Tomato Salad", "1 plate with lemon", 35.0, 1.2, 7.0, 0.2, 2.5, MealType.LUNCH, listOf("salad", "kachumber")),
        IndianFoodItem("Gulab Jamun", "1 medium piece (45g)", 175.0, 2.2, 28.0, 6.5, 0.5, MealType.DINNER, listOf("gulab jamun", "sweet"))
    )

    fun findBestMatch(query: String): IndianFoodItem? {
        val q = query.lowercase().trim()
        if (q.isBlank()) return null

        // 1. Exact match on name
        items.find { it.name.lowercase() == q }?.let { return it }

        // 2. Exact match in aliases
        items.find { it.tags.any { tag -> tag.lowercase() == q } }?.let { return it }

        // 3. Substring match in name or tags
        items.find { it.name.lowercase().contains(q) || it.tags.any { tag -> tag.lowercase().contains(q) } }?.let { return it }

        // 4. Token overlap
        val qTokens = q.split(" ", ",", "&", "+").map { it.trim() }.filter { it.length > 2 }
        if (qTokens.isNotEmpty()) {
            val scored = items.map { item ->
                val allText = (item.name + " " + item.tags.joinToString(" ")).lowercase()
                val score = qTokens.count { token -> allText.contains(token) }
                Pair(item, score)
            }.filter { it.second > 0 }.maxByOrNull { it.second }

            if (scored != null && scored.second > 0) {
                return scored.first
            }
        }
        return null
    }

    fun parseQuantityAndItem(rawText: String): Pair<Double, IndianFoodItem?> {
        val text = rawText.lowercase().trim()
        // Extract leading numbers or written numbers (e.g., "2 rotis", "half plate biryani", "1.5 cup poha")
        var quantity = 1.0
        var cleaned = text

        val numberRegex = Regex("""^(\d+(?:\.\d+)?)\s*(?:x|pcs|pieces|piece|bowls|bowl|katoris|katori|cups|cup|plates|plate|servings|serving|nos)?\s*""", RegexOption.IGNORE_CASE)
        val match = numberRegex.find(text)
        if (match != null) {
            val numStr = match.groupValues[1]
            quantity = numStr.toDoubleOrNull() ?: 1.0
            cleaned = text.substring(match.range.last + 1).trim()
        } else if (text.startsWith("two ") || text.startsWith("2 ")) {
            quantity = 2.0
            cleaned = text.replaceFirst(Regex("""^(two|2)\s+"""), "")
        } else if (text.startsWith("three ") || text.startsWith("3 ")) {
            quantity = 3.0
            cleaned = text.replaceFirst(Regex("""^(three|3)\s+"""), "")
        } else if (text.startsWith("half ") || text.contains("half plate")) {
            quantity = 0.5
            cleaned = text.replace("half plate", "").replace("half", "").trim()
        }

        val item = findBestMatch(cleaned) ?: findBestMatch(text)
        return Pair(quantity, item)
    }

    /**
     * Splits a compound Indian meal description (e.g. "2 roti and 1 dal with dahi") into individual items.
     */
    fun parseMultiItems(rawText: String): List<Pair<Double, IndianFoodItem>> {
        val delimiters = Regex("[,+&]|\\band\\b|\\bwith\\b|\\n", RegexOption.IGNORE_CASE)
        val segments = rawText.split(delimiters).map { it.trim() }.filter { it.isNotBlank() }

        val results = mutableListOf<Pair<Double, IndianFoodItem>>()
        for (segment in segments) {
            val (qty, matched) = parseQuantityAndItem(segment)
            if (matched != null) {
                results.add(Pair(qty, matched))
            }
        }

        // If delimiter splitting found matches, return them
        if (results.isNotEmpty()) {
            return results
        }

        // Fallback to checking the whole text
        val (singleQty, singleMatch) = parseQuantityAndItem(rawText)
        if (singleMatch != null) {
            return listOf(Pair(singleQty, singleMatch))
        }

        return emptyList()
    }

    /**
     * Recommends Indian foods to help user meet their remaining daily calories and macros.
     */
    fun getSuggestedItemsForMacros(
        remainingCalories: Double,
        remainingProtein: Double,
        remainingCarbs: Double,
        remainingFat: Double,
        mealType: MealType
    ): List<IndianFoodItem> {
        val candidates = items.filter { item ->
            // Allow within remaining budget or slightly below
            item.calories <= (remainingCalories.coerceAtLeast(150.0) + 80.0)
        }

        // Score candidates based on macro alignment
        return candidates.sortedByDescending { item ->
            var score = 0.0
            // If protein is deficient, prioritize high protein/calorie ratio
            if (remainingProtein > 15.0 && item.protein >= 6.0) {
                score += (item.protein / (item.calories / 100.0)) * 5.0
            }
            // If carbs remaining is low, avoid carb bombs
            if (remainingCarbs < 30.0 && item.carbs > 35.0) {
                score -= 4.0
            }
            // Category alignment
            if (item.category == mealType) {
                score += 3.0
            }
            score
        }.take(4)
    }

    fun getDefaultQuickAddSuggestions(mealType: MealType): List<IndianFoodItem> {
        return items.filter { it.category == mealType }.take(4).ifEmpty {
            items.take(4)
        }
    }
}
