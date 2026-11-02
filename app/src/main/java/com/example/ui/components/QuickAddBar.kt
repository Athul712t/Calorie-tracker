package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IndianFoodDatabase
import com.example.model.IndianFoodItem
import com.example.model.MenuItem
import com.example.model.MealType
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DarkPillText
import com.example.ui.theme.NothingRed
import com.example.ui.theme.SpaceMonoFont
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextMuted
import java.util.Calendar

@Composable
fun QuickAddBar(
    menuItems: List<MenuItem>,
    onQuickAddMenuItem: (MenuItem, Double) -> Unit,
    onQuickAddIndianFood: (IndianFoodItem, Double) -> Unit,
    onOpenFullMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val currentMealType = MealType.fromHour(currentHour)

    val userMatchingItems = menuItems.filter { it.category == currentMealType.name }
        .sortedByDescending { it.frequencyCount }

    val defaultSuggestions = IndianFoodDatabase.getDefaultQuickAddSuggestions(currentMealType)

    val timeLabel = when (currentMealType) {
        MealType.BREAKFAST -> "BREAKFAST"
        MealType.LUNCH -> "LUNCH"
        MealType.SNACK -> "TEA & SNACKS"
        MealType.DINNER -> "DINNER"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .testTag("quick_add_bar")
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
                        .background(NothingRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$timeLabel // FREQUENT",
                    fontFamily = SpaceMonoFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextMuted
                )
            }

            Text(
                text = "VIEW MENU",
                fontFamily = SpaceMonoFont,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = TextBlack,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onOpenFullMenuClick() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("open_menu_button")
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Learned frequent items
            userMatchingItems.take(4).forEach { item ->
                QuickAddItemChip(
                    name = item.name,
                    calories = item.calories.toInt(),
                    portion = item.defaultPortion,
                    isFromHistory = true,
                    onAddOne = { onQuickAddMenuItem(item, 1.0) },
                    onAddTwo = { onQuickAddMenuItem(item, 2.0) }
                )
            }

            // Default Indian food options
            defaultSuggestions.filter { def -> userMatchingItems.none { it.name.equals(def.name, ignoreCase = true) } }
                .take(4 - userMatchingItems.size.coerceAtMost(2))
                .forEach { food ->
                    QuickAddItemChip(
                        name = food.name,
                        calories = food.calories.toInt(),
                        portion = food.portion,
                        isFromHistory = false,
                        onAddOne = { onQuickAddIndianFood(food, 1.0) },
                        onAddTwo = { onQuickAddIndianFood(food, 2.0) }
                    )
                }
        }
    }
}

@Composable
private fun QuickAddItemChip(
    name: String,
    calories: Int,
    portion: String,
    isFromHistory: Boolean,
    onAddOne: () -> Unit,
    onAddTwo: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
            .testTag("quick_add_chip_${name.replace(" ", "_")}"),
        color = SurfaceWhite
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = TextBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$calories kcal",
                    fontFamily = SpaceMonoFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
                if (isFromHistory) {
                    Text(
                        text = "FREQUENT",
                        fontFamily = SpaceMonoFont,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingRed
                    )
                }
            }
            Text(
                text = portion,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Minimal +1 and +2 buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledTonalButton(
                    onClick = onAddOne,
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SurfaceElevated,
                        contentColor = TextBlack
                    )
                ) {
                    Text("+1", fontFamily = SpaceMonoFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onAddTwo,
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkPill,
                        contentColor = DarkPillText
                    )
                ) {
                    Text("+2", fontFamily = SpaceMonoFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
