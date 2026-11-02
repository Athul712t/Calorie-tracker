package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MealIngredient
import com.example.model.MealParsedResult
import com.example.model.MealType
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
fun MealConfirmationDialog(
    initialData: MealParsedResult,
    onConfirm: (MealParsedResult) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialData.name) }
    var selectedMealType by remember { mutableStateOf(initialData.mealType) }
    var servings by remember { mutableDoubleStateOf(initialData.servings) }

    var ingredients by remember(initialData.ingredients) { mutableStateOf(initialData.ingredients) }
    var manualEditMode by remember { mutableStateOf(initialData.ingredients.isEmpty()) }

    var caloriesStr by remember { mutableStateOf(initialData.calories.toInt().toString()) }
    var proteinStr by remember { mutableStateOf(initialData.protein.toInt().toString()) }
    var carbsStr by remember { mutableStateOf(initialData.carbs.toInt().toString()) }
    var fatStr by remember { mutableStateOf(initialData.fat.toInt().toString()) }
    var fiberStr by remember { mutableStateOf(initialData.fiber.toInt().toString()) }
    var notes by remember { mutableStateOf(initialData.notes) }

    // When ingredients change, keep displayed total strings updated
    LaunchedEffect(ingredients) {
        if (ingredients.isNotEmpty() && !manualEditMode) {
            caloriesStr = ingredients.sumOf { it.totalCalories }.toInt().toString()
            proteinStr = String.format("%.1f", ingredients.sumOf { it.totalProtein })
            carbsStr = String.format("%.1f", ingredients.sumOf { it.totalCarbs })
            fatStr = String.format("%.1f", ingredients.sumOf { it.totalFat })
            fiberStr = String.format("%.1f", ingredients.sumOf { it.totalFiber })
        }
    }

    val imageBitmap = remember(initialData.imageBase64) {
        if (!initialData.imageBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(initialData.imageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("meal_confirmation_dialog"),
        containerColor = SurfaceWhite,
        title = {
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
                        text = "MEAL VERIFICATION",
                        fontFamily = SpaceMonoFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextBlack
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Source & Standard Badge
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (initialData.isAiGenerated) NothingRed else TextBlack)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (initialData.isAiGenerated) "ICMR-NIN IFCT • GEMINI AI" else "ICMR-NIN IFCT • VERIFIED DB",
                                fontFamily = SpaceMonoFont,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextBlack
                            )
                        }
                        Text(
                            text = initialData.confidence.uppercase(),
                            fontFamily = SpaceMonoFont,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Photo preview if available
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Food photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Food Name Field
                Text("FOOD / DISH NAME", fontFamily = SpaceMonoFont, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_food_name_input"),
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

                Spacer(modifier = Modifier.height(10.dp))

                // Meal Category
                Text("MEAL TYPE", fontFamily = SpaceMonoFont, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedMealType == type,
                            onClick = { selectedMealType = type },
                            label = { Text(type.name, fontFamily = SpaceMonoFont, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkPill,
                                selectedLabelColor = DarkPillText,
                                containerColor = SurfaceElevated,
                                labelColor = TextBlack
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedMealType == type,
                                borderColor = BorderLight,
                                selectedBorderColor = DarkPill
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ITEMIZED INGREDIENT BREAKDOWN SECTION
                if (ingredients.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ITEMIZED INGREDIENTS (${ingredients.size})",
                            fontFamily = SpaceMonoFont,
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (manualEditMode) "Edit Items" else "Auto-Summed",
                            fontFamily = SpaceMonoFont,
                            fontSize = 9.sp,
                            color = NothingRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ingredients.forEachIndexed { index, ing ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ingredient_item_$index"),
                                color = SurfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ing.name,
                                            fontFamily = SpaceMonoFont,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextBlack
                                        )
                                        Text(
                                            text = "${ing.portion} • ${ing.unitCalories.toInt()} kcal, ${ing.unitProtein.toInt()}g P",
                                            fontSize = 9.sp,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "= ${ing.totalCalories.toInt()} kcal • ${String.format("%.1f", ing.totalProtein)}g P",
                                            fontFamily = SpaceMonoFont,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ProteinRed
                                        )
                                    }

                                    // Stepper for quantity
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (ing.quantity > 0.5) {
                                                    val newQty = if (ing.quantity > 1.0) ing.quantity - 1.0 else 0.5
                                                    ingredients = ingredients.toMutableList().also {
                                                        it[index] = ing.copy(quantity = newQty)
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(SurfaceWhite)
                                                .border(1.dp, BorderLight, CircleShape)
                                                .testTag("decrease_ingredient_$index")
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextBlack, modifier = Modifier.size(12.dp))
                                        }

                                        Text(
                                            text = if (ing.quantity % 1.0 == 0.0) "${ing.quantity.toInt()}x" else String.format("%.1fx", ing.quantity),
                                            fontFamily = SpaceMonoFont,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextBlack,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                val newQty = ing.quantity + (if (ing.quantity < 1.0) 0.5 else 1.0)
                                                ingredients = ingredients.toMutableList().also {
                                                    it[index] = ing.copy(quantity = newQty)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(SurfaceWhite)
                                                .border(1.dp, BorderLight, CircleShape)
                                                .testTag("increase_ingredient_$index")
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextBlack, modifier = Modifier.size(12.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                ingredients = ingredients.toMutableList().also { it.removeAt(index) }
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("delete_ingredient_$index")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Delete item", tint = TextMuted, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    // Servings Stepper for non-itemized meal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SERVINGS", fontFamily = SpaceMonoFont, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (servings > 0.5) {
                                        val newServings = servings - 0.5
                                        val factor = newServings / servings
                                        servings = newServings
                                        caloriesStr = ((caloriesStr.toDoubleOrNull() ?: 0.0) * factor).toInt().toString()
                                        proteinStr = ((proteinStr.toDoubleOrNull() ?: 0.0) * factor).toInt().toString()
                                        carbsStr = ((carbsStr.toDoubleOrNull() ?: 0.0) * factor).toInt().toString()
                                        fatStr = ((fatStr.toDoubleOrNull() ?: 0.0) * factor).toInt().toString()
                                    }
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextBlack, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = String.format("%.1fx", servings),
                                fontFamily = SpaceMonoFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextBlack,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            IconButton(
                                onClick = {
                                    val newServings = servings + 0.5
                                    val factor = newServings / servings
                                    servings = newServings
                                    caloriesStr = ((caloriesStr.toDoubleOrNull() ?: 0.0) * factor).toInt().toString()
                                    proteinStr = ((proteinStr.toDoubleOrNull() ?: 0.0) * factor).toInt().toString()
                                    carbsStr = ((carbsStr.toDoubleOrNull() ?: 0.0) * factor).toInt().toString()
                                    fatStr = ((fatStr.toDoubleOrNull() ?: 0.0) * factor).toInt().toString()
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextBlack, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Summary Totals Card / Editable Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL NUTRITION (VERIFIED)",
                        fontFamily = SpaceMonoFont,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextMuted
                    )
                    Text(
                        text = if (manualEditMode) "Lock to Auto" else "Manual Edit",
                        fontFamily = SpaceMonoFont,
                        fontSize = 9.sp,
                        color = TextBlack,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { manualEditMode = !manualEditMode }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NutrientInputField(
                        label = "CALORIES (KCAL)",
                        value = caloriesStr,
                        onValueChange = {
                            caloriesStr = it
                            manualEditMode = true
                        },
                        color = TextBlack,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientInputField(
                        label = "PROTEIN (G)",
                        value = proteinStr,
                        onValueChange = {
                            proteinStr = it
                            manualEditMode = true
                        },
                        color = ProteinRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NutrientInputField(
                        label = "CARBS (G)",
                        value = carbsStr,
                        onValueChange = {
                            carbsStr = it
                            manualEditMode = true
                        },
                        color = TextBlack,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientInputField(
                        label = "FAT (G)",
                        value = fatStr,
                        onValueChange = {
                            fatStr = it
                            manualEditMode = true
                        },
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientInputField(
                        label = "FIBER (G)",
                        value = fiberStr,
                        onValueChange = {
                            fiberStr = it
                            manualEditMode = true
                        },
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("PORTION / IFCT NOTES", fontFamily = SpaceMonoFont, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(notes, fontSize = 11.sp, color = TextBlack)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalResult = MealParsedResult(
                        name = name.ifBlank { initialData.name },
                        mealType = selectedMealType,
                        calories = caloriesStr.toDoubleOrNull() ?: initialData.calories,
                        protein = proteinStr.toDoubleOrNull() ?: initialData.protein,
                        carbs = carbsStr.toDoubleOrNull() ?: initialData.carbs,
                        fat = fatStr.toDoubleOrNull() ?: initialData.fat,
                        fiber = fiberStr.toDoubleOrNull() ?: initialData.fiber,
                        servings = servings,
                        notes = notes,
                        confidence = initialData.confidence,
                        imageBase64 = initialData.imageBase64,
                        ingredients = ingredients,
                        isAiGenerated = initialData.isAiGenerated,
                        rawQuery = initialData.rawQuery
                    )
                    onConfirm(finalResult)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("confirm_and_save_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkPill,
                    contentColor = DarkPillText
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CONFIRM & LOG MEAL", fontFamily = SpaceMonoFont, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cancel_confirmation_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CANCEL", fontFamily = SpaceMonoFont, fontSize = 11.sp)
            }
        }
    )
}

@Composable
private fun NutrientInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontFamily = SpaceMonoFont,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) onValueChange(it) },
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
            textStyle = TextStyle(fontFamily = SpaceMonoFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextBlack),
            singleLine = true
        )
    }
}
