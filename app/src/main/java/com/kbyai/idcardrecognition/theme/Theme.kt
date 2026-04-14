package com.kbyai.idcardrecognition.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val NobleColorScheme = lightColorScheme(
    primary = NobleBlue,
    onPrimary = Color.White,
    primaryContainer = NobleBlueLight,
    onPrimaryContainer = NobleTextPrimary,
    secondary = NobleGold,
    onSecondary = Color.White,
    secondaryContainer = NobleAccent,
    onSecondaryContainer = NobleTextPrimary,
    background = NobleBackground,
    onBackground = NobleTextPrimary,
    surface = NobleSurface,
    onSurface = NobleTextPrimary,
    surfaceVariant = NobleAccent,
    onSurfaceVariant = NobleTextSecondary,
    outline = NobleBorder,
    outlineVariant = NobleBorder.copy(alpha = 0.65f),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

private val NobleShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NobleColorScheme,
        typography = Typography,
        shapes = NobleShapes,
        content = content,
    )
}
