package com.example.ui.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Meal
import com.example.model.MealType
import com.example.ui.CalorieTrackerViewModel
import com.example.ui.CalorieUiState
import com.example.ui.kit.AppCard
import com.example.ui.kit.AppLogo
import com.example.ui.kit.PrimaryButton
import com.example.ui.kit.OutlineButton
import com.example.ui.kit.Ring
import com.example.ui.kit.RoundIconButton
import com.example.ui.kit.rememberBase64Image
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppDimens
import com.example.ui.theme.DmSans
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val WEEK_DAYS = 10

@Composable
fun HomeScreen(
    viewModel: CalorieTrackerViewModel,
    onOpenProgress: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCapture: () -> Unit,
    onOpenMeal: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAddFood: () -> Unit,
    onSubmitText: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var weightSheet by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }

    val goalKcal = state.goal.calorieGoal.toDouble().coerceAtLeast(1.0)
    val eaten = state.totalCalories
    val firstDay = state.recentMeals.isEmpty() && state.meals.isEmpty()
    val days = remember { lastDays(WEEK_DAYS) }
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeHeader(onOpenProgress = onOpenProgress, onOpenProfile = onOpenProfile)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
                    .padding(bottom = 140.dp)
            ) {
                if (firstDay) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val name = state.profile?.name.orEmpty()
                        Text(
                            if (name.isBlank()) "Welcome" else "Welcome, $name",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineMedium, color = AppColors.Ink
                        )
                    }
                    FirstMealCard(onOpenCapture = onOpenCapture, onOpenAddFood = onOpenAddFood)
                } else {
                    WeekStrip(days = days, state = state, goalKcal = goalKcal, onSelect = { viewModel.selectDate(it) })
                }

                CalorieCard(state = state, goalKcal = goalKcal, eaten = eaten)
                MacroCard(state = state)
                WeightCard(state = state, onAdd = { weightSheet = true })
                MealLog(
                    state = state,
                    firstDay = firstDay,
                    onOpenMeal = onOpenMeal,
                    onOpenHistory = onOpenHistory
                )
            }
        }

        // floating bottom bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, AppColors.Background)))
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, top = 36.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoundIconButton(
                    Icons.Rounded.CameraAlt, "Add a photo", onOpenCapture, size = 52.dp
                )
                ChatPill(
                    value = draft,
                    onValueChange = { draft = it },
                    onSend = {
                        val t = draft.trim()
                        if (t.isNotEmpty()) {
                            draft = ""
                            onSubmitText(t)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (weightSheet) {
        WeightSheet(
            initialKg = state.weights.firstOrNull()?.kg ?: state.goal.startWeightKg.takeIf { it > 0 } ?: 70.0,
            goalKg = state.goal.goalWeightKg,
            onSave = { kg ->
                viewModel.logWeight(kg)
                weightSheet = false
            },
            onDismiss = { weightSheet = false }
        )
    }
}

// ---------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------

@Composable
private fun HomeHeader(onOpenProgress: () -> Unit, onOpenProfile: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.ScreenGutter, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f)) { AppLogo(44.dp) }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(AppColors.Segment)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(AppColors.Surface)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) { Text("Home", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.Ink) }
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenProgress)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) { Text("Progress", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.Muted) }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            RoundIconButton(Icons.Rounded.Person, "Profile and settings", onOpenProfile)
        }
    }
}

// ---------------------------------------------------------------------
// Week strip
// ---------------------------------------------------------------------

private data class DayCell(val date: String, val dow: String, val dayOfMonth: Int)

private fun lastDays(n: Int): List<DayCell> {
    val key = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dow = SimpleDateFormat("EEE", Locale.getDefault())
    val out = mutableListOf<DayCell>()
    for (i in n - 1 downTo 0) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -i)
        out.add(DayCell(key.format(c.time), dow.format(c.time), c.get(Calendar.DAY_OF_MONTH)))
    }
    return out
}

