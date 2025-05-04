package com.example.opendelhitransit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.opendelhitransit.data.AppTheme
import com.example.opendelhitransit.data.ThemePreferences

// Default dark and light color schemes
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryVariantDark,
    onPrimaryContainer = OnPrimaryDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryVariantDark,
    onSecondaryContainer = OnSecondaryDark,
    tertiary = SecondaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = OnSurfaceDark.copy(alpha = 0.7f),
    error = ErrorDark,
    onError = OnErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = OnPrimary,
    secondary = SecondaryColor,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = OnSecondary,
    tertiary = SecondaryColor,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Surface,
    onSurfaceVariant = OnSurface.copy(alpha = 0.7f),
    error = Error,
    onError = OnError
)

// Green theme color schemes
private val GreenLightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = OnPrimary,
    primaryContainer = GreenPrimaryVariant,
    onPrimaryContainer = OnPrimary,
    secondary = GreenSecondary,
    onSecondary = OnSecondary,
    secondaryContainer = GreenSecondaryVariant,
    onSecondaryContainer = OnSecondary,
    tertiary = GreenSecondary,
    background = GreenBackground,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Surface,
    onSurfaceVariant = OnSurface.copy(alpha = 0.7f),
    error = Error,
    onError = OnError
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = GreenPrimaryVariantDark,
    onPrimaryContainer = OnPrimaryDark,
    secondary = GreenSecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = GreenSecondaryVariantDark,
    onSecondaryContainer = OnSecondaryDark,
    tertiary = GreenSecondaryDark,
    background = GreenBackgroundDark,
    onBackground = OnBackgroundDark,
    surface = GreenSurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = GreenSurfaceDark,
    onSurfaceVariant = OnSurfaceDark.copy(alpha = 0.7f),
    error = ErrorDark,
    onError = OnErrorDark
)

// Blue theme color schemes
private val BlueLightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = OnPrimary,
    primaryContainer = BluePrimaryVariant,
    onPrimaryContainer = OnPrimary,
    secondary = BlueSecondary,
    onSecondary = OnSecondary,
    secondaryContainer = BlueSecondaryVariant,
    onSecondaryContainer = OnSecondary,
    tertiary = BlueSecondary,
    background = BlueBackground,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Surface,
    onSurfaceVariant = OnSurface.copy(alpha = 0.7f),
    error = Error,
    onError = OnError
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = BluePrimaryVariantDark,
    onPrimaryContainer = OnPrimaryDark,
    secondary = BlueSecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = BlueSecondaryVariantDark,
    onSecondaryContainer = OnSecondaryDark,
    tertiary = BlueSecondaryDark,
    background = BlueBackgroundDark,
    onBackground = OnBackgroundDark,
    surface = BlueSurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = BlueSurfaceDark,
    onSurfaceVariant = OnSurfaceDark.copy(alpha = 0.7f),
    error = ErrorDark,
    onError = OnErrorDark
)

// High Contrast theme color schemes
private val HighContrastLightColorScheme = lightColorScheme(
    primary = HighContrastPrimary,
    onPrimary = HighContrastOnPrimary,
    primaryContainer = HighContrastPrimaryVariant,
    onPrimaryContainer = HighContrastOnPrimary,
    secondary = HighContrastSecondary,
    onSecondary = HighContrastOnSecondary,
    secondaryContainer = HighContrastSecondaryVariant,
    onSecondaryContainer = HighContrastOnSecondary,
    tertiary = HighContrastSecondary,
    background = HighContrastBackground,
    onBackground = HighContrastOnBackground,
    surface = HighContrastSurface,
    onSurface = HighContrastOnSurface,
    surfaceVariant = HighContrastSurface.copy(alpha = 0.9f),
    onSurfaceVariant = HighContrastOnSurface.copy(alpha = 0.9f),
    error = Error,
    onError = OnError
)

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = HighContrastPrimaryDark,
    onPrimary = HighContrastOnPrimaryDark,
    primaryContainer = HighContrastPrimaryVariantDark,
    onPrimaryContainer = HighContrastOnPrimaryDark,
    secondary = HighContrastSecondaryDark,
    onSecondary = HighContrastOnSecondaryDark,
    secondaryContainer = HighContrastSecondaryVariantDark,
    onSecondaryContainer = HighContrastOnSecondaryDark,
    tertiary = HighContrastSecondaryDark,
    background = HighContrastBackgroundDark,
    onBackground = HighContrastOnBackgroundDark,
    surface = HighContrastSurfaceDark,
    onSurface = HighContrastOnSurfaceDark,
    surfaceVariant = HighContrastSurfaceDark.copy(alpha = 0.9f),
    onSurfaceVariant = HighContrastOnSurfaceDark.copy(alpha = 0.9f),
    error = ErrorDark,
    onError = OnErrorDark
)

