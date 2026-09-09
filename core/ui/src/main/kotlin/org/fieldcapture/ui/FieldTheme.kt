/*
 * :core:ui — design system for outdoor, gloved use.
 *
 * Responsibility: one Material 3 theme with a high-contrast palette (light background, near-black
 * text, saturated status colours), larger type scale, and controls whose minimum touch target is
 * 72 dp (the Android accessibility floor is 48 dp; gloves and a bouncing tractor cab need more).
 * Feature modules compose only these primitives so the whole app inherits the constraints.
 *
 * Interface: FieldTheme { }, FieldButton(...), StatusBanner(...). Stateless composables only.
 */
package org.fieldcapture.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Minimum touch target for primary field actions. */
val FieldTouchTarget = 72.dp

private val HighContrast = lightColorScheme(
    primary = Color(0xFF0B3D91),
    onPrimary = Color.White,
    secondary = Color(0xFF1B5E20),
    onSecondary = Color.White,
    error = Color(0xFFB00020),
    onError = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF111111),
    surface = Color.White,
    onSurface = Color(0xFF111111),
)

private val FieldTypography = Typography(
    displayLarge = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 20.sp),
    labelLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
)

@Composable
fun FieldTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = HighContrast, typography = FieldTypography, content = content)
}

/** Full-width primary action with a 72 dp minimum height. Label is short and uppercase by convention. */
@Composable
fun FieldButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = FieldTouchTarget),
        colors = ButtonDefaults.buttonColors(),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}

/** Persistent banner for degraded states: GPS lost, STT offline, storage low, recording paused. */
@Composable
fun StatusBanner(message: String, isError: Boolean, modifier: Modifier = Modifier) {
    val bg = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    val fg = if (isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondary
    Box(modifier = modifier.fillMaxWidth().background(bg).padding(16.dp)) {
        Text(message, color = fg, style = MaterialTheme.typography.bodyLarge)
    }
}
