package com.example.ui.capture

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalorieTrackerViewModel
import com.example.ui.kit.OutlineButton
import com.example.ui.kit.PrimaryButton
import com.example.ui.kit.ScreenHeader
import com.example.ui.theme.AppColors
import com.example.ui.theme.DmSans

/**
 * Waits for the AI result. Moves on to Meal detail as soon as a meal is ready.
 * On failure it shows the error with Try again, Search food and Describe it. It never invents values.
 */
@Composable
fun AnalysingScreen(
    viewModel: CalorieTrackerViewModel,
    onBack: () -> Unit,
    onReady: () -> Unit,
    onSearchFood: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val failed = state.errorMessage != null && !state.isAnalyzing

    LaunchedEffect(state.pendingMeal, state.isAnalyzing) {
        if (state.pendingMeal != null && !state.isAnalyzing) onReady()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(AppColors.Background).statusBarsPadding()
    ) {
        ScreenHeader(title = if (failed) "Couldn't identify this" else "Analysing", onBack = {
            viewModel.clearError()
            onBack()
        })
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val bmp = state.failedAiBitmap
            if (bmp != null) {
                Image(
                    bmp.asImageBitmap(), contentDescription = "Your meal photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(28.dp))
                )
                Spacer(Modifier.height(24.dp))
            }
            if (!failed) {
                CircularProgressIndicator(color = AppColors.Green, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    state.analyzingMessage.ifBlank { "Working out your meal..." },
                    fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    color = AppColors.Ink, textAlign = TextAlign.Center
                )
            } else {
                Text(
                    state.errorMessage.orEmpty(),
                    fontFamily = DmSans, fontSize = 15.sp, color = AppColors.Muted, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                PrimaryButton("Try again", onClick = { viewModel.retryAiAnalysis() }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlineButton("Search food", onClick = {
                    viewModel.clearError()
                    onSearchFood()
                }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                OutlineButton("Describe it", onClick = {
                    viewModel.clearError()
                    onBack()
                }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