@Composable
private fun WeekStrip(
    days: List<DayCell>,
    state: CalorieUiState,
    goalKcal: Double,
    onSelect: (String) -> Unit
) {
    val scroll = rememberScrollState()
    LaunchedEffect(Unit) { scroll.scrollTo(scroll.maxValue) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = AppDimens.ScreenGutter, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        days.forEach { d ->
            val kcal = state.recentMeals.filter { it.dateString == d.date }.sumOf { it.calories }
            val selected = d.date == state.selectedDate
            val over = kcal > goalKcal * 1.05
            val shape = RoundedCornerShape(18.dp)
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .clip(shape)
                    .background(if (selected) AppColors.Surface else Color.Transparent)
                    .border(1.dp, if (selected) AppColors.BorderStrong else Color.Transparent, shape)
                    .clickable { onSelect(d.date) }
                    .padding(vertical = 8.dp)
                    .semantics { contentDescription = "${d.dow} ${d.dayOfMonth}, ${kcal.roundToInt()} kilocalories" },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(d.dow, fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = if (selected) AppColors.Ink else AppColors.Muted)
                Ring(
                    progress = (kcal / goalKcal).toFloat(),
                    color = if (over) AppColors.Amber else AppColors.Green,
                    modifier = Modifier.size(40.dp),
                    stroke = 4.dp
                ) {
                    Text(d.dayOfMonth.toString(), fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.Ink)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// Calories and macros
// ---------------------------------------------------------------------

@Composable
private fun CalorieCard(state: CalorieUiState, goalKcal: Double, eaten: Double) {
    val left = goalKcal - eaten
    val over = left < 0
    AppCard(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp).fillMaxWidth(),
        radius = AppDimens.CardRadiusLarge
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Ring(
                progress = (eaten / goalKcal).toFloat(),
                color = if (over) AppColors.Amber else AppColors.Green,
                modifier = Modifier
                    .size(120.dp)
                    .semantics { contentDescription = "${eaten.roundToInt()} of ${goalKcal.roundToInt()} calories eaten" },
                stroke = 9.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(eaten.roundToInt().toString(), fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = AppColors.Ink)
                    Text("eaten", fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = AppColors.Muted)
                }
            }
            Column {
                Text(
                    "%,d".format(Locale.getDefault(), abs(left).roundToInt()),
                    fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 44.sp,
                    color = if (over) AppColors.AmberDeep else AppColors.Ink
                )
                Text(
                    if (over) "kcal over" else "kcal left",
                    fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 15.sp,
                    color = if (over) AppColors.AmberDeep else AppColors.Muted
                )
                Spacer(Modifier.height(10.dp))
                Text(goalLine(state), fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted)
            }
        }
    }
}

private fun goalLine(state: CalorieUiState): String {
    val g = state.goal
    val pace = if (g.paceKgPerWeek % 1.0 == 0.0) g.paceKgPerWeek.toInt().toString() else g.paceKgPerWeek.toString()
    return when (g.goalType) {
        "LOSE" -> "Goal: lose $pace kg a week"
        "GAIN" -> "Goal: gain $pace kg a week"
        else -> "Goal: keep your weight steady"
    }
}

@Composable
private fun MacroCard(state: CalorieUiState) {
    AppCard(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp).fillMaxWidth(),
        radius = AppDimens.CardRadiusLarge
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
            MacroColumn("Protein", state.totalProtein, state.goal.proteinGoal.toDouble(), AppColors.Protein, Modifier.weight(1f))
            MacroColumn("Carbs", state.totalCarbs, state.goal.carbsGoal.toDouble(), AppColors.Carbs, Modifier.weight(1f))
            MacroColumn("Fat", state.totalFat, state.goal.fatGoal.toDouble(), AppColors.Fat, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MacroColumn(name: String, eaten: Double, goal: Double, color: Color, modifier: Modifier) {
    val left = goal - eaten
    val status = if (left >= 0) "${left.roundToInt()} g left" else "${abs(left).roundToInt()} g over"
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Ring(
            progress = (eaten / goal.coerceAtLeast(1.0)).toFloat(),
            color = color,
            modifier = Modifier
                .size(56.dp)
                .semantics { contentDescription = "$name, ${eaten.roundToInt()} of ${goal.roundToInt()} grams" },
            stroke = 5.dp
        ) {
            Text("${eaten.roundToInt()}g", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.Ink)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(name, fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = AppColors.Muted)
        }
        Text(status, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (left < 0) AppColors.AmberDeep else AppColors.Ink)
    }
}

// ---------------------------------------------------------------------
// Weight
// ---------------------------------------------------------------------

@Composable
private fun WeightCard(state: CalorieUiState, onAdd: () -> Unit) {
    val latest = state.weights.firstOrNull()
    val weekAgo = state.weights.drop(1).firstOrNull { daysBetween(it.dateString, latest?.dateString ?: "") >= 6 }
    val change = if (latest != null && weekAgo != null) latest.kg - weekAgo.kg else null
    val series = state.weights.take(8).reversed().map { it.kg }

    AppCard(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp).fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text("Weight", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.Muted)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        latest?.kg?.let { fmt1(it) } ?: "–",
                        fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = AppColors.Ink
                    )
                    Text(" kg", fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = AppColors.Muted, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
            if (series.size >= 2) Sparkline(series, Modifier.size(width = 78.dp, height = 33.dp))
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(60.dp)) {
                Text(
                    change?.let { (if (it > 0) "+" else if (it < 0) "−" else "") + fmt1(abs(it)) } ?: "–",
                    fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.GreenDeep
                )
                Text("this week", fontFamily = DmSans, fontSize = 11.sp, color = AppColors.Muted)
            }
            RoundIconButton(Icons.Rounded.Add, "Log weight", onAdd, size = 38.dp, container = AppColors.Chip, bordered = false)
        }
    }
}

