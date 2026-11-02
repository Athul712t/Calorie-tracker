package com.example.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Meal
import com.example.model.MealType
import com.example.ui.CalorieTrackerViewModel
import com.example.ui.daysAgoString
import com.example.ui.getTodayDateString
import com.example.ui.kit.AppCard
import com.example.ui.kit.OutlineButton
import com.example.ui.kit.PrimaryButton
import com.example.ui.kit.RoundIconButton
import com.example.ui.kit.ScreenHeader
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppDimens
import com.example.ui.theme.DmSans
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

private enum class DayStatus { NONE, ON_TARGET, OVER, UNDER, TODAY, FUTURE }

private val BLUE_TINT = Color(0xFFEAF1FE)
private val BLUE_DEEP = Color(0xFF1F5FD6)
private val KEY = SimpleDateFormat("yyyy-MM-dd", Locale.US)

private fun key(c: Calendar): String = KEY.format(c.time)

private fun statusOf(kcal: Double?, goal: Double, isToday: Boolean, isFuture: Boolean): DayStatus = when {
    isFuture -> DayStatus.FUTURE
    isToday -> DayStatus.TODAY
    kcal == null || kcal <= 0.0 -> DayStatus.NONE
    kcal > goal * 1.05 -> DayStatus.OVER
    kcal < goal * 0.90 -> DayStatus.UNDER
    else -> DayStatus.ON_TARGET
}

private fun fmtTime(m: Meal): String =
    m.createdAt?.toDate()?.let { SimpleDateFormat("h:mm a", Locale.getDefault()).format(it) } ?: ""

@Composable
fun HistoryScreen(
    viewModel: CalorieTrackerViewModel,
    onBack: () -> Unit,
    onAddMeal: (dateString: String) -> Unit,
    onOpenMeal: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val goal = state.goal.calorieGoal.toDouble().coerceAtLeast(1.0)
    val today = remember { getTodayDateString() }
    val yesterday = remember { daysAgoString(1) }

    // month shown: 0 = this month, -1 = last month, ... (data is kept for about 120 days)
    var offset by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf(today) }

    val byDay = remember(state.recentMeals) { state.recentMeals.groupBy { it.dateString } }
    val first = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); add(Calendar.MONTH, offset) }
    val daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH)
    val lead = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
    val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(first.time)

    var logged = 0
    var onTarget = 0
    var sum = 0.0
    val cells = mutableListOf<Pair<String, DayStatus>?>()
    repeat(lead) { cells.add(null) }
    for (d in 1..daysInMonth) {
        val c = (first.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, d) }
        val k = key(c)
        val kcal = byDay[k]?.sumOf { it.calories }
        val st = statusOf(kcal, goal, k == today, k > today)
        if (st != DayStatus.NONE && st != DayStatus.TODAY && st != DayStatus.FUTURE) {
            logged++
            sum += kcal ?: 0.0
            if (st == DayStatus.ON_TARGET) onTarget++
        }
        cells.add(k to st)
    }

    Column(Modifier.fillMaxSize().background(AppColors.Background).statusBarsPadding()) {
        ScreenHeader("History", onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = AppDimens.ScreenGutter).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoundIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Previous month", { if (offset > -3) offset-- }, size = 36.dp, bordered = false, container = AppColors.Chip)
                        Text(monthTitle, Modifier.weight(1f), textAlign = TextAlign.Center, fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppColors.Ink)
                        RoundIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Next month", { if (offset < 0) offset++ }, size = 36.dp, bordered = false, container = AppColors.Chip)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth()) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                            Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.Muted)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    cells.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            for (i in 0 until 7) {
                                val cell = week.getOrNull(i)
                                Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                                    if (cell != null) DayCell(cell.first, cell.second, cell.first == selected, byDay[cell.first]?.sumOf { it.calories }) {
                                        selected = cell.first
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Legend()
                }
            }

            // month stats
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("Days logged", logged.toString(), Modifier.weight(1f))
                Stat("On target", onTarget.toString(), Modifier.weight(1f))
                Stat("Avg kcal", if (logged > 0) "%,d".format(Locale.getDefault(), (sum / logged).roundToInt()) else "–", Modifier.weight(1f))
            }

            DayDetail(
                date = selected, today = today, yesterday = yesterday, goal = goal,
                meals = byDay[selected].orEmpty().sortedBy { it.hourOfDay },
                canCopyYesterday = !byDay[yesterday].isNullOrEmpty(),
                onAddMeal = { onAddMeal(selected) },
                onOpenMeal = onOpenMeal,
                onCopyToToday = { viewModel.copyDay(selected, today) },
                onCopyYesterday = { viewModel.copyDay(yesterday, selected) }
            )
        }
    }
}

