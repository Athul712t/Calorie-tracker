package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalorieUiState
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CarbsCharcoal
import com.example.ui.theme.DarkPill
import com.example.ui.theme.FatSlate
import com.example.ui.theme.NothingRed
import com.example.ui.theme.ProteinRed
import com.example.ui.theme.SpaceMonoFont
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextMuted

@Composable
fun CalorieMacroSummaryCard(
    state: CalorieUiState,
    onEditGoalsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calorieProgress by animateFloatAsState(targetValue = state.calorieProgress, label = "calProgress")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .testTag("calorie_summary_card"),
        color = SurfaceWhite
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Top row: Tag and Goal trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(NothingRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DAILY BUDGET",
                        fontFamily = SpaceMonoFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = TextMuted
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .clickable { onEditGoalsClick() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("edit_goals_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Edit Goals",
                        tint = TextBlack,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GOALS",
                        fontFamily = SpaceMonoFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = TextBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Giant Calorie Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${state.remainingCalories.toInt()}",
                        fontFamily = SpaceMonoFont,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 48.sp,
                        color = TextBlack
                    )
                    Text(
                        text = "KCAL REMAINING",
                        fontFamily = SpaceMonoFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = TextMuted
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Consumed: ${state.totalCalories.toInt()} kcal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextBlack
                    )
                    Text(
                        text = "Target: ${state.goal.calorieGoal} kcal",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Minimal linear calorie track
            LinearProgressIndicator(
                progress = { calorieProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (state.totalCalories > state.goal.calorieGoal) NothingRed else DarkPill,
                trackColor = SurfaceElevated,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Linear Macro Meters (Protein, Carbs, Fats)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MinimalMacroItem(
                    label = "PROTEIN",
                    consumed = state.totalProtein,
                    goal = state.goal.proteinGoal.toDouble(),
                    remaining = state.remainingProtein,
                    color = ProteinRed,
                    modifier = Modifier.weight(1f)
                )
                MinimalMacroItem(
                    label = "CARBS",
                    consumed = state.totalCarbs,
                    goal = state.goal.carbsGoal.toDouble(),
                    remaining = state.remainingCarbs,
                    color = CarbsCharcoal,
                    modifier = Modifier.weight(1f)
                )
                MinimalMacroItem(
                    label = "FATS",
                    consumed = state.totalFat,
                    goal = state.goal.fatGoal.toDouble(),
                    remaining = state.remainingFat,
                    color = FatSlate,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MinimalMacroItem(
    label: String,
    consumed: Double,
    goal: Double,
    remaining: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) (consumed / goal).toFloat().coerceIn(0f, 1f) else 0f

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
        color = SurfaceElevated
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontFamily = SpaceMonoFont,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = color
                )
                Text(
                    text = "${remaining.toInt()}g left",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = SurfaceWhite,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${consumed.toInt()} / ${goal.toInt()}g",
                fontFamily = SpaceMonoFont,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )
        }
    }
}