@Composable
private fun Sparkline(values: List<Double>, modifier: Modifier) {
    Canvas(modifier = modifier.semantics { contentDescription = "Weight trend" }) {
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val pad = 6.dp.toPx()
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        val path = Path()
        var last = Offset.Zero
        values.forEachIndexed { i, v ->
            val x = pad + w * i / (values.size - 1)
            val y = pad + h * (1f - ((v - min) / range).toFloat())
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            last = Offset(x, y)
        }
        drawPath(path, AppColors.Green, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(AppColors.Surface, radius = 5.dp.toPx(), center = last)
        drawCircle(AppColors.Green, radius = 3.5.dp.toPx(), center = last)
    }
}

private fun daysBetween(older: String, newer: String): Int {
    return try {
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val a = f.parse(older)?.time ?: return 0
        val b = f.parse(newer)?.time ?: return 0
        ((b - a) / 86_400_000L).toInt()
    } catch (e: Exception) {
        0
    }
}

private fun fmt1(v: Double): String = String.format(Locale.getDefault(), "%.1f", v)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightSheet(initialKg: Double, goalKg: Double, onSave: (Double) -> Unit, onDismiss: () -> Unit) {
    var kg by remember { mutableStateOf(Math.round(initialKg * 10) / 10.0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Surface,
        shape = RoundedCornerShape(topStart = AppDimens.SheetRadius, topEnd = AppDimens.SheetRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Log weight", style = MaterialTheme.typography.titleLarge, color = AppColors.Ink)
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                RoundIconButton(Icons.Rounded.Remove, "Decrease by 0.1 kilograms", { kg = Math.round((kg - 0.1) * 10) / 10.0 }, size = 56.dp, container = AppColors.Chip, bordered = false)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(fmt1(kg), fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 56.sp, color = AppColors.Ink)
                    Text(" kg", fontFamily = DmSans, fontSize = 18.sp, color = AppColors.Muted, modifier = Modifier.padding(bottom = 10.dp))
                }
                RoundIconButton(Icons.Rounded.Add, "Increase by 0.1 kilograms", { kg = Math.round((kg + 0.1) * 10) / 10.0 }, size = 56.dp, container = AppColors.Chip, bordered = false)
            }
            if (goalKg > 0) {
                Spacer(Modifier.height(8.dp))
                val diff = kg - goalKg
                Text(
                    if (abs(diff) < 0.05) "You are at your goal" else "${fmt1(abs(diff))} kg ${if (diff > 0) "to lose" else "to gain"} to reach ${fmt1(goalKg)} kg",
                    fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted
                )
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton("Save", onClick = { onSave(kg) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ---------------------------------------------------------------------
// Meal log
// ---------------------------------------------------------------------

@Composable
private fun MealLog(
    state: CalorieUiState,
    firstDay: Boolean,
    onOpenMeal: (String) -> Unit,
    onOpenHistory: () -> Unit
) {
    val isToday = state.selectedDate == com.example.ui.getTodayDateString()
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (isToday) "Today’s meals" else "Meals",
                    fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink
                )
                if (state.meals.isNotEmpty()) {
                    Text("${state.meals.size} logged", fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted)
                }
            }
            if (!firstDay) {
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(AppColors.Surface)
                        .border(1.dp, AppColors.BorderStrong, CircleShape)
                        .clickable(onClick = onOpenHistory)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(14.dp))
                    Text("History", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AppColors.Ink)
                }
            }
        }

        if (state.meals.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDimens.CardRadius))
                    .background(AppColors.Surface)
                    .border(1.dp, AppColors.BorderStrong, RoundedCornerShape(AppDimens.CardRadius))
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(AppColors.Chip), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = AppColors.Muted, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text("No meals yet", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppColors.Ink)
                Text(
                    if (isToday) "Meals you add today show up here." else "Nothing was logged on this day.",
                    fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted, textAlign = TextAlign.Center
                )
            }
        } else {
            AppCard(Modifier.fillMaxWidth()) {
                state.meals.forEachIndexed { i, m ->
                    MealRow(m, showDivider = i > 0, onClick = { onOpenMeal(m.id) })
                }
            }
        }
    }
}

