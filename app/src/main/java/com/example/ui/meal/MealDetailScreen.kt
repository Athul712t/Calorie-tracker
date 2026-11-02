package com.example.ui.meal

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MealIngredient
import com.example.model.MealType
import com.example.ui.CalorieTrackerViewModel
import com.example.ui.kit.AppCard
import com.example.ui.kit.MacroPillRow
import com.example.ui.kit.PrimaryButton
import com.example.ui.kit.RoundIconButton
import com.example.ui.kit.ScreenHeader
import com.example.ui.kit.rememberBase64Image
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppDimens
import com.example.ui.theme.DmSans
import java.util.Locale
import kotlin.math.roundToInt

private data class ChatLine(val fromUser: Boolean, val text: String, val isError: Boolean = false)

/**
 * Shows the meal in `pendingMeal` (new from AI/search, or a saved meal opened for editing).
 * Items can be changed with − / + and delete, or by typing a change in the chat.
 */
@Composable
fun MealDetailScreen(
    viewModel: CalorieTrackerViewModel,
    onClose: () -> Unit,
    onDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val meal = state.pendingMeal
    var chatOpen by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    val lines = remember { mutableStateListOf<ChatLine>() }
    val isSaved = state.editingMealId != null

    // The meal can disappear (last item deleted, or discarded). Then there is nothing to show.
    LaunchedEffect(meal == null) { if (meal == null) onClose() }
    if (meal == null) return

    val image = rememberBase64Image(meal.imageBase64)
    val send: () -> Unit = {
        val t = draft.trim()
        if (t.isNotEmpty() && !state.chatBusy) {
            draft = ""
            lines.add(ChatLine(true, t))
            viewModel.applyChatEdit(t) { ok, reply ->
                lines.add(ChatLine(false, reply, isError = !ok))
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(AppColors.Background).statusBarsPadding()
    ) {
        ScreenHeader(
            title = if (isSaved) "Edit meal" else "Meal",
            onBack = {
                viewModel.discardPendingMeal()
                onClose()
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = AppDimens.ScreenGutter, end = AppDimens.ScreenGutter, bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (image != null) {
                item {
                    Image(
                        image, contentDescription = "Photo of ${meal.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(AppDimens.CardRadius))
                    )
                }
            }
            item {
                Text(meal.name, style = MaterialTheme.typography.headlineMedium, color = AppColors.Ink)
                if (!isSaved) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (meal.isAiGenerated) "AI estimate · confidence ${meal.confidence}" else "From food search",
                        fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted
                    )
                }
            }
            item { TypeAndPortionRow(meal.mealType, meal.servings, viewModel) }
            item { TotalsCard(meal.calories, meal.protein, meal.carbs, meal.fat) }
            item {
                Text(
                    "Items", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    color = AppColors.Ink, modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
            itemsIndexed(meal.ingredients) { index, ing ->
                ItemRow(
                    ing,
                    onMinus = { viewModel.updateIngredientQuantity(index, stepDown(ing.quantity)) },
                    onPlus = { viewModel.updateIngredientQuantity(index, ing.quantity + step(ing.quantity)) },
                    onDelete = { viewModel.removeIngredient(index) }
                )
            }
            if (isSaved) {
                item {
                    Text(
                        "Delete this meal",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.deleteEditingMeal(onDone) }
                            .padding(vertical = 14.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                        color = AppColors.AmberDeep
                    )
                }
            }
        }

        if (chatOpen) {
            ChatPanel(lines = lines, busy = state.chatBusy)
        }
        state.errorMessage?.let {
            Text(
                it, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                fontFamily = DmSans, fontSize = 13.sp, color = AppColors.AmberDeep
            )
        }

        // bottom row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Background)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (chatOpen) {
                ChatInput(draft, { draft = it }, send, enabled = !state.chatBusy)
                Spacer(Modifier.height(10.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoundIconButton(
                    if (chatOpen) Icons.Rounded.Close else Icons.Rounded.ChatBubbleOutline,
                    if (chatOpen) "Close chat" else "Change this meal by chatting",
                    { chatOpen = !chatOpen },
                    size = 56.dp
                )
                PrimaryButton(
                    if (isSaved) "Save changes" else "Done",
                    onClick = { viewModel.saveMealFromPending(onDone) },
                    enabled = !state.isLoading && !state.chatBusy,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun step(q: Double): Double = if (q >= 4.0) 1.0 else 0.5
private fun stepDown(q: Double): Double = (q - (if (q > 4.0) 1.0 else 0.5)).coerceAtLeast(0.5)

private fun fmtQty(q: Double): String =
    if (q % 1.0 == 0.0) q.toInt().toString() else String.format(Locale.getDefault(), "%.1f", q)

@Composable
private fun TypeAndPortionRow(type: MealType, servings: Double, vm: CalorieTrackerViewModel) {
    var menu by remember { mutableStateOf(false) }
    var portionText by remember(servings) { mutableStateOf(fmtQty(servings)) }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.Surface)
                    .border(1.dp, AppColors.BorderStrong, RoundedCornerShape(16.dp))
                    .clickable { menu = true }
                    .padding(horizontal = 16.dp)
                    .semantics { contentDescription = "Meal type, ${type.displayName}" },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(type.displayName, modifier = Modifier.weight(1f), fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AppColors.Ink)
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = AppColors.Muted)
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                MealType.values().forEach { t ->
                    DropdownMenuItem(
                        text = { Text(t.displayName, fontFamily = DmSans) },
                        onClick = { vm.setPendingMealType(t); menu = false }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .width(140.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.Surface)
                .border(1.dp, AppColors.BorderStrong, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = portionText,
                onValueChange = {
                    portionText = it
                    it.replace(',', '.').toDoubleOrNull()?.takeIf { v -> v > 0 }?.let { v -> vm.setPendingPortion(v) }
                },
                singleLine = true,
                cursorBrush = SolidColor(AppColors.Ink),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AppColors.Ink),
                modifier = Modifier.weight(1f).semantics { contentDescription = "Portion multiplier" }
            )
            Text("× portion", fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted)
        }
    }
}

@Composable
private fun TotalsCard(kcal: Double, p: Double, c: Double, f: Double) {
    AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(kcal.roundToInt().toString(), fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 44.sp, color = AppColors.Ink)
                Text(" kcal", fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = AppColors.Muted, modifier = Modifier.padding(bottom = 8.dp))
            }
            Spacer(Modifier.height(10.dp))
            MacroPillRow(p.roundToInt(), c.roundToInt(), f.roundToInt())
        }
    }
}

@Composable
private fun ItemRow(ing: MealIngredient, onMinus: () -> Unit, onPlus: () -> Unit, onDelete: () -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(ing.name, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AppColors.Ink)
                Text(
                    "${fmtQty(ing.quantity)} × ${ing.portion} · ${ing.totalCalories.roundToInt()} kcal",
                    fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted
                )
            }
            RoundIconButton(Icons.Rounded.Remove, "Less ${ing.name}", onMinus, size = 36.dp, container = AppColors.Chip, bordered = false)
            RoundIconButton(Icons.Rounded.Add, "More ${ing.name}", onPlus, size = 36.dp, container = AppColors.Chip, bordered = false)
            RoundIconButton(Icons.Rounded.DeleteOutline, "Remove ${ing.name}", onDelete, size = 36.dp, container = Color.Transparent, tint = AppColors.Muted, bordered = false)
        }
    }
}

