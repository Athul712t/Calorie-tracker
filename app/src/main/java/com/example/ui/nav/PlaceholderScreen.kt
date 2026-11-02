package com.example.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppColors

/** Stand-in for a screen that has a design but is not built yet. */
@Composable
fun PlaceholderScreen(info: ScreenInfo, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(info.title, style = MaterialTheme.typography.headlineMedium, color = AppColors.Ink)
        Spacer(Modifier.height(8.dp))
        Text("Design: ${info.designFile}", style = MaterialTheme.typography.bodyMedium, color = AppColors.Muted)
        Text("Not built yet", style = MaterialTheme.typography.bodyMedium, color = AppColors.Muted)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Ink, contentColor = AppColors.Surface)
        ) { Text("Back") }
    }
}

/** Developer-only list of every route. Set it as the start destination to click through the app. */
@Composable
fun ScreenIndex(onOpen: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Text("Screens", style = MaterialTheme.typography.headlineMedium, color = AppColors.Ink)
            Spacer(Modifier.height(8.dp))
        }
        items(ALL_SCREENS) { s ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Surface, MaterialTheme.shapes.large)
                    .clickable { onOpen(s.route) }
                    .padding(16.dp)
            ) {
                Text(s.title, style = MaterialTheme.typography.titleMedium, color = AppColors.Ink)
                Text(s.designFile, style = MaterialTheme.typography.bodyMedium, color = AppColors.Muted)
            }
        }
    }
}
