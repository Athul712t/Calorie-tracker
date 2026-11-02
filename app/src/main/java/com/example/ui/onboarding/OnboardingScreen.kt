package com.example.ui.onboarding

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.domain.ActivityLevel
import com.example.domain.BodyProfile
import com.example.domain.GoalInput
import com.example.domain.GoalType
import com.example.domain.NutritionCalculator
import com.example.domain.Sex
import com.example.model.UserGoal
import com.example.model.UserProfile
import com.example.ui.CalorieTrackerViewModel
import com.example.ui.kit.AppCard
import com.example.ui.kit.AppLogo
import com.example.ui.kit.ChoiceChip
import com.example.ui.kit.FieldRow
import com.example.ui.kit.MacroPillRow
import com.example.ui.kit.PrimaryButton
import com.example.ui.kit.RoundIconButton
import com.example.ui.kit.Segmented
import com.example.ui.performGoogleSignIn
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppDimens
import com.example.ui.theme.DmSans
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class Step { WELCOME, GOAL, ABOUT, PACE, FOOD, PLAN }

private val DIETS = listOf("Vegetarian", "Eggetarian", "Non-veg", "Vegan", "Jain")
private val ALLERGIES = listOf("Peanuts", "Dairy", "Gluten", "Eggs", "Shellfish", "Mushrooms")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    viewModel: CalorieTrackerViewModel,
    onFinished: () -> Unit
) {
    val context: Context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var stepIndex by remember { mutableIntStateOf(if (Firebase.auth.currentUser != null) 1 else 0) }
    var goal by remember { mutableStateOf(GoalType.LOSE) }
    var sex by remember { mutableStateOf(Sex.MALE) }
    var age by remember { mutableStateOf("30") }
    var height by remember { mutableStateOf("175") }
    var weight by remember { mutableStateOf("72.4") }
    var goalWeight by remember { mutableStateOf("68") }
    var pace by remember { mutableStateOf(0.5) }
    var diet by remember { mutableIntStateOf(1) }
    val avoid: SnapshotStateList<String> = remember { mutableStateListOf() }
    val custom: SnapshotStateList<String> = remember { mutableStateListOf() }
    var adding by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    val steps = remember(goal) {
        buildList {
            add(Step.WELCOME); add(Step.GOAL); add(Step.ABOUT)
            if (goal != GoalType.MAINTAIN) add(Step.PACE)
            add(Step.FOOD); add(Step.PLAN)
        }
    }
    val idx = stepIndex.coerceIn(0, steps.lastIndex)
    val current = steps[idx]

    val ageN = age.toIntOrNull()
    val heightN = height.toDoubleOrNull()
    val weightN = weight.toDoubleOrNull()
    val goalN = goalWeight.toDoubleOrNull()
    val aboutValid = ageN != null && ageN in 10..120 &&
        heightN != null && heightN in 80.0..250.0 &&
        weightN != null && weightN in 20.0..400.0 &&
        (goal == GoalType.MAINTAIN || (goalN != null && goalN in 20.0..400.0))

    // The signed-in state may arrive after the screen opens (Google sign-in).
    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser != null && stepIndex == 0) stepIndex = 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // top bar with progress
        if (current != Step.WELCOME) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.ScreenGutter, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoundIconButton(
                    Icons.AutoMirrored.Rounded.ArrowBack, "Back",
                    onClick = { if (idx > 1) stepIndex = idx - 1 },
                    size = 40.dp
                )
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 1..steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(if (i <= idx) AppColors.Ink else AppColors.Track)
                        )
                    }
                }
            }
        }

        // content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.ScreenGutter)
        ) {
            when (current) {
                Step.WELCOME -> {
                    Spacer(Modifier.height(96.dp))
                    AppLogo(size = 72.dp)
                    Spacer(Modifier.height(28.dp))
                    Title("Log meals with a photo or a sentence")
                    Sub("Two minutes to set up. Then tracking takes seconds.")
                }
                Step.GOAL -> {
                    Spacer(Modifier.height(12.dp))
                    Title("What is your goal?")
                    Sub("We will set your daily calories to match.")
                    Spacer(Modifier.height(20.dp))
                    OptionCard("Lose weight", "Eat a little less, steadily", goal == GoalType.LOSE) { goal = GoalType.LOSE }
                    Spacer(Modifier.height(10.dp))
                    OptionCard("Maintain weight", "Stay where you are", goal == GoalType.MAINTAIN) { goal = GoalType.MAINTAIN }
                    Spacer(Modifier.height(10.dp))
                    OptionCard("Gain weight", "Eat a little more, with enough protein", goal == GoalType.GAIN) {
                        goal = GoalType.GAIN
                        val w = weightN
                        val g = goalN
                        if (w != null && (g == null || g <= w)) goalWeight = (Math.round((w + 4) * 10) / 10.0).toString()
                    }
                }
                Step.ABOUT -> {
                    Spacer(Modifier.height(12.dp))
                    Title("About you")
                    Sub("Used only to work out your daily needs.")
                    Spacer(Modifier.height(20.dp))
                    Segmented(
                        options = listOf("Male", "Female"),
                        selected = if (sex == Sex.MALE) 0 else 1,
                        onSelect = { sex = if (it == 0) Sex.MALE else Sex.FEMALE },
                        height = 48.dp
                    )
                    Spacer(Modifier.height(10.dp))
                    FieldRow("Age", age, "yrs", { age = it.filter(Char::isDigit).take(3) }, keyboard = KeyboardType.Number)
                    Spacer(Modifier.height(10.dp))
                    FieldRow("Height", height, "cm", { height = it.take(6) })
                    Spacer(Modifier.height(10.dp))
                    FieldRow("Weight", weight, "kg", { weight = it.take(6) })
                    if (goal != GoalType.MAINTAIN) {
                        Spacer(Modifier.height(10.dp))
                        FieldRow("Goal weight", goalWeight, "kg", { goalWeight = it.take(6) })
                    }
                }
                Step.PACE -> {
                    Spacer(Modifier.height(12.dp))
                    Title(if (goal == GoalType.GAIN) "Gain how fast?" else "Lose how fast?")
                    Sub("A slower pace is easier to keep up.")
                    Spacer(Modifier.height(20.dp))
                    OptionCard("Gentle · 0.25 kg a week", "Easiest to stick to", pace == 0.25) { pace = 0.25 }
                    Spacer(Modifier.height(10.dp))
                    OptionCard("Steady · 0.5 kg a week", "The best balance for most people", pace == 0.5, tag = "Recommended") { pace = 0.5 }
                    Spacer(Modifier.height(10.dp))
                    OptionCard("Faster · 0.75 kg a week", "Needs more discipline", pace == 0.75) { pace = 0.75 }
                }
                Step.FOOD -> {
                    Spacer(Modifier.height(12.dp))
                    Title("How do you eat?")
                    Sub("So suggestions fit your plate.")
                    Spacer(Modifier.height(20.dp))
                    SmallLabel("Diet")
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DIETS.forEachIndexed { i, l -> ChoiceChip(l, diet == i, { diet = i }) }
                    }
                    Spacer(Modifier.height(22.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        SmallLabel("Allergies or foods to avoid")
                        Text("  (optional)", fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Ghost)
                    }
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ALLERGIES.forEach { l ->
                            ChoiceChip(l, avoid.contains(l), { if (avoid.contains(l)) avoid.remove(l) else avoid.add(l) })
                        }
                        custom.forEachIndexed { i, l -> ChoiceChip("$l  ×", true, { custom.removeAt(i) }) }
                        if (!adding) {
                            ChoiceChip("+ Add", false, { adding = true })
                        }
                    }
                    if (adding) {
                        Spacer(Modifier.height(10.dp))
                        val commit = {
                            val t = draft.trim()
                            val exists = (custom + ALLERGIES).any { it.equals(t, ignoreCase = true) }
                            if (t.isNotEmpty() && !exists) custom.add(t.replaceFirstChar { it.uppercase() })
                            draft = ""; adding = false
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(AppColors.Surface)
                                .border(1.5.dp, AppColors.Blue, RoundedCornerShape(26.dp))
                                .padding(start = 18.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = draft,
                                onValueChange = { draft = it.take(30) },
                                singleLine = true,
                                cursorBrush = SolidColor(AppColors.Ink),
                                textStyle = TextStyle(fontFamily = DmSans, fontSize = 16.sp, color = AppColors.Ink),
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Ink)
                                    .clickable { commit() }
                                    .padding(horizontal = 18.dp),
                                contentAlignment = Alignment.Center
                            ) { Text("Add", color = AppColors.Surface, fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        }
                    }
                }
                Step.PLAN -> {
                    val w = weightN ?: 70.0
                    val body = BodyProfile(sex, ageN ?: 30, heightN ?: 170.0, w, ActivityLevel.LIGHT)
                    val goalWeightN = if (goal == GoalType.MAINTAIN) w else (goalN ?: w)
                    val input = GoalInput(goal, goalWeightN, pace)
                    val t = NutritionCalculator.targets(body, input)
                    val weeks = NutritionCalculator.weeksToGoal(w, goalWeightN, if (goal == GoalType.MAINTAIN) 0.0 else pace)
                    val cal = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, weeks) }
                    val date = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(cal.time)

                    Spacer(Modifier.height(24.dp))
                    AppLogo(size = 56.dp)
                    Spacer(Modifier.height(20.dp))
                    Title("Your plan is ready")
                    Spacer(Modifier.height(20.dp))
                    AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
                        Column(Modifier.padding(20.dp)) {
                            SmallLabel("Daily calories")
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    "%,d".format(Locale.getDefault(), t.calories),
                                    fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 52.sp, color = AppColors.Ink
                                )
                                Text("  kcal", fontFamily = DmSans, fontSize = 16.sp, color = AppColors.Muted, modifier = Modifier.padding(bottom = 10.dp))
                            }
                            Text(
                                when (goal) {
                                    GoalType.LOSE -> "Built to take off $pace kg a week."
                                    GoalType.GAIN -> "Built to add $pace kg a week."
                                    GoalType.MAINTAIN -> "Built to keep your weight steady."
                                },
                                fontFamily = DmSans, fontSize = 14.sp, color = AppColors.InkSoft
                            )
                            Spacer(Modifier.height(16.dp))
                            MacroPillRow(t.proteinG, t.carbsG, t.fatG)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            Text(
                                if (goal == GoalType.MAINTAIN) "Hold at ${trim(w)} kg" else "Reach ${trim(goalWeightN)} kg by $date",
                                fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink
                            )
                            Text(
                                if (goal == GoalType.MAINTAIN) "Log what you eat and we will keep you on track."
                                else "About $weeks weeks at $pace kg a week",
                                fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Estimated for a lightly active day. You can change any of this later in your profile.",
                        fontFamily = DmSans, fontSize = 13.sp, color = AppColors.Muted, modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // footer button
        Box(modifier = Modifier.padding(horizontal = AppDimens.ScreenGutter, vertical = 16.dp)) {
            when (current) {
                Step.WELCOME -> PrimaryButton("Login", onClick = {
                    performGoogleSignIn(context, credentialManager, scope) { stepIndex = 1 }
                }, modifier = Modifier.fillMaxWidth())
                Step.PLAN -> PrimaryButton(if (saving) "Saving…" else "Start tracking", onClick = {
                    val w = weightN ?: return@PrimaryButton
                    saving = true
                    val body = BodyProfile(sex, ageN ?: 30, heightN ?: 170.0, w, ActivityLevel.LIGHT)
                    val gw = if (goal == GoalType.MAINTAIN) w else (goalN ?: w)
                    val t = NutritionCalculator.targets(body, GoalInput(goal, gw, pace))
                    val tags = buildList {
                        add(DIETS[diet]); addAll(avoid); addAll(custom)
                    }.joinToString(",")
                    val profile = UserProfile(
                        name = Firebase.auth.currentUser?.displayName?.substringBefore(" ") ?: "",
                        sex = sex.name,
                        age = (ageN ?: 30).toLong(),
                        heightCm = heightN ?: 170.0,
                        activity = ActivityLevel.LIGHT.name,
                        dietTags = tags
                    )
                    val goalDoc = UserGoal(
                        id = "current",
                        calorieGoal = t.calories.toLong(),
                        proteinGoal = t.proteinG.toLong(),
                        carbsGoal = t.carbsG.toLong(),
                        fatGoal = t.fatG.toLong(),
                        goalType = goal.name,
                        goalWeightKg = gw,
                        startWeightKg = w,
                        paceKgPerWeek = if (goal == GoalType.MAINTAIN) 0.0 else pace,
                        manual = false
                    )
                    viewModel.saveOnboarding(profile, goalDoc, w) { onFinished() }
                }, enabled = !saving, modifier = Modifier.fillMaxWidth())
                Step.FOOD -> PrimaryButton("See my plan", onClick = { stepIndex = idx + 1 }, modifier = Modifier.fillMaxWidth())
                Step.ABOUT -> PrimaryButton("Continue", onClick = { stepIndex = idx + 1 }, enabled = aboutValid, modifier = Modifier.fillMaxWidth())
                else -> PrimaryButton("Continue", onClick = { stepIndex = idx + 1 }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun trim(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()

@Composable
private fun Title(text: String) {
    Text(text, style = MaterialTheme.typography.headlineMedium, color = AppColors.Ink)
}

@Composable
private fun Sub(text: String) {
    Spacer(Modifier.height(6.dp))
    Text(text, fontFamily = DmSans, fontSize = 15.sp, color = AppColors.Muted)
}

@Composable
private fun SmallLabel(text: String) {
    Text(text, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AppColors.Muted)
}

@Composable
private fun OptionCard(
    title: String,
    sub: String,
    selected: Boolean,
    tag: String? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(AppDimens.CardRadius)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.Surface)
            .border(2.dp, if (selected) AppColors.Ink else AppColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppColors.Ink)
                if (tag != null) {
                    Text(
                        tag,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AppColors.GreenTint)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AppColors.GreenDeep
                    )
                }
            }
            Text(sub, fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted)
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) AppColors.Ink else Color(0xFFE3E7EC)),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Box(Modifier.size(8.dp).clip(CircleShape).background(AppColors.Surface))
        }
    }
}
