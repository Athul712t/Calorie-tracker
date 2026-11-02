package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.IndianFoodItem
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DarkPillText
import com.example.ui.theme.NothingRed
import com.example.ui.theme.ProteinRed
import com.example.ui.theme.SpaceMonoFont
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextMuted

@Composable
fun IndianFoodSuggestionsSection(
    suggestions: List<IndianFoodItem>,
    remainingCalories: Double,
    remainingProtein: Double,
    isGenerating: Boolean,
    onRefresh: () -> Unit,
    onQuickAdd: (IndianFoodItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty() && !isGenerating) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("indian_suggestions_section")
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
                Column {
                    Text(
                        text = "SUGGESTIONS // FILL GAP",
                        fontFamily = SpaceMonoFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "${remainingCalories.toInt()} kcal & ${remainingProtein.toInt()}g protein needed",
                        fontSize = 11.sp,
                        color = TextBlack
                    )
                }
            }

            IconButton(
                onClick = onRefresh,
                enabled = !isGenerating,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("refresh_suggestions_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = TextBlack,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Indian food suggestions",
                        tint = TextBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            suggestions.forEach { food ->
                SuggestionCard(
                    food = food,
                    onAdd = { onQuickAdd(food) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    food: IndianFoodItem,
    onAdd: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
            .testTag("suggestion_card_${food.name.replace(" ", "_")}"),
        color = SurfaceWhite
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${food.calories.toInt()} kcal",
                    fontFamily = SpaceMonoFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FITS GOAL",
                        fontFamily = SpaceMonoFont,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProteinRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = food.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack,
                maxLines = 1
            )
            Text(
                text = food.portion,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("P: ${food.protein.toInt()}g", fontFamily = SpaceMonoFont, fontSize = 10.sp, color = ProteinRed, fontWeight = FontWeight.Bold)
                Text("•", fontSize = 8.sp, color = TextMuted)
                Text("C: ${food.carbs.toInt()}g", fontFamily = SpaceMonoFont, fontSize = 10.sp, color = TextBlack, fontWeight = FontWeight.Normal)
                Text("•", fontSize = 8.sp, color = TextMuted)
                Text("F: ${food.fat.toInt()}g", fontFamily = SpaceMonoFont, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Normal)
            }

            Spacer(modifier = Modifier.height(10.dp))

            FilledTonalButton(
                onClick = onAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = DarkPill,
                    contentColor = DarkPillText
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("QUICK ADD", fontFamily = SpaceMonoFont, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
