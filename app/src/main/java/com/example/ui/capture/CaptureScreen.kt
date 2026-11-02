package com.example.ui.capture

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PhotoLibrary
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.kit.PrimaryButton
import com.example.ui.kit.RoundIconButton
import com.example.ui.theme.AppColors
import com.example.ui.theme.DmSans

/**
 * Full-screen camera with gallery, shutter and flash.
 * [onPhoto] gets the photo (already rotated upright) once the user takes or picks one.
 */
@Composable
fun CaptureScreen(onBack: () -> Unit, onPhoto: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var asked by remember { mutableStateOf(false) }
    var flashOn by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<String?>(null) }
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        asked = true
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val bmp = decodeScaled(context, uri)
            if (bmp != null) onPhoto(bmp) else failure = "Could not open that picture."
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (granted) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val view = PreviewView(ctx)
                    view.scaleType = PreviewView.ScaleType.FILL_CENTER
                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        try {
                            val provider = providerFuture.get()
                            val preview = Preview.Builder().build()
                            preview.setSurfaceProvider(view.surfaceProvider)
                            provider.unbindAll()
                            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                        } catch (e: Exception) {
                            failure = "The camera could not start."
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    view
                }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera access", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (asked) "Camera access is off. You can turn it on in Settings, or pick a photo from your gallery."
                    else "Allow the camera so you can photograph your meals.",
                    fontFamily = DmSans, fontSize = 15.sp, color = Color(0xFFC9D0DA), textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                if (!asked) PrimaryButton("Allow camera", onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            }
        }

        // top bar
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconButton(
                Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack,
                container = Color.Black.copy(alpha = 0.45f), tint = Color.White, bordered = false
            )
            Text(
                "Point at your meal",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White
            )
            RoundIconButton(
                if (flashOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                if (flashOn) "Turn flash off" else "Turn flash on",
                {
                    flashOn = !flashOn
                    imageCapture.flashMode = if (flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                },
                container = Color.Black.copy(alpha = 0.45f), tint = Color.White, bordered = false
            )
        }

        failure?.let {
            Text(
                it,
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                fontFamily = DmSans, fontSize = 15.sp, color = Color.White, textAlign = TextAlign.Center
            )
        }

        // bottom controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RoundIconButton(
                Icons.Rounded.PhotoLibrary, "Choose from gallery", { galleryLauncher.launch("image/*") },
                size = 52.dp, container = Color.Black.copy(alpha = 0.45f), tint = Color.White, bordered = false
            )
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(enabled = granted) {
                        imageCapture.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bmp = try {
                                        val raw = image.toBitmap()
                                        val rot = image.imageInfo.rotationDegrees
                                        if (rot == 0) raw else Bitmap.createBitmap(
                                            raw, 0, 0, raw.width, raw.height,
                                            Matrix().apply { postRotate(rot.toFloat()) }, true
                                        )
                                    } catch (e: Exception) {
                                        null
                                    } finally {
                                        image.close()
                                    }
                                    if (bmp != null) onPhoto(scaleDown(bmp)) else failure = "The photo could not be saved."
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    failure = "The photo could not be taken. Please try again."
                                }
                            }
                        )
                    }
                    .semantics { contentDescription = "Take photo" }
            )
            Box(Modifier.size(52.dp))
        }
    }
}

private const val MAX_SIDE = 1280

private fun scaleDown(src: Bitmap): Bitmap {
    val longest = maxOf(src.width, src.height)
    if (longest <= MAX_SIDE) return src
    val f = MAX_SIDE.toFloat() / longest
    return Bitmap.createScaledBitmap(src, (src.width * f).toInt(), (src.height * f).toInt(), true)
}

private fun decodeScaled(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE * 2) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        bmp?.let { scaleDown(it) }
    } catch (e: Exception) {
        null
    }
}
