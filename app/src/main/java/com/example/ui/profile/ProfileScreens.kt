package com.example.ui.profile

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ActivityLevel
import com.example.domain.BodyProfile
import com.example.domain.GoalInput
import com.example.domain.GoalType
import com.example.domain.NutritionCalculator
import com.example.domain.Sex
import com.example.model.UserGoal
import com.example.model.UserProfile
import com.example.ui.CalorieTrackerViewModel
import com.example.ui.CalorieUiState
import com.example.ui.kit.AppCard
import com.example.ui.kit.ChoiceChip
import com.example.ui.kit.FieldRow
import com.example.ui.kit.MacroPillRow
import com.example.ui.kit.PrimaryButton
import com.example.ui.kit.OutlineButton
import com.example.ui.kit.RoundIconButton
import com.example.ui.kit.ScreenHeader
import com.example.ui.kit.Segmented
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppDimens
import com.example.ui.theme.DmSans
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private fun fmt1(v: Double) = String.format(Locale.getDefault(), "%.1f", v)

private fun currentWeight(s: CalorieUiState): Double =
    s.weights.firstOrNull()?.kg ?: s.goal.startWeightKg.takeIf { it > 0 } ?: 0.0

private fun bodyOf(p: UserProfile, weightKg: Double) = BodyProfile(
    sex = if (p.sex == "FEMALE") Sex.FEMALE else Sex.MALE,
    ageYears = p.age.toInt(),
    heightCm = p.heightCm,
    weightKg = weightKg,
    activity = try { ActivityLevel.valueOf(p.activity) } catch (e: Exception) { ActivityLevel.LIGHT }
)

private fun goalTypeOf(g: UserGoal) = try { GoalType.valueOf(g.goalType) } catch (e: Exception) { GoalType.LOSE }

private fun goalSentence(g: UserGoal): String {
    val pace = if (g.paceKgPerWeek % 1.0 == 0.0) g.paceKgPerWeek.toInt().toString() else g.paceKgPerWeek.toString()
    return when (goalTypeOf(g)) {
        GoalType.LOSE -> "Lose $pace kg a week"
        GoalType.GAIN -> "Gain $pace kg a week"
        GoalType.MAINTAIN -> "Maintain your weight"
    }
}

private fun reachDate(weeks: Int): String {
    val c = Calendar.getInstance()
    c.add(Calendar.DAY_OF_YEAR, weeks * 7)
    return SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(c.time)
}

// ---------------------------------------------------------------------
// Profile
// ---------------------------------------------------------------------