@Composable
private fun DayCell(dateKey: String, status: DayStatus, isSelected: Boolean, kcal: Double?, onClick: () -> Unit) {
    val (bg, fg) = when (status) {
        DayStatus.ON_TARGET -> AppColors.GreenTint to AppColors.GreenDeep
        DayStatus.OVER -> AppColors.AmberTint to AppColors.AmberDeep
        DayStatus.UNDER -> BLUE_TINT to BLUE_DEEP
        DayStatus.FUTURE -> Color.Transparent to Color(0xFFC4CAD3)
        else -> Color.Transparent to AppColors.Ink
    }
    val day = dateKey.takeLast(2).trimStart('0')
    val dashed = status == DayStatus.NONE
    val label = when (status) {
        DayStatus.FUTURE -> "upcoming"
        DayStatus.NONE -> "not logged"
        else -> "${(kcal ?: 0.0).roundToInt()} kilocalories"
    }
    Box(
        Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(if (isSelected) AppColors.Ink else bg)
            .then(if (status == DayStatus.TODAY && !isSelected) Modifier.border(1.5.dp, AppColors.Ink, CircleShape) else Modifier)
            .then(
                if (dashed && !isSelected) Modifier.drawBehind {
                    drawRoundRect(
                        color = Color(0xFFD5DAE1), cornerRadius = CornerRadius(size.minDimension / 2),
                        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                    )
                } else Modifier
            )
            .clickable(enabled = status != DayStatus.FUTURE, onClick = onClick)
            .semantics { contentDescription = "$dateKey, $label" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            day, fontFamily = DmSans,
            fontWeight = if (isSelected || status == DayStatus.TODAY) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp, color = if (isSelected) Color.White else fg
        )
    }
}

@Composable
private fun Legend() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf(
            "On target" to AppColors.Green, "Over" to AppColors.Amber, "Under" to AppColors.Blue, "Not logged" to Color(0xFFC4CAD3)
        ).forEach { (t, c) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(c))
                Text(t, fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    AppCard(modifier) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppColors.Ink)
            Text(label, fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted)
        }
    }
}

@Composable
private fun DayDetail(
    date: String, today: String, yesterday: String, goal: Double, meals: List<Meal>,
    canCopyYesterday: Boolean,
    onAddMeal: () -> Unit, onOpenMeal: (String) -> Unit,
    onCopyToToday: () -> Unit, onCopyYesterday: () -> Unit
) {
    val parsed = try { KEY.parse(date) } catch (e: Exception) { null }
    val rel = when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> parsed?.let { SimpleDateFormat("EEEE", Locale.getDefault()).format(it) } ?: ""
    }
    val title = parsed?.let { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(it) } ?: date
    val kcal = meals.sumOf { it.calories }

    AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
        Column(Modifier.padding(18.dp)) {
            Text(rel, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.Muted)
            Text(title, fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.Ink)
            if (meals.isEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text("Nothing logged", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppColors.Ink)
                Text("You can still add what you ate. Past days count towards your trends.", fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted)
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Add a meal", onClick = onAddMeal, modifier = Modifier.fillMaxWidth())
                if (date != yesterday && canCopyYesterday) {
                    Spacer(Modifier.height(8.dp))
                    OutlineButton("Copy yesterday", onClick = onCopyYesterday, modifier = Modifier.fillMaxWidth())
                }
            } else {
                Spacer(Modifier.height(6.dp))
                Text(
                    "%,d of %,d kcal".format(Locale.getDefault(), kcal.roundToInt(), goal.roundToInt()),
                    fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AppColors.Muted
                )
                Spacer(Modifier.height(10.dp))
                meals.forEachIndexed { i, m ->
                    if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))
                    val type = try { MealType.valueOf(m.mealType.uppercase()).displayName } catch (e: Exception) { m.mealType }
                    Row(Modifier.fillMaxWidth().clickable { onOpenMeal(m.id) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(m.name, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = AppColors.Ink, maxLines = 1)
                            Text(
                                listOf(fmtTime(m), type, "P ${m.protein.roundToInt()} · C ${m.carbs.roundToInt()} · F ${m.fat.roundToInt()} g").filter { it.isNotBlank() }.joinToString(" · "),
                                fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted
                            )
                        }
                        Text("${m.calories.roundToInt()} kcal", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.Ink)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlineButton("Add meal", onClick = onAddMeal, modifier = Modifier.weight(1f))
                    if (date != today) OutlineButton("Copy to today", onClick = onCopyToToday, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
