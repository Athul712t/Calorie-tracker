package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserGoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DarkPillText
import com.example.ui.theme.ProteinRed
import com.example.ui.theme.SpaceMonoFont
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextMuted

@Composable
fun GoalSettingsDialog(
    currentGoal: UserGoal,
    onSaveGoals: (calorie: Long, protein: Long, carbs: Long, fat: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var calorieStr by remember { mutableStateOf(currentGoal.calorieGoal.toString()) }
    var proteinStr by remember { mutableStateOf(currentGoal.proteinGoal.toString()) }
    var carbsStr by remember { mutableStateOf(currentGoal.carbsGoal.toString()) }
    var fatStr by remember { mutableStateOf(currentGoal.fatGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("goal_settings_dialog"),
        containerColor = SurfaceWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = TextBlack,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SET DAILY TARGETS",
                    fontFamily = SpaceMonoFont,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextBlack
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("QUICK PRESETS", fontFamily = SpaceMonoFont, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = TextMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            calorieStr = "2000"
                            proteinStr = "100"
                            carbsStr = "220"
                            fatStr = "55"
                        },
                        label = { Text("BALANCED", fontFamily = SpaceMonoFont, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = SurfaceElevated, labelColor = TextBlack),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = BorderLight)
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            calorieStr = "2100"
                            proteinStr = "140"
                            carbsStr = "180"
                            fatStr = "50"
                        },
                        label = { Text("HIGH PROTEIN", fontFamily = SpaceMonoFont, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = SurfaceElevated, labelColor = TextBlack),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = BorderLight)
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            calorieStr = "1600"
                            proteinStr = "110"
                            carbsStr = "140"
                            fatStr = "40"
                        },
                        label = { Text("DEFICIT", fontFamily = SpaceMonoFont, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = SurfaceElevated, labelColor = TextBlack),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = BorderLight)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Calorie Target
                Text("CALORIE GOAL (KCAL)", fontFamily = SpaceMonoFont, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                OutlinedTextField(
                    value = calorieStr,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) calorieStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_calorie_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkPill,
                        unfocusedBorderColor = BorderLight,
                        focusedTextColor = TextBlack,
                        unfocusedTextColor = TextBlack,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    )
                )

                // Macros
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoalMacroInput(label = "PROTEIN (G)", value = proteinStr, onValueChange = { proteinStr = it }, color = ProteinRed, modifier = Modifier.weight(1f))
                    GoalMacroInput(label = "CARBS (G)", value = carbsStr, onValueChange = { carbsStr = it }, color = TextBlack, modifier = Modifier.weight(1f))
                    GoalMacroInput(label = "FAT (G)", value = fatStr, onValueChange = { fatStr = it }, color = TextMuted, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = calorieStr.toLongOrNull() ?: 2000L
                    val p = proteinStr.toLongOrNull() ?: 100L
                    val c = carbsStr.toLongOrNull() ?: 220L
                    val f = fatStr.toLongOrNull() ?: 55L
                    onSaveGoals(cal, p, c, f)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_goals_button"),
                colors = ButtonDefaults.buttonColors(containerColor = DarkPill, contentColor = DarkPillText),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("SAVE TARGETS", fontFamily = SpaceMonoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("CANCEL", fontFamily = SpaceMonoFont, fontSize = 11.sp)
            }
        }
    )
}

@Composable
private fun GoalMacroInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, fontFamily = SpaceMonoFont, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.all { ch -> ch.isDigit() }) onValueChange(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkPill,
                unfocusedBorderColor = BorderLight,
                focusedTextColor = TextBlack,
                unfocusedTextColor = TextBlack,
                focusedContainerColor = SurfaceElevated,
                unfocusedContainerColor = SurfaceElevated
            ),
            singleLine = true
        )
    }
}
