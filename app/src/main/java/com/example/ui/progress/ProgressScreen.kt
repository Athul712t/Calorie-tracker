package com.example.ui.progress

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalorieTrackerViewModel
import com.example.ui.CalorieUiState
import com.example.ui.daysAgoString
import com.example.ui.kit.AppCard
import com.example.ui.kit.OutlineButton
import com.example.ui.kit.RoundIconButton
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppDimens
import com.example.ui.theme.DmSans
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val QUESTIONS = listOf("How was my week?", "Why do I go over on some days?", "Am I on track for my goal?")
private val RANGES = listOf("1W" to 7, "1M" to 30, "3M" to 90)
private val DAY_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

private fun fmt1(v: Double) = String.format(Locale.getDefault(), "%.1f", v)

private fun shortDate(key: String): String = try {
    SimpleDateFormat("MMM d", Locale.getDefault()).format(DAY_FMT.parse(key)!!)
} catch (e: Exception) { key }

@Composable
fun ProgressScreen(
    viewModel: CalorieTrackerViewModel,
    onHome: () -> Unit,
    onOpenPhotos: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var question by remember { mutableStateOf("") }
    var asked by remember { mutableStateOf<String?>(null) }
    var answer by remember { mutableStateOf<String?>(null) }
    var answerIsError by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    val ask: (String) -> Unit = { q ->
        if (q.isNotBlank() && !busy) {
            asked = q
            answer = null
            answerIsError = false
            busy = true
            question = ""
            viewModel.askInsight(q) { ok, text ->
                busy = false
                answerIsError = !ok
                answer = text
            }
        }
    }

    Box(Modifier.fillMaxSize().background(AppColors.Background).statusBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            ProgressHeader(onHome)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = AppDimens.ScreenGutter).padding(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightsCard(asked, answer, answerIsError, busy, ask)
                WeightCard(state)
                PhotosCard(viewModel, state, onOpenPhotos)
                CaloriesCard(state)
                MacrosCard(state)
            }
        }

        // chat bar at the bottom of the page, not inside a card
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, AppColors.Background)))
                .navigationBarsPadding().imePadding()
                .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(26.dp)).background(AppColors.Surface)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(26.dp)).padding(start = 20.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (question.isEmpty()) Text("Ask about your progress...", fontFamily = DmSans, fontSize = 16.sp, color = Color(0xFF737E8F))
                    BasicTextField(
                        value = question, onValueChange = { question = it }, singleLine = true,
                        cursorBrush = SolidColor(AppColors.Ink),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { ask(question.trim()) }),
                        textStyle = TextStyle(fontFamily = DmSans, fontSize = 16.sp, color = AppColors.Ink),
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Ask a question about your progress" }
                    )
                }
                RoundIconButton(
                    Icons.AutoMirrored.Rounded.Send, "Send", { ask(question.trim()) },
                    size = 40.dp, container = Color.Transparent, tint = AppColors.Blue, bordered = false
                )
            }
        }
    }
}

@Composable
private fun ProgressHeader(onHome: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = AppDimens.ScreenGutter, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(Modifier.clip(CircleShape).background(AppColors.Segment).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                Modifier.height(36.dp).clip(CircleShape).clickable(onClick = onHome).padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) { Text("Home", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.Muted) }
            Box(
                Modifier.height(36.dp).clip(CircleShape).background(AppColors.Surface).padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) { Text("Progress", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.Ink) }
        }
    }
}

// ---------------------------------------------------------------------
// Insights
// ---------------------------------------------------------------------

