package com.example.ui.kit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppDimens
import com.example.ui.theme.DmSans

/** White card with the design's border and radius. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    radius: Dp = AppDimens.CardRadius,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Border, shape),
        content = content
    )
}

/** Circular progress ring. [content] is drawn in the middle. */
@Composable
fun Ring(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    stroke: Dp = 9.dp,
    track: Color = AppColors.Track,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            val topLeft = Offset(s / 2f, s / 2f)
            val arc = Size(size.width - s, size.height - s)
            drawArc(
                color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arc, style = Stroke(width = s)
            )
            val p = progress.coerceIn(0f, 1f)
            if (p > 0f) {
                drawArc(
                    color = color, startAngle = -90f, sweepAngle = 360f * p, useCenter = false,
                    topLeft = topLeft, size = arc, style = Stroke(width = s, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(AppDimens.ButtonHeight),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Ink,
            contentColor = AppColors.Surface,
            disabledContainerColor = AppColors.Ink.copy(alpha = 0.4f),
            disabledContentColor = AppColors.Surface
        )
    ) {
        Text(text, fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, AppColors.BorderStrong),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Surface,
            contentColor = AppColors.Ink,
            disabledContainerColor = AppColors.Surface,
            disabledContentColor = AppColors.Ghost
        )
    ) {
        Text(text, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

/** Round icon-only button. Always give it a [contentDescription]. */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = AppDimens.IconButton,
    container: Color = AppColors.Surface,
    tint: Color = AppColors.Ink,
    bordered: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container)
            .then(if (bordered) Modifier.border(1.dp, AppColors.BorderStrong, CircleShape) else Modifier)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.45f))
    }
}

/** Screen header: back button, centred title. */
@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimens.ScreenGutter, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.Ink
        )
        Spacer(Modifier.width(AppDimens.IconButton))
    }
}

/** Pill-shaped segmented control. */
@Composable
fun Segmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(AppColors.Segment)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height - 8.dp)
                    .clip(CircleShape)
                    .background(if (on) AppColors.Surface else Color.Transparent)
                    .clickable(role = Role.RadioButton) { onSelect(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontFamily = DmSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (on) AppColors.Ink else AppColors.Muted
                )
            }
        }
    }
}

/** Small selectable chip. */
@Composable
fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) AppColors.Ink else AppColors.Surface
    val fg = if (selected) AppColors.Surface else AppColors.InkSoft
    val border = if (selected) AppColors.Ink else AppColors.BorderStrong
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = fg)
    }
}

/** Labelled number input row, for example "Age   30 yrs". */
@Composable
fun FieldRow(
    label: String,
    value: String,
    unit: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboard: KeyboardType = KeyboardType.Decimal
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppDimens.FieldHeight)
            .clip(shape)
            .background(AppColors.Surface)
            .border(1.5.dp, AppColors.Border, shape)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = DmSans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.Ink)
        Spacer(Modifier.weight(1f))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            cursorBrush = SolidColor(AppColors.Ink),
            textStyle = TextStyle(
                fontFamily = DmSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AppColors.Ink,
                textAlign = TextAlign.End
            ),
            modifier = Modifier
                .width(96.dp)
                .semantics { this.contentDescription = "$label in $unit" }
        )
        Text(
            unit,
            modifier = Modifier.width(32.dp).padding(start = 6.dp),
            fontFamily = DmSans,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = AppColors.Muted
        )
    }
}

/** Coloured pill showing a macro value, for example "P 120 g". */
@Composable
fun MacroPill(text: String, dot: Color, deep: Color, tint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tint)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        Text(text, fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = deep)
    }
}

/** The three macro pills in one row. */
@Composable
fun MacroPillRow(proteinG: Int, carbsG: Int, fatG: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MacroPill("P $proteinG g", AppColors.Protein, AppColors.ProteinDeep, AppColors.ProteinTint, Modifier.weight(1f))
        MacroPill("C $carbsG g", AppColors.Carbs, AppColors.CarbsDeep, AppColors.CarbsTint, Modifier.weight(1f))
        MacroPill("F $fatG g", AppColors.Fat, AppColors.FatDeep, AppColors.FatTint, Modifier.weight(1f))
    }
}

/** Section label in small capitals, as used on the edit screens. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier = modifier.padding(horizontal = 4.dp),
        fontFamily = DmSans,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp,
        color = AppColors.Muted
    )
}

/** App logo: dark rounded square with a ring and a green dot. */
@Composable
fun AppLogo(size: Dp = 44.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size).semantics { contentDescription = "App logo" }) {
        val w = this.size.width
        drawRoundRect(
            color = AppColors.Ink,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.32f, w * 0.32f)
        )
        val s = w * 0.08f
        val r = w * 0.23f
        drawArc(
            color = Color.White, startAngle = -90f, sweepAngle = 270f, useCenter = false,
            topLeft = Offset(w / 2f - r, w / 2f - r), size = Size(r * 2f, r * 2f),
            style = Stroke(width = s, cap = StrokeCap.Round)
        )
        drawCircle(color = AppColors.Green, radius = w * 0.073f, center = Offset(w / 2f, w / 2f))
    }
}

/** Decodes a small base64 JPEG stored with a meal. Returns null if there is none or it is invalid. */
@Composable
fun rememberBase64Image(base64: String?): androidx.compose.ui.graphics.ImageBitmap? {
    return androidx.compose.runtime.remember(base64) {
        if (base64.isNullOrBlank()) {
            null
        } else {
            try {
                val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }
}
