package com.example.ui.progress

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ProgressPhoto
import com.example.ui.CalorieTrackerViewModel
import com.example.ui.kit.AppCard
import com.example.ui.kit.PrimaryButton
import com.example.ui.kit.ScreenHeader
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppDimens
import com.example.ui.theme.DmSans
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val ANGLES = listOf("FRONT" to "Front", "LEFT" to "Left", "RIGHT" to "Right", "BACK" to "Back")
private val TILE_TINTS = mapOf(
    "FRONT" to Color(0xFFE9EFFD), "LEFT" to Color(0xFFE8F6EF), "RIGHT" to Color(0xFFFDF3E3), "BACK" to Color(0xFFF3EEFE)
)

private fun prettyDate(key: String): String = try {
    SimpleDateFormat("MMM d", Locale.getDefault()).format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key)!!)
} catch (e: Exception) { key }

private fun fmt1(v: Double) = String.format(Locale.getDefault(), "%.1f", v)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(
    viewModel: CalorieTrackerViewModel,
    onBack: () -> Unit,
    onCapture: (angle: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val dates = remember(state.photos) { state.photos.map { it.dateString }.distinct().sorted() }
    var index by remember { mutableStateOf(-1) } // -1 = latest
    val current = if (dates.isEmpty()) null else dates[(if (index < 0) dates.lastIndex else index).coerceIn(0, dates.lastIndex)]
    val currentIdx = if (current == null) 0 else dates.indexOf(current)
    var viewerAngle by remember { mutableStateOf<String?>(null) }
    var pickAngle by remember { mutableStateOf(false) }

    fun weightFor(date: String): Double? {
        state.photos.firstOrNull { it.dateString == date && it.weightKg > 0 }?.let { return it.weightKg }
        return state.weights.firstOrNull { it.dateString <= date }?.kg
    }

    Box(Modifier.fillMaxSize().background(AppColors.Background).statusBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("Progress photos", onBack)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = AppDimens.ScreenGutter).padding(bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (current == null) {
                    EmptyPhotos()
                } else {
                    Text(prettyDate(current), fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppColors.Ink)
                    val forDate = state.photos.filter { it.dateString == current }
                    ANGLES.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { (angle, label) ->
                                PhotoTile(
                                    viewModel, label, angle, forDate.firstOrNull { it.angle == angle },
                                    Modifier.weight(1f),
                                    onOpen = { viewerAngle = angle },
                                    onAdd = { onCapture(angle) }
                                )
                            }
                        }
                    }

                    if (dates.size >= 2) {
                        val w = weightFor(current)
                        val start = weightFor(dates.first())
                        AppCard(Modifier.fillMaxWidth(), radius = AppDimens.CardRadiusLarge) {
                            Column(Modifier.padding(18.dp)) {
                                Text("Memory lane", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Ink)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    listOfNotNull(
                                        prettyDate(current),
                                        w?.let { "${fmt1(it)} kg" },
                                        if (w != null && start != null && currentIdx > 0) {
                                            val d = start - w
                                            if (abs(d) < 0.05) "no change since start"
                                            else (if (d > 0) "−" else "+") + fmt1(abs(d)) + " kg since start"
                                        } else null
                                    ).joinToString(" · "),
                                    fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted
                                )
                                Slider(
                                    value = currentIdx.toFloat(),
                                    onValueChange = { index = it.roundToInt().coerceIn(0, dates.lastIndex) },
                                    valueRange = 0f..dates.lastIndex.toFloat(),
                                    steps = (dates.size - 2).coerceAtLeast(0),
                                    colors = SliderDefaults.colors(
                                        thumbColor = AppColors.Ink, activeTrackColor = AppColors.Ink,
                                        inactiveTrackColor = AppColors.Track,
                                        activeTickColor = Color.White, inactiveTickColor = AppColors.Ghost
                                    ),
                                    modifier = Modifier.semantics { contentDescription = "Check-in date" }
                                )
                                Row(Modifier.fillMaxWidth()) {
                                    Text(prettyDate(dates.first()), Modifier.weight(1f), fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted)
                                    Text(prettyDate(dates.last()), fontFamily = DmSans, fontSize = 12.sp, color = AppColors.Muted)
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(20.dp)) {
            PrimaryButton("Capture", onClick = { pickAngle = true }, modifier = Modifier.fillMaxWidth())
        }
    }

    if (pickAngle) {
        ModalBottomSheet(
            onDismissRequest = { pickAngle = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = AppColors.Surface,
            shape = RoundedCornerShape(topStart = AppDimens.SheetRadius, topEnd = AppDimens.SheetRadius)
        ) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
                Text("Which angle?", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.Ink)
                Text("Taking one again today replaces it.", fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted)
                Spacer(Modifier.height(14.dp))
                ANGLES.forEach { (angle, label) ->
                    Row(
                        Modifier.fillMaxWidth().clickable { pickAngle = false; onCapture(angle) }.padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = AppColors.Muted)
                        Text(label, Modifier.padding(start = 14.dp), fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.Ink)
                    }
                }
            }
        }
    }

    val open = viewerAngle
    if (open != null && current != null) {
        PhotoViewer(
            viewModel = viewModel,
            date = current,
            photos = state.photos.filter { it.dateString == current },
            startAngle = open,
            onClose = { viewerAngle = null }
        )
    }
}

