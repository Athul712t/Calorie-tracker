package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.components.CalorieMacroSummaryCard
import com.example.ui.components.ChatCameraInputBar
import com.example.ui.components.GoalSettingsDialog
import com.example.ui.components.IndianFoodSuggestionsSection
import com.example.ui.components.MealConfirmationDialog
import com.example.ui.components.MealListSection
import com.example.ui.components.QuickAddBar
import com.example.ui.components.SavedMenuSheet
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CanvasBackground
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DarkPillText
import com.example.ui.theme.NothingRed
import com.example.ui.theme.SpaceMonoFont
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextMuted
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalorieTrackerScreen(
    viewModel: CalorieTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showMenuSheet by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            currentUser?.uid?.let { uid ->
                viewModel.initializeUser(uid)
            }
        }
        Firebase.auth.addAuthStateListener(listener)
    }

    LaunchedEffect(currentUser) {
        val uid = currentUser?.uid ?: "local_guest_user"
        viewModel.initializeUser(uid)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        containerColor = CanvasBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                selectedDate = state.selectedDate,
                onPreviousDate = {
                    val prev = shiftDate(state.selectedDate, -1)
                    viewModel.selectDate(prev)
                },
                onNextDate = {
                    val next = shiftDate(state.selectedDate, 1)
                    viewModel.selectDate(next)
                },
                onTodayClick = {
                    viewModel.selectDate(getTodayDateString())
                },
                currentUserEmail = currentUser?.email,
                onAccountClick = { showAccountDialog = true },
                onMenuClick = { showMenuSheet = true }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // Inline AI Error / Retry & Fallback recovery banner
                if (state.errorMessage != null && state.failedAiQuery != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("ai_error_recovery_banner"),
                        color = SurfaceWhite,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NothingRed)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(NothingRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI ESTIMATION ERROR",
                                        fontFamily = SpaceMonoFont,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NothingRed
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.clearError() },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss error",
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.errorMessage ?: "",
                                fontSize = 11.sp,
                                color = TextBlack,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.retryAiAnalysis() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkPill,
                                        contentColor = DarkPillText
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .testTag("retry_ai_button"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("RETRY AI", fontFamily = SpaceMonoFont, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.useIndianDatabaseFallback() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextBlack),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .testTag("use_indian_db_button"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("USE INDIAN DB", fontFamily = SpaceMonoFont, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                ChatCameraInputBar(
                    isAnalyzing = state.isAnalyzing,
                    analyzingMessage = state.analyzingMessage,
                    onAnalyzeMeal = { text, bitmap, notes ->
                        viewModel.analyzeMealInput(text, bitmap, notes)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) {
            // 1. Calorie and Macro Summary Card (Hero Remaining Calories & Linear Meters)
            item {
                CalorieMacroSummaryCard(
                    state = state,
                    onEditGoalsClick = { showGoalDialog = true }
                )
            }

            // 2. Quick Add Bar (Time of Day Eating Pattern Suggestions)
            item {
                QuickAddBar(
                    menuItems = state.menuItems,
                    onQuickAddMenuItem = { item, multiplier ->
                        viewModel.quickAddFromMenuItem(item, multiplier)
                    },
                    onQuickAddIndianFood = { food, multiplier ->
                        viewModel.quickAddIndianFood(food, multiplier)
                    },
                    onOpenFullMenuClick = { showMenuSheet = true }
                )
            }

            // 3. Indian Food Suggestions for Remaining Calorie & Macro Target
            item {
                IndianFoodSuggestionsSection(
                    suggestions = state.indianSuggestions,
                    remainingCalories = state.remainingCalories,
                    remainingProtein = state.remainingProtein,
                    isGenerating = state.isGeneratingSuggestions,
                    onRefresh = { viewModel.refreshIndianSuggestions() },
                    onQuickAdd = { food ->
                        viewModel.quickAddIndianFood(food, 1.0)
                    }
                )
            }

            // 4. Today's Logged Meals Timeline
            item {
                MealListSection(
                    meals = state.meals,
                    onDeleteMeal = { mealId ->
                        viewModel.deleteMeal(mealId)
                    }
                )
            }
        }
    }

    // Meal Confirmation / Review Dialog (Cal AI style review)
    state.pendingMeal?.let { pending ->
        MealConfirmationDialog(
            initialData = pending,
            onConfirm = { edited ->
                viewModel.confirmAndSaveMeal(edited)
            },
            onDismiss = {
                viewModel.dismissPendingMeal()
            }
        )
    }

    // Goal Settings Dialog
    if (showGoalDialog) {
        GoalSettingsDialog(
            currentGoal = state.goal,
            onSaveGoals = { cal, p, c, f ->
                viewModel.updateGoals(cal, p, c, f)
            },
            onDismiss = { showGoalDialog = false }
        )
    }

    // Saved Menu Bottom Sheet
    if (showMenuSheet) {
        SavedMenuSheet(
            menuItems = state.menuItems,
            onQuickAdd = { item, multiplier ->
                viewModel.quickAddFromMenuItem(item, multiplier)
                showMenuSheet = false
            },
            onAddCustomItem = { name, cal, p, c, f, portion, cat ->
                viewModel.addCustomMenuItem(name, cal, p, c, f, portion, cat)
            },
            onDismiss = { showMenuSheet = false }
        )
    }

    // Account / Google Sign-In Dialog
    if (showAccountDialog) {
        AccountAuthDialog(
            user = currentUser,
            onGoogleSignIn = {
                performGoogleSignIn(context, credentialManager, coroutineScope, onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                    showAccountDialog = false
                })
            },
            onSignOut = {
                Firebase.auth.signOut()
                coroutineScope.launch {
                    try {
                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                    } catch (e: Exception) {
                        Log.e("Auth", "Failed to clear credentials", e)
                    }
                    currentUser = null
                    showAccountDialog = false
                }
            },
            onDismiss = { showAccountDialog = false }
        )
    }
}

@Composable
private fun TopAppBar(
    selectedDate: String,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onTodayClick: () -> Unit,
    currentUserEmail: String?,
    onAccountClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        color = CanvasBackground
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Brand with Dot Accent
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("app_brand_logo")
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(NothingRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CALORIETRACK",
                        fontFamily = SpaceMonoFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextBlack
                    )
                    Text(
                        text = "INDIAN NUTRITION",
                        fontFamily = SpaceMonoFont,
                        fontSize = 9.sp,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Date Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceWhite)
                    .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = onPreviousDate,
                    modifier = Modifier.size(28.dp).testTag("prev_date_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Day",
                        tint = TextBlack,
                        modifier = Modifier.size(15.dp)
                    )
                }

                val isToday = selectedDate == getTodayDateString()
                Text(
                    text = if (isToday) "TODAY" else formatDateShort(selectedDate),
                    fontFamily = SpaceMonoFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) NothingRed else TextBlack,
                    modifier = Modifier
                        .clickable { onTodayClick() }
                        .padding(horizontal = 6.dp)
                        .testTag("date_label")
                )

                IconButton(
                    onClick = onNextDate,
                    modifier = Modifier.size(28.dp).testTag("next_date_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Day",
                        tint = TextBlack,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Right icons: Menu and Account
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .border(1.dp, BorderLight, CircleShape)
                        .testTag("top_menu_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = "Menu Items",
                        tint = TextBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onAccountClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .border(1.dp, BorderLight, CircleShape)
                        .testTag("top_account_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Account",
                        tint = if (currentUserEmail != null) NothingRed else TextBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountAuthDialog(
    user: com.google.firebase.auth.FirebaseUser?,
    onGoogleSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    var isSigningIn by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(
                text = if (user != null) "ACCOUNT // CLOUD SYNC" else "SIGN IN WITH GOOGLE",
                fontFamily = SpaceMonoFont,
                color = TextBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (user != null) {
                    Text(
                        text = "Signed in as: ${user.email ?: user.displayName ?: "User"}",
                        fontSize = 13.sp,
                        color = TextBlack,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Your meals, daily calorie history, custom menu items, and goals are securely synced with Firebase Firestore.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                } else {
                    Text(
                        text = "Sign in with Google to securely backup and sync your meals, custom menu items, and daily calorie goals across devices.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        },
        confirmButton = {
            if (user != null) {
                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = TextBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SIGN OUT", fontFamily = SpaceMonoFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        isSigningIn = true
                        onGoogleSignIn()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPill, contentColor = DarkPillText),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("sign_in_with_google_button"),
                    enabled = !isSigningIn
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DarkPillText, strokeWidth = 2.dp)
                    } else {
                        Text("SIGN IN WITH GOOGLE", fontFamily = SpaceMonoFont, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CLOSE", fontFamily = SpaceMonoFont, color = TextMuted, fontSize = 11.sp)
            }
        }
    )
}

fun performGoogleSignIn(
    context: Context,
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onAuthSuccess: () -> Unit
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        Log.e("Auth", "Missing default_web_client_id", e)
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled: ${e.message}")
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
        }
    }
}

fun shiftDate(dateStr: String, days: Int): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return try {
        val date = sdf.parse(dateStr) ?: Date()
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, days)
        sdf.format(cal.time)
    } catch (e: Exception) {
        dateStr
    }
}

fun formatDateShort(dateStr: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outSdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return try {
        val date = sdf.parse(dateStr) ?: Date()
        outSdf.format(date).uppercase(Locale.getDefault())
    } catch (e: Exception) {
        dateStr
    }
}