@Composable
fun ProfileScreen(
    viewModel: CalorieTrackerViewModel,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onEditGoal: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val profile = state.profile ?: UserProfile()
    val goal = state.goal
    val weight = currentWeight(state)
    var menu by remember { mutableStateOf(false) }
    var dietDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(AppColors.Background).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppDimens.ScreenGutter, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
            Text(
                "Profile", modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.Ink
            )
            Box {
                RoundIconButton(Icons.Rounded.MoreVert, "More options", { menu = true })
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("Log out", fontFamily = DmSans) },
                        onClick = {
                            menu = false
                            Firebase.auth.signOut()
                            onLoggedOut()
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.ScreenGutter).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // profile card
            AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(56.dp).clip(CircleShape).background(AppColors.Ink), contentAlignment = Alignment.Center) {
                        Text(
                            profile.name.trim().take(1).uppercase().ifEmpty { "?" },
                            fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(profile.name.ifBlank { "Your profile" }, fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.Ink)
                        Text(
                            "${if (profile.sex == "FEMALE") "Female" else "Male"} · ${profile.age} yrs · ${profile.heightCm.roundToInt()} cm",
                            fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted
                        )
                    }
                    RoundIconButton(Icons.Rounded.Edit, "Edit profile", onEditProfile, size = 40.dp)
                }
            }

            // goal card
            AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Goal", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.Muted)
                            Text(goalSentence(goal), fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.Ink)
                        }
                        RoundIconButton(Icons.Rounded.Edit, "Edit goal", onEditGoal, size = 40.dp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (goal.goalWeightKg > 0) "${fmt1(weight)} kg now · ${fmt1(goal.goalWeightKg)} kg goal" else "${fmt1(weight)} kg now",
                        fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted
                    )
                    if (goal.goalWeightKg > 0 && goal.startWeightKg > 0 && goalTypeOf(goal) != GoalType.MAINTAIN) {
                        Spacer(Modifier.height(12.dp))
                        val total = abs(goal.startWeightKg - goal.goalWeightKg).coerceAtLeast(0.1)
                        val done = (abs(goal.startWeightKg - weight) / total).coerceIn(0.0, 1.0)
                        val movingRight = (goal.startWeightKg - weight) * (goal.startWeightKg - goal.goalWeightKg) >= 0
                        val frac = if (movingRight) done.toFloat() else 0f
                        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(AppColors.Track)) {
                            Box(Modifier.fillMaxWidth(frac).height(8.dp).clip(CircleShape).background(AppColors.Green))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("${goal.calorieGoal} kcal a day", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Ink)
                    Spacer(Modifier.height(8.dp))
                    MacroPillRow(goal.proteinGoal.toInt(), goal.carbsGoal.toInt(), goal.fatGoal.toInt())
                }
            }

            // food card
            AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Food", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.Muted)
                        Text(
                            profile.dietTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.joinToString(", ")
                                .ifEmpty { "Nothing set" },
                            fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.Ink
                        )
                    }
                    RoundIconButton(Icons.Rounded.Edit, "Edit food preferences", { dietDialog = true }, size = 40.dp)
                }
            }
        }
    }

    if (dietDialog) {
        var text by remember { mutableStateOf(profile.dietTags) }
        AlertDialog(
            onDismissRequest = { dietDialog = false },
            title = { Text("Food preferences", fontFamily = DmSans, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Separate items with commas, for example: Vegetarian, Peanuts", fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted)
                    Spacer(Modifier.height(10.dp))
                    BasicTextField(
                        value = text, onValueChange = { text = it },
                        cursorBrush = SolidColor(AppColors.Ink),
                        textStyle = TextStyle(fontFamily = DmSans, fontSize = 16.sp, color = AppColors.Ink),
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Chip).padding(14.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveProfile(profile.copy(dietTags = text.split(",").map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",")))
                    dietDialog = false
                }) { Text("Save", fontFamily = DmSans, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { dietDialog = false }) { Text("Cancel", fontFamily = DmSans) } }
        )
    }
}

// ---------------------------------------------------------------------
// Edit profile
// ---------------------------------------------------------------------

private val ACTIVITIES = listOf(
    Triple("Light", ActivityLevel.LIGHT, "Mostly sitting, light movement"),
    Triple("Moderate", ActivityLevel.MODERATE, "Exercise 3 to 4 days a week"),
    Triple("Active", ActivityLevel.ACTIVE, "Exercise most days or a physical job")
)

@Composable
fun EditProfileScreen(viewModel: CalorieTrackerViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val p = state.profile ?: UserProfile()
    var name by remember { mutableStateOf(p.name) }
    var female by remember { mutableStateOf(p.sex == "FEMALE") }
    var age by remember { mutableStateOf(p.age.toString()) }
    var height by remember { mutableStateOf(if (p.heightCm % 1.0 == 0.0) p.heightCm.toInt().toString() else p.heightCm.toString()) }
    var act by remember { mutableStateOf(ACTIVITIES.indexOfFirst { it.second.name == p.activity }.coerceAtLeast(0)) }

    val ageN = age.toIntOrNull()?.takeIf { it in 13..100 }
    val heightN = height.replace(',', '.').toDoubleOrNull()?.takeIf { it in 100.0..250.0 }
    val valid = ageN != null && heightN != null

    Column(Modifier.fillMaxSize().background(AppColors.Background).statusBarsPadding()) {
        ScreenHeader("Edit profile", onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = AppDimens.ScreenGutter),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("About you", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink)
            // name
            val shape = RoundedCornerShape(22.dp)
            Row(
                Modifier.fillMaxWidth().height(AppDimens.FieldHeight).clip(shape).background(AppColors.Surface)
                    .border(1.5.dp, AppColors.Border, shape).padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Name", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.Ink)
                BasicTextField(
                    value = name, onValueChange = { name = it }, singleLine = true,
                    cursorBrush = SolidColor(AppColors.Ink),
                    textStyle = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink, textAlign = androidx.compose.ui.text.style.TextAlign.End),
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
            }
            Segmented(listOf("Male", "Female"), if (female) 1 else 0, { female = it == 1 })
            FieldRow("Age", age, "yrs", { age = it.filter { c -> c.isDigit() }.take(3) }, keyboard = KeyboardType.Number)
            FieldRow("Height", height, "cm", { height = it })
            Spacer(Modifier.height(4.dp))
            Text("Activity level", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink)
            Segmented(ACTIVITIES.map { it.first }, act, { act = it })
            Text(ACTIVITIES[act].third, fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted)
            Text(
                "These details are used to work out your daily calories. Your weight comes from your weigh-ins on Home.",
                fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted
            )
        }
        Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(AppDimens.ScreenGutter)) {
            PrimaryButton("Save", enabled = valid, modifier = Modifier.fillMaxWidth(), onClick = {
                val updated = p.copy(
                    name = name.trim(), sex = if (female) "FEMALE" else "MALE",
                    age = ageN!!.toLong(), heightCm = heightN!!, activity = ACTIVITIES[act].second.name
                )
                viewModel.saveProfile(updated)
                // Recalculate the daily targets unless the user set them by hand.
                val g = state.goal
                val w = currentWeight(state)
                if (!g.manual && w > 0) {
                    val t = NutritionCalculator.targets(
                        bodyOf(updated, w),
                        GoalInput(goalTypeOf(g), g.goalWeightKg, g.paceKgPerWeek)
                    )
                    viewModel.saveGoal(
                        g.copy(id = "current", calorieGoal = t.calories.toLong(), proteinGoal = t.proteinG.toLong(),
                            carbsGoal = t.carbsG.toLong(), fatGoal = t.fatG.toLong())
                    )
                }
                onBack()
            })
        }
    }
}