// Color blind themes - Deuteranopia (Green blindness)
private val DeuteranopiaColorScheme = lightColorScheme(
    primary = DeuteranopiaPrimary,
    onPrimary = DeuteranopiaOnPrimary,
    primaryContainer = DeuteranopiaPrimaryVariant,
    onPrimaryContainer = DeuteranopiaOnPrimary,
    secondary = DeuteranopiaSecondary,
    onSecondary = DeuteranopiaOnSecondary,
    secondaryContainer = DeuteranopiaSecondaryVariant,
    onSecondaryContainer = DeuteranopiaOnSecondary,
    tertiary = DeuteranopiaSecondary,
    background = DeuteranopiaBackground,
    onBackground = OnBackground,
    surface = DeuteranopiaSurface,
    onSurface = OnSurface,
    surfaceVariant = DeuteranopiaSurface.copy(alpha = 0.9f),
    onSurfaceVariant = OnSurface.copy(alpha = 0.7f),
    error = Error,
    onError = OnError
)

// Color blind themes - Protanopia (Red blindness)
private val ProtanopiaColorScheme = lightColorScheme(
    primary = ProtanopiaPrimary,
    onPrimary = ProtanopiaOnPrimary,
    primaryContainer = ProtanopiaPrimaryVariant,
    onPrimaryContainer = ProtanopiaOnPrimary,
    secondary = ProtanopiaSecondary,
    onSecondary = ProtanopiaOnSecondary,
    secondaryContainer = ProtanopiaSecondaryVariant, 
    onSecondaryContainer = ProtanopiaOnSecondary,
    tertiary = ProtanopiaSecondary,
    background = ProtanopiaBackground,
    onBackground = OnBackground,
    surface = ProtanopiaSurface,
    onSurface = OnSurface,
    surfaceVariant = ProtanopiaSurface.copy(alpha = 0.9f),
    onSurfaceVariant = OnSurface.copy(alpha = 0.7f),
    error = Error,
    onError = OnError
)

// Color blind themes - Tritanopia (Blue blindness)
private val TritanopiaColorScheme = lightColorScheme(
    primary = TritanopiaPrimary,
    onPrimary = TritanopiaOnPrimary,
    primaryContainer = TritanopiaPrimaryVariant,
    onPrimaryContainer = TritanopiaOnPrimary,
    secondary = TritanopiaSecondary,
    onSecondary = TritanopiaOnSecondary,
    secondaryContainer = TritanopiaSecondaryVariant,
    onSecondaryContainer = TritanopiaOnSecondary,
    tertiary = TritanopiaSecondary,
    background = TritanopiaBackground,
    onBackground = OnBackground,
    surface = TritanopiaSurface,
    onSurface = OnSurface,
    surfaceVariant = TritanopiaSurface.copy(alpha = 0.9f),
    onSurfaceVariant = OnSurface.copy(alpha = 0.7f),
    error = Error,
    onError = OnError
)

@Composable
fun OpenDelhiTransitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to ensure our custom colors are used
    themePreferences: ThemePreferences? = null,
    content: @Composable () -> Unit
) {
    // Get the current theme preference if ThemePreferences is provided
    val themePreference = themePreferences?.themeFlow?.collectAsState(initial = AppTheme.SYSTEM)
    
    // Determine if dark mode should be used based on theme preference
    val useDarkTheme = when {
        // If theme preference is available, respect it
        themePreference != null -> {
            when (themePreference.value) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> darkTheme
                else -> darkTheme // For other themes, use system setting for dark/light
            }
        }
        // Otherwise fall back to system setting
        else -> darkTheme
    }
    
    // Select color scheme based on theme preference and dark/light mode
    val colorScheme = when {
        // Dynamic colors take precedence if enabled (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        
        // Apply specific theme based on preference
        themePreference != null -> {
            when (themePreference.value) {
                AppTheme.SYSTEM -> if (useDarkTheme) DarkColorScheme else LightColorScheme
                AppTheme.DARK -> DarkColorScheme
                AppTheme.LIGHT -> LightColorScheme
                AppTheme.BLUE -> if (useDarkTheme) BlueDarkColorScheme else BlueLightColorScheme
                AppTheme.GREEN -> if (useDarkTheme) GreenDarkColorScheme else GreenLightColorScheme
                AppTheme.HIGH_CONTRAST -> if (useDarkTheme) HighContrastDarkColorScheme else HighContrastLightColorScheme
                AppTheme.COLOR_BLIND_DEUTERANOPIA -> DeuteranopiaColorScheme
                AppTheme.COLOR_BLIND_PROTANOPIA -> ProtanopiaColorScheme
                AppTheme.COLOR_BLIND_TRITANOPIA -> TritanopiaColorScheme
            }
        }
        
        // Fall back to default themes
        else -> {
            if (useDarkTheme) DarkColorScheme else LightColorScheme
        }
    }
    
    // Apply status bar color
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    // Apply the MaterialTheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}