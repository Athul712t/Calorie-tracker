package com.example.ui.food

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IndianFoodDatabase
import com.example.model.IndianFoodItem
import com.example.model.MealIngredient
import com.example.model.MealType
import com.example.ui.CalorieTrackerViewModel
import com.example.ui.kit.AppCard
import com.example.ui.kit.ChoiceChip
import com.example.ui.kit.MacroPillRow
import com.example.ui.kit.OutlineButton
import com.example.ui.kit.PrimaryButton
import com.example.ui.kit.RoundIconButton
import com.example.ui.kit.ScreenHeader
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppDimens
import com.example.ui.theme.DmSans
import java.util.Locale
import kotlin.math.roundToInt

private val TYPES = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.SNACK, MealType.DINNER)
private val TYPE_LABELS = listOf("Breakfast", "Lunch", "Snack", "Dinner")

private fun fmt(q: Double) = if (q % 1.0 == 0.0) q.toInt().toString() else String.format(Locale.getDefault(), "%.1f", q)

private fun IndianFoodItem.toIngredient(qty: Double) = MealIngredient(
    name = name, portion = portion, quantity = qty,
    unitCalories = calories, unitProtein = protein, unitCarbs = carbs, unitFat = fat, unitFiber = fiber
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    viewModel: CalorieTrackerViewModel,
    onBack: () -> Unit,
    onOpenCapture: () -> Unit,
    onDescribe: (String) -> Unit,
    onDone: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(MealType.fromHour(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY))) }
    var selected by remember { mutableStateOf<IndianFoodItem?>(null) }
    var describing by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    val added = remember { mutableStateListOf<MealIngredient>() }

    val needle = query.trim().lowercase()
    val results = remember(needle) {
        if (needle.isEmpty()) IndianFoodDatabase.items
        else IndianFoodDatabase.items.filter { f ->
            f.name.lowercase().contains(needle) || f.tags.any { it.lowercase().contains(needle) }
        }
    }

    Column(Modifier.fillMaxSize().background(AppColors.Background).statusBarsPadding()) {
        ScreenHeader("Add food", onBack = {
            onBack()
        })

        // search + camera
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppDimens.ScreenGutter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(26.dp)).background(AppColors.Surface)
                    .border(1.dp, AppColors.BorderStrong, RoundedCornerShape(26.dp)).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = AppColors.Muted)
                Box(Modifier.weight(1f).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) Text("Search food", fontFamily = DmSans, fontSize = 16.sp, color = Color(0xFF737E8F))
                    BasicTextField(
                        value = query, onValueChange = { query = it }, singleLine = true,
                        cursorBrush = SolidColor(AppColors.Ink),
                        textStyle = TextStyle(fontFamily = DmSans, fontSize = 16.sp, color = AppColors.Ink),
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Search food" }
                    )
                }
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Rounded.Close, contentDescription = "Clear search", tint = AppColors.Muted,
                        modifier = Modifier.clickable { query = "" }
                    )
                }
            }
            RoundIconButton(Icons.Rounded.CameraAlt, "Use the camera", onOpenCapture, size = 52.dp)
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = AppDimens.ScreenGutter, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TYPES.forEachIndexed { i, t -> ChoiceChip(TYPE_LABELS[i], type == t, { type = t }) }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = AppDimens.ScreenGutter, end = AppDimens.ScreenGutter, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Text(
                    if (needle.isEmpty()) "Common foods" else if (results.isEmpty()) "No results" else "Results",
                    fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )
            }
            if (results.isNotEmpty()) {
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        results.forEachIndexed { i, f ->
                            if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))
                            Row(
                                Modifier.fillMaxWidth().clickable { selected = f }.padding(horizontal = 16.dp, vertical = 14.dp)
                                    .semantics { contentDescription = "Add ${f.name}, ${f.portion}, ${f.calories.roundToInt()} kilocalories" },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(f.name, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AppColors.Ink)
                                    Text("${f.portion} · ${f.calories.roundToInt()} kcal", fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted)
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        "No match for “$query”. Describe it in your own words and the AI will estimate it.",
                        fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted, modifier = Modifier.padding(4.dp)
                    )
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                if (!describing) {
                    OutlineButton("Can’t find it? Describe it", onClick = { describing = true }, modifier = Modifier.fillMaxWidth())
                } else {
                    Row(
                        Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(28.dp)).background(AppColors.Surface)
                            .border(1.dp, AppColors.BorderStrong, RoundedCornerShape(28.dp)).padding(start = 20.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f)) {
                            if (description.isEmpty()) Text(
                                "Two parathas with butter and a glass of lassi",
                                fontFamily = DmSans, fontSize = 14.sp, color = Color(0xFF737E8F), maxLines = 1
                            )
                            BasicTextField(
                                value = description, onValueChange = { description = it }, singleLine = true,
                                cursorBrush = SolidColor(AppColors.Ink),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                textStyle = TextStyle(fontFamily = DmSans, fontSize = 16.sp, color = AppColors.Ink),
                                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Describe your meal" }
                            )
                        }
                        Box(
                            Modifier.height(40.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Ink)
                                .clickable(enabled = description.isNotBlank()) { onDescribe(description.trim()) }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Estimate", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White) }
                    }
                }
            }
        }

        if (added.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().background(AppColors.Surface).navigationBarsPadding().imePadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${added.size} ${if (added.size == 1) "item" else "items"} added",
                        fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Ink
                    )
                    Text(
                        "${added.sumOf { it.totalCalories }.roundToInt()} kcal",
                        fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted
                    )
                }
                PrimaryButton("Done", onClick = {
                    viewModel.startMealFromItems(added.toList(), type)
                    onDone()
                }, modifier = Modifier.width(120.dp))
            }
        }
    }

    selected?.let { food ->
        var qtyText by remember(food) { mutableStateOf("1") }
        val qty = qtyText.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 && it <= 50 }
        ModalBottomSheet(
            onDismissRequest = { selected = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = AppColors.Surface,
            shape = RoundedCornerShape(topStart = AppDimens.SheetRadius, topEnd = AppDimens.SheetRadius)
        ) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
                Text(food.name, fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppColors.Ink)
                Text("1 serving = ${food.portion}", fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted)
                Spacer(Modifier.height(18.dp))
                Text("Servings", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.Muted)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.width(88.dp).height(48.dp).clip(RoundedCornerShape(16.dp)).background(AppColors.Chip).padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = qtyText, onValueChange = { qtyText = it }, singleLine = true,
                            cursorBrush = SolidColor(AppColors.Ink),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink),
                            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Number of servings" }
                        )
                    }
                    listOf(0.5, 1.0, 1.5, 2.0).forEach { v ->
                        ChoiceChip("${fmt(v)}×", qty == v, { qtyText = fmt(v) })
                    }
                }
                Spacer(Modifier.height(16.dp))
                val q = qty ?: 0.0
                Text("${(food.calories * q).roundToInt()} kcal", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = AppColors.Ink)
                Spacer(Modifier.height(8.dp))
                MacroPillRow((food.protein * q).roundToInt(), (food.carbs * q).roundToInt(), (food.fat * q).roundToInt())
                Spacer(Modifier.height(20.dp))
                PrimaryButton(
                    "Add to ${TYPE_LABELS[TYPES.indexOf(type)]}",
                    enabled = qty != null,
                    onClick = {
                        added.add(food.toIngredient(qty!!))
                        selected = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
