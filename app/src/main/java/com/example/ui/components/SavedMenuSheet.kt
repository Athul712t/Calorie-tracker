package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IndianFoodDatabase
import com.example.model.MenuItem
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMenuSheet(
    menuItems: List<MenuItem>,
    onQuickAdd: (MenuItem, Double) -> Unit,
    onAddCustomItem: (name: String, calories: Double, protein: Double, carbs: Double, fat: Double, portion: String, category: MealType) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>("ALL") }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val combinedItems = remember(menuItems, searchQuery, selectedCategory) {
        val userItemsMapped = menuItems.map { it }
        val databaseItemsMapped = IndianFoodDatabase.items
            .filter { dbItem -> menuItems.none { it.name.equals(dbItem.name, ignoreCase = true) } }
            .map { dbItem ->
                MenuItem(
                    id = "db_${dbItem.name}",
                    name = dbItem.name,
                    calories = dbItem.calories,
                    protein = dbItem.protein,
                    carbs = dbItem.carbs,
                    fat = dbItem.fat,
                    fiber = dbItem.fiber,
                    defaultPortion = dbItem.portion,
                    category = dbItem.category.name,
                    frequencyCount = 0L
                )
            }

        val all = userItemsMapped + databaseItemsMapped
        all.filter { item ->
            val matchesCategory = selectedCategory == "ALL" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        modifier = Modifier.testTag("saved_menu_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FOOD MENU & QUICK ADD",
                        fontFamily = SpaceMonoFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextBlack
                    )
                    Text(
                        text = "Tap +1 or +2 to immediately log to today",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                FilledTonalButton(
                    onClick = { showAddCustomDialog = true },
                    modifier = Modifier.testTag("add_custom_food_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkPill,
                        contentColor = DarkPillText
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD CUSTOM", fontFamily = SpaceMonoFont, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search roti, dal, dosa, chai, paneer...", fontSize = 12.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("menu_search_input"),
                shape = RoundedCornerShape(12.dp),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val categories = listOf("ALL", "BREAKFAST", "LUNCH", "SNACK", "DINNER")
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = cat,
                                fontFamily = SpaceMonoFont,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkPill,
                            selectedLabelColor = DarkPillText,
                            containerColor = SurfaceElevated,
                            labelColor = TextBlack
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == cat,
                            borderColor = BorderLight,
                            selectedBorderColor = DarkPill
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Menu Items List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(combinedItems) { item ->
                    MenuItemRow(
                        item = item,
                        onAddOne = { onQuickAdd(item, 1.0) },
                        onAddTwo = { onQuickAdd(item, 2.0) }
                    )
                }
            }
        }
    }

    if (showAddCustomDialog) {
        AddCustomFoodDialog(
            onConfirm = { name, cal, p, c, f, portion, cat ->
                onAddCustomItem(name, cal, p, c, f, portion, cat)
                showAddCustomDialog = false
            },
            onDismiss = { showAddCustomDialog = false }
        )
    }
}

@Composable
private fun MenuItemRow(
    item: MenuItem,
    onAddOne: () -> Unit,
    onAddTwo: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
                Text(
                    text = "${item.defaultPortion} • ${item.calories.toInt()} kcal",
                    fontFamily = SpaceMonoFont,
                    fontSize = 11.sp,
                    color = TextBlack,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "P ${item.protein.toInt()}g • C ${item.carbs.toInt()}g • F ${item.fat.toInt()}g",
                    fontFamily = SpaceMonoFont,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            // Quick add buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(
                    onClick = onAddOne,
                    modifier = Modifier
                        .size(width = 46.dp, height = 32.dp)
                        .testTag("add_1_${item.name.replace(" ", "_")}"),
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
                        .size(width = 46.dp, height = 32.dp)
                        .testTag("add_2_${item.name.replace(" ", "_")}"),
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

@Composable
private fun AddCustomFoodDialog(
    onConfirm: (name: String, calories: Double, protein: Double, carbs: Double, fat: Double, portion: String, category: MealType) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }
    var portion by remember { mutableStateOf("1 serving") }
    var selectedCategory by remember { mutableStateOf(MealType.LUNCH) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = { Text("ADD CUSTOM FOOD", fontFamily = SpaceMonoFont, color = TextBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Food name (e.g. Oats Cheela)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = portion,
                    onValueChange = { portion = it },
                    placeholder = { Text("Portion (e.g. 2 pieces, 1 bowl)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = caloriesStr,
                        onValueChange = { caloriesStr = it },
                        placeholder = { Text("Calories") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it },
                        placeholder = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = carbsStr,
                        onValueChange = { carbsStr = it },
                        placeholder = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = fatStr,
                        onValueChange = { fatStr = it },
                        placeholder = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name,
                            caloriesStr.toDoubleOrNull() ?: 100.0,
                            proteinStr.toDoubleOrNull() ?: 2.0,
                            carbsStr.toDoubleOrNull() ?: 15.0,
                            fatStr.toDoubleOrNull() ?: 2.0,
                            portion,
                            selectedCategory
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkPill, contentColor = DarkPillText),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("SAVE TO MENU", fontFamily = SpaceMonoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("CANCEL", fontFamily = SpaceMonoFont, color = TextMuted, fontSize = 11.sp)
            }
        }
    )
}