// ---------------------------------------------------------------------
// Edit goal
// ---------------------------------------------------------------------

private val GOAL_TYPES = listOf(GoalType.LOSE, GoalType.MAINTAIN, GoalType.GAIN)
private val PACES = listOf(Triple("Gentle", 0.25, "0.25 kg a week"), Triple("Steady", 0.5, "0.5 kg a week"), Triple("Faster", 0.75, "0.75 kg a week"))

@Composable
fun EditGoalScreen(viewModel: CalorieTrackerViewModel, onBack: () -> Unit, onEditProfile: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val g0 = state.goal
    val profile = state.profile ?: UserProfile()
    val weight = currentWeight(state)

    var type by remember { mutableStateOf(goalTypeOf(g0)) }
    var goalWeight by remember { mutableStateOf(if (g0.goalWeightKg > 0) fmt1(g0.goalWeightKg) else "") }
    var pace by remember { mutableStateOf(g0.paceKgPerWeek.takeIf { it > 0 } ?: 0.5) }
    var manual by remember { mutableStateOf(g0.manual) }
    var mk by remember { mutableStateOf(g0.calorieGoal.toString()) }
    var mp by remember { mutableStateOf(g0.proteinGoal.toString()) }
    var mc by remember { mutableStateOf(g0.carbsGoal.toString()) }
    var mf by remember { mutableStateOf(g0.fatGoal.toString()) }

    val gw = if (type == GoalType.MAINTAIN) weight else goalWeight.replace(',', '.').toDoubleOrNull() ?: 0.0
    val usedPace = if (type == GoalType.MAINTAIN) 0.0 else pace
    val auto = if (weight > 0) NutritionCalculator.targets(bodyOf(profile, weight), GoalInput(type, gw, usedPace)) else null

    val kcal = if (manual) mk.toIntOrNull() ?: 0 else auto?.calories ?: 0
    val pG = if (manual) mp.toIntOrNull() ?: 0 else auto?.proteinG ?: 0
    val cG = if (manual) mc.toIntOrNull() ?: 0 else auto?.carbsG ?: 0
    val fG = if (manual) mf.toIntOrNull() ?: 0 else auto?.fatG ?: 0
    val macroKcal = NutritionCalculator.macroCalories(pG, cG, fG)
    val off = kcal > 0 && abs(macroKcal - kcal) > kcal * 0.05

    // The goal weight must point the right way for the chosen goal.
    val goalWeightOk = when (type) {
        GoalType.MAINTAIN -> true
        GoalType.LOSE -> gw in 30.0..weight - 0.1
        GoalType.GAIN -> gw >= weight + 0.1 && gw <= 250.0
    }
    val canSave = weight > 0 && kcal >= 800 && goalWeightOk

    Column(Modifier.fillMaxSize().background(AppColors.Background).statusBarsPadding()) {
        ScreenHeader("Edit goal", onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = AppDimens.ScreenGutter),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // preview
            AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
                Column(Modifier.padding(18.dp)) {
                    Text("Daily target", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.Muted)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("%,d".format(Locale.getDefault(), kcal), fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 40.sp, color = AppColors.Ink)
                        Text(" kcal", fontFamily = DmSans, fontSize = 16.sp, color = AppColors.Muted, modifier = Modifier.padding(bottom = 7.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    MacroPillRow(pG, cG, fG)
                    Spacer(Modifier.height(10.dp))
                    val weeks = NutritionCalculator.weeksToGoal(weight, gw, usedPace)
                    Text(
                        when {
                            type == GoalType.MAINTAIN -> "Keeping your weight steady"
                            usedPace > 0 && weeks > 0 && goalWeightOk -> "Reach ${fmt1(gw)} kg by ${reachDate(weeks)} (about $weeks weeks)"
                            else -> "Enter a goal weight on the right side of ${fmt1(weight)} kg"
                        },
                        fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted
                    )
                }
            }

            Text("Goal", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink)
            Segmented(listOf("Lose", "Maintain", "Gain"), GOAL_TYPES.indexOf(type), { type = GOAL_TYPES[it] })
            if (type != GoalType.MAINTAIN) {
                FieldRow("Goal weight", goalWeight, "kg", { goalWeight = it })
                Text("Pace", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PACES.forEach { (label, v, _) -> ChoiceChip(label, pace == v, { pace = v }) }
                }
                Text(PACES.first { it.second == pace }.third, fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted)
            }

            Text("Targets", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Set manually", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.Ink)
                    Text(
                        if (manual) "Using your own numbers" else "Calculated from your profile",
                        fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted
                    )
                }
                Switch(
                    checked = manual,
                    onCheckedChange = { on ->
                        manual = on
                        if (on && auto != null) {
                            mk = auto.calories.toString(); mp = auto.proteinG.toString()
                            mc = auto.carbsG.toString(); mf = auto.fatG.toString()
                        }
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Green)
                )
            }
            if (manual) {
                FieldRow("Calories", mk, "kcal", { mk = it.filter { c -> c.isDigit() }.take(5) }, keyboard = KeyboardType.Number)
                FieldRow("Protein", mp, "g", { mp = it.filter { c -> c.isDigit() }.take(4) }, keyboard = KeyboardType.Number)
                FieldRow("Carbs", mc, "g", { mc = it.filter { c -> c.isDigit() }.take(4) }, keyboard = KeyboardType.Number)
                FieldRow("Fat", mf, "g", { mf = it.filter { c -> c.isDigit() }.take(4) }, keyboard = KeyboardType.Number)
                Text(
                    if (off) "Your macros add up to %,d kcal, which is not close to %,d kcal.".format(Locale.getDefault(), macroKcal, kcal)
                    else "Your macros add up to %,d kcal.".format(Locale.getDefault(), macroKcal),
                    fontFamily = DmSans, fontSize = 13.sp, color = if (off) AppColors.AmberDeep else AppColors.Muted
                )
                OutlineButton("Reset to recommended", onClick = { manual = false }, modifier = Modifier.fillMaxWidth())
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Based on your profile: ${if (profile.sex == "FEMALE") "Female" else "Male"}, ${profile.age}, ${profile.heightCm.roundToInt()} cm, ${fmt1(weight)} kg, ${profile.activity.lowercase()} activity.",
                    modifier = Modifier.weight(1f), fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted
                )
                Text(
                    "Edit", modifier = Modifier.clickable(onClick = onEditProfile).padding(8.dp),
                    fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.Blue
                )
            }
            Spacer(Modifier.height(4.dp))
        }
        Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(AppDimens.ScreenGutter)) {
            PrimaryButton("Save · %,d kcal".format(Locale.getDefault(), kcal), enabled = canSave, modifier = Modifier.fillMaxWidth(), onClick = {
                viewModel.saveGoal(
                    g0.copy(
                        id = "current",
                        calorieGoal = kcal.toLong(), proteinGoal = pG.toLong(), carbsGoal = cG.toLong(), fatGoal = fG.toLong(),
                        goalType = type.name, goalWeightKg = gw,
                        startWeightKg = if (g0.startWeightKg > 0 && g0.goalType == type.name) g0.startWeightKg else weight,
                        paceKgPerWeek = usedPace, manual = manual
                    )
                )
                onBack()
            })
        }
    }
}