@Composable
private fun ChatPanel(lines: List<ChatLine>, busy: Boolean) {
    val listState = rememberLazyListState()
    LaunchedEffect(lines.size, busy) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
    ) {
        if (lines.isEmpty() && !busy) {
            Text(
                "Tell me what to change, for example “make it 3 rotis” or “no ghee”.",
                modifier = Modifier.padding(16.dp),
                fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(lines) { _, l ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (l.fromUser) Arrangement.End else Arrangement.Start) {
                    Text(
                        l.text,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (l.fromUser) AppColors.Ink else if (l.isError) AppColors.AmberTint else AppColors.Chip)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        fontFamily = DmSans, fontSize = 14.sp,
                        color = if (l.fromUser) Color.White else if (l.isError) AppColors.AmberDeep else AppColors.Ink
                    )
                }
            }
            if (busy) {
                item { Text("Updating…", fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted) }
            }
        }
    }
}

@Composable
private fun ChatInput(value: String, onChange: (String) -> Unit, onSend: () -> Unit, enabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(26.dp))
            .padding(start = 20.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) Text("Change something…", fontFamily = DmSans, fontSize = 16.sp, color = Color(0xFF737E8F))
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                enabled = enabled,
                cursorBrush = SolidColor(AppColors.Ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                textStyle = TextStyle(fontFamily = DmSans, fontSize = 16.sp, color = AppColors.Ink),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Describe a change to this meal" }
            )
        }
        RoundIconButton(
            Icons.AutoMirrored.Rounded.Send, "Send", onSend,
            size = 40.dp, container = Color.Transparent, tint = AppColors.Blue, bordered = false
        )
    }
}