@Composable
private fun EmptyPhotos() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No photos yet", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.Ink)
        Spacer(Modifier.height(6.dp))
        Text(
            "Take front, side and back photos every couple of weeks. You can slide through them later to see the change.",
            fontFamily = DmSans, fontSize = 14.sp, color = AppColors.Muted, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PhotoTile(
    viewModel: CalorieTrackerViewModel,
    label: String,
    angle: String,
    photo: ProgressPhoto?,
    modifier: Modifier,
    onOpen: () -> Unit,
    onAdd: () -> Unit
) {
    val bmp by produceState<Bitmap?>(null, photo?.storagePath) {
        value = photo?.let { viewModel.loadPhoto(it.storagePath) }
    }
    Box(
        modifier.aspectRatio(0.75f).clip(RoundedCornerShape(AppDimens.CardRadius))
            .background(TILE_TINTS[angle] ?: AppColors.Chip)
            .clickable { if (photo != null) onOpen() else onAdd() }
            .semantics { contentDescription = if (photo != null) "Open $label photo" else "Add $label photo" },
        contentAlignment = Alignment.Center
    ) {
        bmp?.let { Image(it.asImageBitmap(), "$label photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
        if (photo == null) {
            Text("+ Add", fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.InkSoft)
        }
        Text(
            label,
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp).clip(CircleShape)
                .background(Color.White.copy(alpha = 0.85f)).padding(horizontal = 10.dp, vertical = 4.dp),
            fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppColors.Ink
        )
    }
}

@Composable
private fun PhotoViewer(
    viewModel: CalorieTrackerViewModel,
    date: String,
    photos: List<ProgressPhoto>,
    startAngle: String,
    onClose: () -> Unit
) {
    var angle by remember { mutableStateOf(startAngle) }
    val photo = photos.firstOrNull { it.angle == angle }
    val bmp by produceState<Bitmap?>(null, photo?.storagePath) {
        value = photo?.let { viewModel.loadPhoto(it.storagePath) }
    }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color(0xFF101319))) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(prettyDate(date), Modifier.weight(1f), fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Icon(
                        Icons.Rounded.Close, contentDescription = "Close", tint = Color.White,
                        modifier = Modifier.clip(CircleShape).clickable(onClick = onClose).padding(8.dp)
                    )
                }
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    bmp?.let { Image(it.asImageBitmap(), "$angle photo", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize()) }
                    if (photo == null) Text("No photo for this angle", fontFamily = DmSans, fontSize = 15.sp, color = Color(0xFFC9D0DA))
                }
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ANGLES.forEach { (a, label) ->
                        val on = a == angle
                        Box(
                            Modifier.weight(1f).height(40.dp).clip(CircleShape)
                                .background(if (on) Color.White else Color.White.copy(alpha = 0.12f))
                                .clickable { angle = a },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (on) Color(0xFF101319) else Color.White)
                        }
                    }
                }
            }
        }
    }
}