@Composable
private fun InsightsCard(asked: String?, answer: String?, isError: Boolean, busy: Boolean, ask: (String) -> Unit) {
    AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
        Column(Modifier.padding(18.dp)) {
            Text("Insights", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QUESTIONS.forEach { q ->
                    Text(
                        q,
                        modifier = Modifier.clip(CircleShape).background(AppColors.Chip).clickable { ask(q) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        fontFamily = DmSans, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = AppColors.InkSoft
                    )
                }
            }
            if (asked != null) {
                Spacer(Modifier.height(14.dp))
                Text(asked, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.Muted)
                Spacer(Modifier.height(4.dp))
                Text(
                    when {
                        busy -> "Looking at your numbers…"
                        else -> answer.orEmpty()
                    },
                    fontFamily = DmSans, fontSize = 15.sp,
                    color = if (isError) AppColors.AmberDeep else AppColors.Ink
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// Weight
// ---------------------------------------------------------------------

@Composable
private fun WeightCard(state: CalorieUiState) {
    var range by remember { mutableStateOf(0) }
    val cutoff = daysAgoString(RANGES[range].second)
    val points = state.weights.filter { it.dateString >= cutoff }.sortedBy { it.dateString }
    val current = state.weights.firstOrNull()?.kg
    val change = if (points.size >= 2) points.last().kg - points.first().kg else null
    val g = state.goal

    AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Weight", Modifier.weight(1f), fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink)
                Row(Modifier.clip(CircleShape).background(AppColors.Segment).padding(3.dp)) {
                    RANGES.forEachIndexed { i, (label, _) ->
                        Box(
                            Modifier.height(30.dp).clip(CircleShape)
                                .background(if (i == range) AppColors.Surface else Color.Transparent)
                                .clickable { range = i }.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                                color = if (i == range) AppColors.Ink else AppColors.Muted)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(current?.let { fmt1(it) } ?: "–", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 36.sp, color = AppColors.Ink)
                Text(" kg", fontFamily = DmSans, fontSize = 15.sp, color = AppColors.Muted, modifier = Modifier.padding(bottom = 6.dp))
                Spacer(Modifier.weight(1f))
                if (change != null) {
                    Text(
                        (if (change > 0) "+" else if (change < 0) "−" else "") + fmt1(abs(change)) + " kg",
                        fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.GreenDeep
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (points.size >= 2) {
                WeightChart(points.map { it.kg }, Modifier.fillMaxWidth().height(132.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text(shortDate(points.first().dateString), Modifier.weight(1f), fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted)
                    Text(shortDate(points.last().dateString), fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted)
                }
            } else {
                Text(
                    "Log your weight on Home to see a trend here.",
                    fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted, modifier = Modifier.padding(vertical = 24.dp)
                )
            }
            if (g.goalWeightKg > 0 && g.startWeightKg > 0 && current != null && g.goalType != "MAINTAIN") {
                Spacer(Modifier.height(14.dp))
                val total = abs(g.startWeightKg - g.goalWeightKg).coerceAtLeast(0.1)
                val onTrack = (g.startWeightKg - current) * (g.startWeightKg - g.goalWeightKg) > 0
                val moved = abs(g.startWeightKg - current)
                val frac = if (onTrack) (moved / total).coerceIn(0.0, 1.0).toFloat() else 0f
                Row(Modifier.fillMaxWidth()) {
                    Text("Start ${fmt1(g.startWeightKg)} kg", Modifier.weight(1f), fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted)
                    Text("Goal ${fmt1(g.goalWeightKg)} kg", fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted)
                }
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(AppColors.Track)) {
                    Box(Modifier.fillMaxWidth(frac).fillMaxHeight().clip(CircleShape).background(AppColors.Green))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (onTrack) "${fmt1(moved)} kg ${if (g.goalType == "GAIN") "gained" else "lost"} · ${fmt1(abs(current - g.goalWeightKg))} kg to go"
                    else "${fmt1(abs(current - g.goalWeightKg))} kg to go",
                    fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.Ink
                )
            }
        }
    }
}

@Composable
private fun WeightChart(values: List<Double>, modifier: Modifier) {
    Canvas(modifier.semantics { contentDescription = "Weight trend from ${fmt1(values.first())} to ${fmt1(values.last())} kilograms" }) {
        val mn = values.min()
        val mx = values.max()
        val span = (mx - mn).takeIf { it > 0.0 } ?: 1.0
        val padX = 8.dp.toPx()
        val padY = 14.dp.toPx()
        val w = size.width - padX * 2
        val h = size.height - padY * 2
        val pts = values.mapIndexed { i, v ->
            Offset(padX + w * i / (values.size - 1), padY + h * (1f - ((v - mn) / span).toFloat()))
        }
        val line = Path().apply { pts.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) } }
        val area = Path().apply {
            addPath(line)
            lineTo(pts.last().x, size.height)
            lineTo(pts.first().x, size.height)
            close()
        }
        drawPath(area, AppColors.Green.copy(alpha = 0.10f))
        drawPath(line, AppColors.Green, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        val last = pts.last()
        drawCircle(AppColors.Surface, 6.dp.toPx(), last)
        drawCircle(AppColors.Green, 4.dp.toPx(), last)
    }
}

// ---------------------------------------------------------------------
// Photos preview
// ---------------------------------------------------------------------

@Composable
private fun PhotosCard(viewModel: CalorieTrackerViewModel, state: CalorieUiState, onOpenPhotos: () -> Unit) {
    val latestDate = state.photos.firstOrNull()?.dateString
    val latest = if (latestDate == null) emptyList() else state.photos.filter { it.dateString == latestDate }
    AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth().clickable(onClick = onOpenPhotos), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Progress photos", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink)
                    Text(
                        if (latestDate != null) "Latest check-in · ${shortDate(latestDate)}" else "No check-ins yet",
                        fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted
                    )
                }
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Open progress photos", tint = AppColors.Muted)
            }
            Spacer(Modifier.height(12.dp))
            if (latest.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("FRONT", "LEFT", "RIGHT", "BACK").forEach { angle ->
                        val photo = latest.firstOrNull { it.angle == angle }
                        val bmp by produceState<Bitmap?>(null, photo?.storagePath) {
                            value = photo?.let { viewModel.loadPhoto(it.storagePath) }
                        }
                        Box(
                            Modifier.weight(1f).height(110.dp).clip(RoundedCornerShape(14.dp)).background(AppColors.Chip),
                            contentAlignment = Alignment.Center
                        ) {
                            bmp?.let {
                                Image(it.asImageBitmap(), "$angle photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            OutlineButton(if (latest.isEmpty()) "Add your first photos" else "Upload new", onClick = onOpenPhotos, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ---------------------------------------------------------------------
// Calories and macros (last 7 days, ending today)
// ---------------------------------------------------------------------

private data class DayTotal(val key: String, val dow: String, val kcal: Double, val p: Double, val c: Double, val f: Double, val logged: Boolean)

private fun lastSevenDays(state: CalorieUiState): List<DayTotal> {
    val byDay = state.recentMeals.groupBy { it.dateString }
    val dow = SimpleDateFormat("EEE", Locale.getDefault())
    return (6 downTo 0).map { i ->
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
        val key = DAY_FMT.format(cal.time)
        val meals = byDay[key].orEmpty()
        DayTotal(key, dow.format(cal.time).take(3), meals.sumOf { it.calories }, meals.sumOf { it.protein },
            meals.sumOf { it.carbs }, meals.sumOf { it.fat }, meals.isNotEmpty())
    }
}

@Composable
private fun CaloriesCard(state: CalorieUiState) {
    val days = lastSevenDays(state)
    val goal = state.goal.calorieGoal.toDouble().coerceAtLeast(1.0)
    val logged = days.filter { it.logged }
    val avg = if (logged.isNotEmpty()) logged.sumOf { it.kcal } / logged.size else null
    val onTarget = logged.count { it.kcal in goal * 0.9..goal * 1.05 }
    val maxV = maxOf(days.maxOf { it.kcal }, goal) * 1.1

    AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
        Column(Modifier.padding(18.dp)) {
            Text("Calories", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink)
            Text("Last 7 days", fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(avg?.let { "%,d".format(Locale.getDefault(), it.roundToInt()) } ?: "–", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = AppColors.Ink)
                Text(" kcal avg", fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted, modifier = Modifier.padding(bottom = 5.dp))
                Spacer(Modifier.weight(1f))
                if (logged.isNotEmpty()) {
                    Text("$onTarget of ${logged.size} days on target", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AppColors.GreenDeep)
                }
            }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(140.dp)) {
                Canvas(Modifier.fillMaxSize()) {
                    val y = size.height * (1f - (goal / maxV).toFloat())
                    drawLine(
                        AppColors.BorderStrong, Offset(0f, y), Offset(size.width, y),
                        strokeWidth = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                    days.forEach { d ->
                        val frac = (d.kcal / maxV).toFloat().coerceIn(0f, 1f)
                        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                            if (d.logged) {
                                Box(
                                    Modifier.fillMaxWidth().fillMaxHeight(frac.coerceAtLeast(0.02f))
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(if (d.kcal > goal * 1.05) AppColors.Amber else AppColors.Green)
                                        .semantics { contentDescription = "${d.dow}, ${d.kcal.roundToInt()} kilocalories" }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                days.forEach { Text(it.dow, Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted) }
            }
        }
    }
}

@Composable
private fun MacrosCard(state: CalorieUiState) {
    val logged = lastSevenDays(state).filter { it.logged }
    val n = logged.size.coerceAtLeast(1)
    val rows = listOf(
        Triple("Protein", logged.sumOf { it.p } / n to state.goal.proteinGoal.toDouble(), AppColors.Protein),
        Triple("Carbs", logged.sumOf { it.c } / n to state.goal.carbsGoal.toDouble(), AppColors.Carbs),
        Triple("Fat", logged.sumOf { it.f } / n to state.goal.fatGoal.toDouble(), AppColors.Fat)
    )
    AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column {
                Text("Macros", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink)
                Text("Daily average of the last 7 days", fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted)
            }
            if (logged.isEmpty()) {
                Text("Log a meal to see your averages.", fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted)
            } else rows.forEach { (name, vals, color) ->
                val (avg, target) = vals
                Column {
                    Row {
                        Text(name, Modifier.weight(1f), fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.Ink)
                        Text("${avg.roundToInt()} / ${target.roundToInt()} g", fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted)
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(AppColors.Track)) {
                        Box(Modifier.fillMaxWidth((avg / target.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)).fillMaxHeight().clip(CircleShape).background(color))
                    }
                }
            }
        }
    }
}
