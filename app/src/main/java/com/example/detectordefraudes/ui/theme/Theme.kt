package com.example.detectordefraudes.ui.theme



import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


// Paleta de cor escura personalizada
private val DarkColorScheme = darkColorScheme(
    primary = FuturisticAccent,
    onPrimary = Color.Black,
    background = FuturisticDarkBackground,
    onBackground = FuturisticTextPrimary,
    surface = FuturisticCard,
    onSurface = FuturisticTextSecondary
)

@Composable
fun DetectorDeFraudesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // pode customizar também depois
        content = content
    )
}
