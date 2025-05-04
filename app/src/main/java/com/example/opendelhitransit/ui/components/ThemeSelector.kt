package com.example.opendelhitransit.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.opendelhitransit.data.AppTheme
import com.example.opendelhitransit.viewmodel.ThemeViewModel

@Composable
fun ThemeSettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = "Theme Settings",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ThemeSelectorDialog(
    viewModel: ThemeViewModel,
    onDismiss: () -> Unit
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Theme Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Choose a theme for the app",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ThemeOptionsList(viewModel = viewModel, currentTheme = currentTheme)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Close button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDismiss() },
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "Close",
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOptionsList(
    viewModel: ThemeViewModel,
    currentTheme: AppTheme
) {
    LazyColumn {
        item {
            ThemeCategory(title = "System")
        }
        items(listOf(AppTheme.SYSTEM, AppTheme.LIGHT, AppTheme.DARK)) { theme ->
            ThemeOption(
                theme = theme,
                isSelected = theme == currentTheme,
                viewModel = viewModel
            )
        }
        
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ThemeCategory(title = "Colors")
        }
        items(listOf(AppTheme.BLUE, AppTheme.GREEN)) { theme ->
            ThemeOption(
                theme = theme,
                isSelected = theme == currentTheme,
                viewModel = viewModel
            )
        }
        
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ThemeCategory(title = "Accessibility")
        }
        items(
            listOf(
                AppTheme.HIGH_CONTRAST,
                AppTheme.COLOR_BLIND_DEUTERANOPIA,
                AppTheme.COLOR_BLIND_PROTANOPIA,
                AppTheme.COLOR_BLIND_TRITANOPIA
            )
        ) { theme ->
            ThemeOption(
                theme = theme,
                isSelected = theme == currentTheme,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ThemeCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ThemeOption(
    theme: AppTheme,
    isSelected: Boolean,
    viewModel: ThemeViewModel
) {
    val icon = getThemeIcon(theme)
    val themeColor = getThemePreviewColor(theme)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.setTheme(theme) }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { viewModel.setTheme(theme) }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(themeColor)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = getContrastColor(themeColor),
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = viewModel.getThemeName(theme),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = viewModel.getThemeDescription(theme),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun getThemeIcon(theme: AppTheme): ImageVector {
    return when (theme) {
        AppTheme.SYSTEM -> Icons.Default.Settings
        AppTheme.LIGHT -> Icons.Default.LightMode
        AppTheme.DARK -> Icons.Default.DarkMode
        AppTheme.BLUE, AppTheme.GREEN -> Icons.Default.ColorLens
        AppTheme.HIGH_CONTRAST -> Icons.Default.Contrast
        AppTheme.COLOR_BLIND_DEUTERANOPIA, 
        AppTheme.COLOR_BLIND_PROTANOPIA, 
        AppTheme.COLOR_BLIND_TRITANOPIA -> Icons.Default.Visibility
    }
}

@Composable
private fun getThemePreviewColor(theme: AppTheme): Color {
    return when (theme) {
        AppTheme.SYSTEM -> MaterialTheme.colorScheme.surface
        AppTheme.LIGHT -> Color(0xFFF5F8FF)
        AppTheme.DARK -> Color(0xFF0A1128)
        AppTheme.BLUE -> Color(0xFF03A9F4)
        AppTheme.GREEN -> Color(0xFF2E7D32)
        AppTheme.HIGH_CONTRAST -> Color(0xFF000000)
        AppTheme.COLOR_BLIND_DEUTERANOPIA -> Color(0xFF0072B2)
        AppTheme.COLOR_BLIND_PROTANOPIA -> Color(0xFF0072B2)
        AppTheme.COLOR_BLIND_TRITANOPIA -> Color(0xFFD55E00)
    }
}

// Calculate a contrasting color for text
private fun getContrastColor(backgroundColor: Color): Color {
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return if (luminance > 0.5f) Color.Black else Color.White
} 