package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Meal
import com.example.model.MealType
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkPill
import com.example.ui.theme.NothingRed
import com.example.ui.theme.ProteinRed
import com.example.ui.theme.SpaceMonoFont
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextMuted

@Composable
fun MealListSection(
    meals: List<Meal>,
    onDeleteMeal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("meal_list_section")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(DarkPill)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TODAY'S LOG",
                    fontFamily = SpaceMonoFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextMuted
                )
            }
            Text(
                text = "${meals.size} ITEMS",
                fontFamily = SpaceMonoFont,
                fontSize = 10.sp,
                color = TextMuted
            )
        }

        if (meals.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(16.dp)),
                color = SurfaceWhite
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Fastfood,
                            contentDescription = null,
                            tint = TextBlack,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "NO MEALS LOGGED YET",
                        fontFamily = SpaceMonoFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = TextBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Type food or tap the camera icon in the bar below to analyze and log meals.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            MealType.entries.forEach { mealType ->
                val typeMeals = meals.filter { it.mealType.equals(mealType.name, ignoreCase = true) }
                if (typeMeals.isNotEmpty()) {
                    MealTypeGroup(
                        mealType = mealType,
                        meals = typeMeals,
                        onDeleteMeal = onDeleteMeal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MealTypeGroup(
    mealType: MealType,
    meals: List<Meal>,
    onDeleteMeal: (String) -> Unit
) {
    val totalKcal = meals.sumOf { it.calories }.toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${mealType.name} // $totalKcal KCAL",
                fontFamily = SpaceMonoFont,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = TextBlack
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            meals.forEach { meal ->
                MealCardItem(meal = meal, onDelete = { onDeleteMeal(meal.id) })
            }
        }
    }
}

@Composable
private fun MealCardItem(
    meal: Meal,
    onDelete: () -> Unit
) {
    val imageBitmap = remember(meal.imageUrl) {
        if (meal.imageUrl.isNotBlank()) {
            try {
                val bytes = Base64.decode(meal.imageUrl, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
            .testTag("meal_item_${meal.id}"),
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo thumbnail or cutlery icon
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = meal.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = TextBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Meal info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    maxLines = 1
                )
                val subText = if (meal.ingredientsSummary.isNotBlank()) meal.ingredientsSummary else meal.contextNotes
                if (subText.isNotBlank()) {
                    Text(
                        text = subText,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${meal.calories.toInt()} kcal",
                        fontFamily = SpaceMonoFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                    Text("•", fontSize = 10.sp, color = TextMuted)
                    Text("P ${meal.protein.toInt()}g", fontFamily = SpaceMonoFont, fontSize = 10.sp, color = ProteinRed, fontWeight = FontWeight.Bold)
                    Text("C ${meal.carbs.toInt()}g", fontFamily = SpaceMonoFont, fontSize = 10.sp, color = TextBlack)
                    Text("F ${meal.fat.toInt()}g", fontFamily = SpaceMonoFont, fontSize = 10.sp, color = TextMuted)
                }
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("delete_meal_${meal.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete meal",
                    tint = TextMuted,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