@Composable
private fun MealRow(m: Meal, showDivider: Boolean, onClick: () -> Unit) {
    val image = rememberBase64Image(m.imageUrl)
    val typeLabel = try { MealType.valueOf(m.mealType.uppercase()).displayName } catch (e: Exception) { m.mealType }
    val time = m.createdAt?.toDate()?.let { SimpleDateFormat("h:mm a", Locale.getDefault()).format(it) } ?: ""
    Column {
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(AppColors.Chip), contentAlignment = Alignment.Center) {
                if (image != null) {
                    Image(image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = AppColors.Muted, modifier = Modifier.size(26.dp))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(m.name, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AppColors.Ink, maxLines = 1)
                Text(
                    listOf(time, typeLabel).filter { it.isNotBlank() }.joinToString(" · "),
                    fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted
                )
                Text(
                    "P ${m.protein.roundToInt()} · C ${m.carbs.roundToInt()} · F ${m.fat.roundToInt()} g",
                    fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(m.calories.roundToInt().toString(), fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Ink)
                Text("kcal", fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted)
            }
        }
    }
}

// ---------------------------------------------------------------------
// First-day card and chat pill
// ---------------------------------------------------------------------

@Composable
private fun FirstMealCard(onOpenCapture: () -> Unit, onOpenAddFood: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimens.CardRadiusLarge))
            .background(AppColors.Ink)
            .padding(22.dp)
    ) {
        Text("Log your first meal", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(
            "Snap a photo and the AI works out the calories and macros for you.",
            fontFamily = DmSans, fontSize = 14.sp, color = Color(0xFFC9D0DA)
        )
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onOpenCapture),
                contentAlignment = Alignment.Center
            ) { Text("Take a photo", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Ink) }
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                    .clickable(onClick = onOpenAddFood)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) { Text("Search food", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White) }
        }
    }
}

@Composable
private fun ChatPill(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(26.dp))
            .padding(start = 20.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text("Enter your food...", fontFamily = DmSans, fontSize = 16.sp, color = Color(0xFF737E8F))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                cursorBrush = SolidColor(AppColors.Ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                textStyle = TextStyle(fontFamily = DmSans, fontSize = 16.sp, color = AppColors.Ink),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Enter your food or add context" }
            )
        }
        RoundIconButton(
            Icons.AutoMirrored.Rounded.Send, "Send", onSend,
            size = 40.dp, container = Color.Transparent, tint = AppColors.Blue, bordered = false
        )
    }
}
