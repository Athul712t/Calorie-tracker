package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DarkPill
import com.example.ui.theme.DarkPillText
import com.example.ui.theme.NothingRed
import com.example.ui.theme.SpaceMonoFont
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextMuted

@Composable
fun ChatCameraInputBar(
    isAnalyzing: Boolean,
    analyzingMessage: String,
    onAnalyzeMeal: (text: String, bitmap: Bitmap?, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mealText by remember { mutableStateOf("") }
    var contextNotes by remember { mutableStateOf("") }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCameraPickerModal by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
        }
    }

    // Photo gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                capturedBitmap = bitmap
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
            .testTag("chat_camera_input_bar"),
        color = SurfaceWhite,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Attached Photo Preview & Notes
            AnimatedVisibility(visible = capturedBitmap != null) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        capturedBitmap?.let { bmp ->
                            Box(modifier = Modifier.size(64.dp)) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Food preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, BorderLight, RoundedCornerShape(10.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(DarkPill)
                                        .clickable { capturedBitmap = null }
                                        .testTag("remove_photo_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = DarkPillText,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        OutlinedTextField(
                            value = contextNotes,
                            onValueChange = { contextNotes = it },
                            placeholder = { Text("Context (e.g. half plate, cooked in ghee)", fontSize = 11.sp, color = TextMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .testTag("context_notes_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkPill,
                                unfocusedBorderColor = BorderLight,
                                focusedTextColor = TextBlack,
                                unfocusedTextColor = TextBlack,
                                focusedContainerColor = SurfaceElevated,
                                unfocusedContainerColor = SurfaceElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }
                }
            }

            // Main chat input bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Camera Button (Tapping opens camera/photo options)
                IconButton(
                    onClick = { showCameraPickerModal = !showCameraPickerModal },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (capturedBitmap != null) SurfaceElevated else SurfaceElevated)
                        .testTag("camera_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Open camera scanner",
                        tint = if (capturedBitmap != null) NothingRed else TextBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Input Field
                OutlinedTextField(
                    value = mealText,
                    onValueChange = { mealText = it },
                    placeholder = {
                        Text(
                            text = if (capturedBitmap != null) "Describe photo (optional)..." else "What did you eat? (e.g. 2 rotis & dal)...",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("meal_chat_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkPill,
                        unfocusedBorderColor = BorderLight,
                        focusedTextColor = TextBlack,
                        unfocusedTextColor = TextBlack,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    ),
                    maxLines = 2,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (mealText.isNotBlank() || capturedBitmap != null) {
                                keyboardController?.hide()
                                onAnalyzeMeal(mealText, capturedBitmap, contextNotes)
                                mealText = ""
                                contextNotes = ""
                                capturedBitmap = null
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                val canSubmit = (mealText.isNotBlank() || capturedBitmap != null) && !isAnalyzing
                IconButton(
                    onClick = {
                        if (canSubmit) {
                            keyboardController?.hide()
                            onAnalyzeMeal(mealText, capturedBitmap, contextNotes)
                            mealText = ""
                            contextNotes = ""
                            capturedBitmap = null
                        }
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (canSubmit) DarkPill else SurfaceElevated)
                        .testTag("analyze_meal_button")
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = DarkPillText,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send meal entry",
                            tint = if (canSubmit) DarkPillText else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Camera / Gallery quick chooser row
            AnimatedVisibility(visible = showCameraPickerModal) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                            .clickable {
                                showCameraPickerModal = false
                                cameraLauncher.launch(null)
                            }
                            .testTag("take_photo_option"),
                        color = SurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = TextBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Take Photo",
                                fontFamily = SpaceMonoFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextBlack
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                            .clickable {
                                showCameraPickerModal = false
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("choose_gallery_option"),
                        color = SurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = TextBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Pick Gallery",
                                fontFamily = SpaceMonoFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextBlack
                            )
                        }
                    }
                }
            }

            // Analyzing status
            if (isAnalyzing) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NothingRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = analyzingMessage,
                        fontFamily = SpaceMonoFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextBlack
                    )
                }
            }
        }
    }
}
