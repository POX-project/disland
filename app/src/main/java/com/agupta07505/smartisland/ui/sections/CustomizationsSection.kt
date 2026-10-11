/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui.sections

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AvTimer
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agupta07505.smartisland.R
import com.agupta07505.smartisland.data.SmartIslandSettings
import com.agupta07505.smartisland.data.SmartIslandSettingsRepository
import kotlinx.coroutines.launch

import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableFloatStateOf
import com.agupta07505.smartisland.ui.SliderSettingItem
import kotlin.math.abs

private val PRESET_COLORS = listOf(
    0xFF10B981L to "Emerald",
    0xFF38BDF8L to "Sky",
    0xFF6366F1L to "Indigo",
    0xFFFF6B9AL to "Pink",
    0xFFEF4444L to "Coral",
    0xFFF59E0BL to "Amber",
    0xFF8B5CF6L to "Purple"
)

@Composable
fun CustomizationsSection(
    settings: SmartIslandSettings,
    repository: SmartIslandSettingsRepository
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var localOpacity by remember(settings.opacity) { mutableFloatStateOf(settings.opacity) }
    var localShadowElevation by remember(settings.shadowElevation) { mutableFloatStateOf(settings.shadowElevation) }
    var localShadowAlpha by remember(settings.shadowAlpha) { mutableFloatStateOf(settings.shadowAlpha) }
    var localBorderWidth by remember(settings.borderWidth) { mutableFloatStateOf(settings.borderWidth) }
    var localBorderAlpha by remember(settings.borderAlpha) { mutableFloatStateOf(settings.borderAlpha) }
    var localInnerShadowRadius by remember(settings.innerShadowRadius) { mutableFloatStateOf(settings.innerShadowRadius) }
    var localInnerShadowAlpha by remember(settings.innerShadowAlpha) { mutableFloatStateOf(settings.innerShadowAlpha) }
    var localBackgroundBlur by remember(settings.backgroundBlur) { mutableFloatStateOf(settings.backgroundBlur) }

    var showDialog by remember { mutableStateOf(false) }
    var currentColorTarget by remember { mutableStateOf("") }
    var colorPickerTitle by remember { mutableStateOf("") }
    var initialColor by remember { mutableStateOf(0xFF10B981L) }

    if (showDialog) {
        RgbColorPickerDialog(
            title = colorPickerTitle,
            initialColor = initialColor,
            onDismiss = { showDialog = false },
            onSave = { color ->
                showDialog = false
                scope.launch {
                    when (currentColorTarget) {
                        "battery" -> repository.setBatteryColor(color)
                        "notification" -> repository.setNotificationDotColor(color)
                        "music" -> repository.setMusicVisualizerColor(color)
                        "hotspot" -> repository.setHotspotColor(color)
                        "call" -> repository.setCallColor(color)
                        "live_activity" -> repository.setLiveActivityColor(color)
                        "transfer" -> repository.setTransferColor(color)
                        "navigation" -> repository.setNavigationColor(color)
                        "bluetooth" -> repository.setBluetoothColor(color)
                        "flashlight" -> repository.setFlashlightColor(color)
                        "screen_recording" -> repository.setScreenRecordingColor(color)
                        "timer" -> repository.setTimerColor(color)
                        "stopwatch" -> repository.setStopwatchColor(color)
                        "shadow_color" -> repository.setShadowColor(color)
                        "border_color" -> repository.setBorderColor(color)
                        "inner_shadow_color" -> repository.setInnerShadowColor(color)
                        "island_background_color" -> repository.setIslandBackgroundColor(color)
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 0: Visual Styling, Blur, Shadows & Specular Border
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.title_glassmorphism_shadows),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.desc_glassmorphism_shadows),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Theme Presets Row
                Text(
                    text = stringResource(R.string.theme_preset_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "OLED_BLACK" to stringResource(R.string.theme_oled_black),
                        "APPLE_GLASS" to stringResource(R.string.theme_apple_glass),
                        "FROSTED_ACRYLIC" to stringResource(R.string.theme_frosted_acrylic),
                        "CYBER_GLOW" to stringResource(R.string.theme_cyber_glow),
                        "NEUMORPHIC" to stringResource(R.string.theme_neumorphic)
                    ).forEach { (presetKey, label) ->
                        val isSelected = when (presetKey) {
                            "OLED_BLACK" -> localBorderWidth == 0f && localBackgroundBlur == 0f && localOpacity >= 0.98f && settings.islandBackgroundColor == 0xFF000000L
                            "APPLE_GLASS" -> abs(localBackgroundBlur - 14f) < 2f && abs(localOpacity - 0.88f) < 0.05f
                            "FROSTED_ACRYLIC" -> abs(localBackgroundBlur - 22f) < 2f && abs(localOpacity - 0.72f) < 0.05f
                            "CYBER_GLOW" -> abs(localBackgroundBlur - 8f) < 2f && settings.shadowColor == 0xFF06B6D4L
                            "NEUMORPHIC" -> localBackgroundBlur == 0f && abs(localInnerShadowRadius - 10f) < 2f && settings.innerShadowColor == 0xFF000000L
                            else -> false
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    scope.launch {
                                        repository.setVisualThemePreset(presetKey)
                                        when (presetKey) {
                                            "OLED_BLACK" -> {
                                                localOpacity = 1f
                                                localShadowElevation = 18f
                                                localShadowAlpha = 0.9f
                                                localBorderWidth = 0f
                                                localBorderAlpha = 0.20f
                                                localInnerShadowRadius = 0f
                                                localInnerShadowAlpha = 0f
                                                localBackgroundBlur = 0f
                                            }
                                            "APPLE_GLASS" -> {
                                                localOpacity = 0.88f
                                                localShadowElevation = 16f
                                                localShadowAlpha = 0.75f
                                                localBorderWidth = 0.8f
                                                localBorderAlpha = 0.22f
                                                localInnerShadowRadius = 4f
                                                localInnerShadowAlpha = 0.15f
                                                localBackgroundBlur = 14f
                                            }
                                            "FROSTED_ACRYLIC" -> {
                                                localOpacity = 0.72f
                                                localShadowElevation = 10f
                                                localShadowAlpha = 0.6f
                                                localBorderWidth = 1.2f
                                                localBorderAlpha = 0.35f
                                                localInnerShadowRadius = 8f
                                                localInnerShadowAlpha = 0.25f
                                                localBackgroundBlur = 22f
                                            }
                                            "CYBER_GLOW" -> {
                                                localOpacity = 0.92f
                                                localShadowElevation = 24f
                                                localShadowAlpha = 0.85f
                                                localBorderWidth = 1.4f
                                                localBorderAlpha = 0.65f
                                                localInnerShadowRadius = 6f
                                                localInnerShadowAlpha = 0.30f
                                                localBackgroundBlur = 8f
                                            }
                                            "NEUMORPHIC" -> {
                                                localOpacity = 0.95f
                                                localShadowElevation = 18f
                                                localShadowAlpha = 0.7f
                                                localBorderWidth = 1.0f
                                                localBorderAlpha = 0.15f
                                                localInnerShadowRadius = 10f
                                                localInnerShadowAlpha = 0.35f
                                                localBackgroundBlur = 0f
                                            }
                                        }
                                        Toast.makeText(context, context.getString(R.string.toast_theme_applied, label), Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(vertical = 8.dp, horizontal = 2.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Section A: Outer Floating Shadows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.toggle_drop_shadow_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.slider_shadow_elevation_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.enableShadow,
                        onCheckedChange = { checked ->
                            scope.launch { repository.setEnableShadow(checked) }
                        }
                    )
                }

                if (settings.enableShadow) {
                    SliderSettingItem(
                        label = stringResource(R.string.slider_shadow_elevation),
                        value = localShadowElevation,
                        range = SmartIslandSettings.MIN_SHADOW_ELEVATION..SmartIslandSettings.MAX_SHADOW_ELEVATION,
                        suffix = "dp",
                        step = 1f,
                        onValueChange = { localShadowElevation = it },
                        onValueChangeFinished = { scope.launch { repository.setShadowElevation(localShadowElevation) } }
                    )

                    SliderSettingItem(
                        label = stringResource(R.string.slider_shadow_alpha),
                        value = (localShadowAlpha * 100f),
                        range = (SmartIslandSettings.MIN_SHADOW_ALPHA * 100f)..(SmartIslandSettings.MAX_SHADOW_ALPHA * 100f),
                        suffix = "%",
                        step = 5f,
                        onValueChange = { localShadowAlpha = (it / 100f).coerceIn(SmartIslandSettings.MIN_SHADOW_ALPHA, SmartIslandSettings.MAX_SHADOW_ALPHA) },
                        onValueChangeFinished = { scope.launch { repository.setShadowAlpha(localShadowAlpha) } }
                    )

                    FeatureColorRow(
                        title = stringResource(R.string.color_shadow_tint),
                        subtitle = stringResource(R.string.slider_shadow_alpha_desc),
                        icon = Icons.Rounded.CenterFocusStrong,
                        selectedColor = settings.shadowColor,
                        onColorSelected = { scope.launch { repository.setShadowColor(it) } },
                        onCustomClicked = {
                            initialColor = settings.shadowColor
                            currentColorTarget = "shadow_color"
                            colorPickerTitle = ""
                            showDialog = true
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Section B: Specular Hairline Rim / Inner Border
                SliderSettingItem(
                    label = stringResource(R.string.slider_border_width),
                    value = localBorderWidth,
                    range = SmartIslandSettings.MIN_BORDER_WIDTH..SmartIslandSettings.MAX_BORDER_WIDTH,
                    suffix = "dp",
                    step = 0.2f,
                    onValueChange = { localBorderWidth = it },
                    onValueChangeFinished = { scope.launch { repository.setBorderWidth(localBorderWidth) } }
                )

                if (localBorderWidth > 0f) {
                    SliderSettingItem(
                        label = stringResource(R.string.slider_border_alpha),
                        value = (localBorderAlpha * 100f),
                        range = (SmartIslandSettings.MIN_BORDER_ALPHA * 100f)..(SmartIslandSettings.MAX_BORDER_ALPHA * 100f),
                        suffix = "%",
                        step = 5f,
                        onValueChange = { localBorderAlpha = (it / 100f).coerceIn(SmartIslandSettings.MIN_BORDER_ALPHA, SmartIslandSettings.MAX_BORDER_ALPHA) },
                        onValueChangeFinished = { scope.launch { repository.setBorderAlpha(localBorderAlpha) } }
                    )

                    FeatureColorRow(
                        title = stringResource(R.string.color_border_tint),
                        subtitle = stringResource(R.string.slider_border_alpha_desc),
                        icon = Icons.Rounded.AutoAwesome,
                        selectedColor = settings.borderColor,
                        onColorSelected = { scope.launch { repository.setBorderColor(it) } },
                        onCustomClicked = {
                            initialColor = settings.borderColor
                            currentColorTarget = "border_color"
                            colorPickerTitle = ""
                            showDialog = true
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Section C: Inset Inner Shadow / Inner Glow
                SliderSettingItem(
                    label = stringResource(R.string.slider_inner_shadow_radius),
                    value = localInnerShadowRadius,
                    range = SmartIslandSettings.MIN_INNER_SHADOW_RADIUS..SmartIslandSettings.MAX_INNER_SHADOW_RADIUS,
                    suffix = "dp",
                    step = 1f,
                    onValueChange = { localInnerShadowRadius = it },
                    onValueChangeFinished = { scope.launch { repository.setInnerShadowRadius(localInnerShadowRadius) } }
                )

                if (localInnerShadowRadius > 0f) {
                    SliderSettingItem(
                        label = stringResource(R.string.slider_inner_shadow_alpha),
                        value = (localInnerShadowAlpha * 100f),
                        range = 5f..100f,
                        suffix = "%",
                        step = 5f,
                        onValueChange = { localInnerShadowAlpha = (it / 100f).coerceIn(0.05f, 1f) },
                        onValueChangeFinished = { scope.launch { repository.setInnerShadowAlpha(localInnerShadowAlpha) } }
                    )

                    FeatureColorRow(
                        title = stringResource(R.string.color_inner_shadow_tint),
                        subtitle = stringResource(R.string.slider_inner_shadow_radius_desc),
                        icon = Icons.Rounded.Tune,
                        selectedColor = settings.innerShadowColor,
                        onColorSelected = { scope.launch { repository.setInnerShadowColor(it) } },
                        onCustomClicked = {
                            initialColor = settings.innerShadowColor
                            currentColorTarget = "inner_shadow_color"
                            colorPickerTitle = ""
                            showDialog = true
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Section D: Frosted Glass & Blur
                SliderSettingItem(
                    label = stringResource(R.string.slider_background_blur),
                    value = localBackgroundBlur,
                    range = SmartIslandSettings.MIN_BACKGROUND_BLUR..SmartIslandSettings.MAX_BACKGROUND_BLUR,
                    suffix = "dp",
                    step = 1f,
                    onValueChange = { localBackgroundBlur = it },
                    onValueChangeFinished = { scope.launch { repository.setBackgroundBlur(localBackgroundBlur) } }
                )

                FeatureColorRow(
                    title = stringResource(R.string.color_island_background),
                    subtitle = stringResource(R.string.opacity_card_desc),
                    icon = Icons.Rounded.Notifications,
                    selectedColor = settings.islandBackgroundColor,
                    onColorSelected = { scope.launch { repository.setIslandBackgroundColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.islandBackgroundColor
                        currentColorTarget = "island_background_color"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            repository.resetVisualStyles()
                            localShadowElevation = SmartIslandSettings.Default.shadowElevation
                            localShadowAlpha = SmartIslandSettings.Default.shadowAlpha
                            localBorderWidth = SmartIslandSettings.Default.borderWidth
                            localBorderAlpha = SmartIslandSettings.Default.borderAlpha
                            localInnerShadowRadius = SmartIslandSettings.Default.innerShadowRadius
                            localInnerShadowAlpha = SmartIslandSettings.Default.innerShadowAlpha
                            localBackgroundBlur = SmartIslandSettings.Default.backgroundBlur
                            localOpacity = SmartIslandSettings.Default.opacity
                        }
                        Toast.makeText(context, context.getString(R.string.toast_reset_visual_styles), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_reset_visual_styles), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Card 1: Island Opacity & Transparency
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.opacity_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.opacity_card_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(localOpacity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Preset Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        1.0f to stringResource(R.string.opacity_solid),
                        0.85f to stringResource(R.string.opacity_dark),
                        0.70f to stringResource(R.string.opacity_glass),
                        0.50f to stringResource(R.string.opacity_clear)
                    ).forEach { (targetVal, label) ->
                        val isSelected = abs(localOpacity - targetVal) < 0.04f
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    localOpacity = targetVal
                                    scope.launch { repository.setOpacity(targetVal) }
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Slider
                SliderSettingItem(
                    label = stringResource(R.string.slider_precision_opacity),
                    value = (localOpacity * 100f),
                    range = (SmartIslandSettings.MIN_OPACITY * 100f)..(SmartIslandSettings.MAX_OPACITY * 100f),
                    suffix = "%",
                    step = 5f,
                    onValueChange = { localOpacity = (it / 100f).coerceIn(SmartIslandSettings.MIN_OPACITY, SmartIslandSettings.MAX_OPACITY) },
                    onValueChangeFinished = { scope.launch { repository.setOpacity(localOpacity) } }
                )
            }
        }
        // Card 1: Music Player Experience
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.media_player_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.media_player_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.toggle_artwork_backdrop_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.toggle_artwork_backdrop_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = settings.enableMusicArtworkBackground,
                        onCheckedChange = { checked ->
                            scope.launch { repository.setEnableMusicArtworkBackground(checked) }
                        }
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Option: Show Lyrics Below Song Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Show Lyrics Below Controls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Extend expanded Dynamic Island to display real-time lyrics under music player controls",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = settings.showLyricsUnderControls,
                        onCheckedChange = { checked ->
                            scope.launch { repository.setShowLyricsUnderControls(checked) }
                        }
                    )
                }
            }
        }

        // Card 2: Feature Accent Colors Studio
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.color_studio_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.color_studio_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Feature Color Rows
                FeatureColorRow(
                    title = stringResource(R.string.color_battery_charging),
                    subtitle = stringResource(R.string.color_battery_charging_desc),
                    icon = Icons.Rounded.BatteryChargingFull,
                    selectedColor = settings.batteryColor,
                    onColorSelected = { scope.launch { repository.setBatteryColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.batteryColor
                        currentColorTarget = "battery"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_notification_dot),
                    subtitle = stringResource(R.string.color_notification_dot_desc),
                    icon = Icons.Rounded.Notifications,
                    selectedColor = settings.notificationDotColor,
                    onColorSelected = { scope.launch { repository.setNotificationDotColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.notificationDotColor
                        currentColorTarget = "notification"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_music_visualizer),
                    subtitle = stringResource(R.string.color_music_visualizer_desc),
                    icon = Icons.Rounded.MusicNote,
                    selectedColor = settings.musicVisualizerColor,
                    onColorSelected = { scope.launch { repository.setMusicVisualizerColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.musicVisualizerColor
                        currentColorTarget = "music"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_phone_calls),
                    subtitle = stringResource(R.string.color_phone_calls_desc),
                    icon = Icons.Rounded.Call,
                    selectedColor = settings.callColor,
                    onColorSelected = { scope.launch { repository.setCallColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.callColor
                        currentColorTarget = "call"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_live_activities),
                    subtitle = stringResource(R.string.color_live_activities_desc),
                    icon = Icons.Rounded.Navigation,
                    selectedColor = settings.liveActivityColor,
                    onColorSelected = { scope.launch { repository.setLiveActivityColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.liveActivityColor
                        currentColorTarget = "live_activity"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_file_downloads),
                    subtitle = stringResource(R.string.color_file_downloads_desc),
                    icon = Icons.Rounded.FileDownload,
                    selectedColor = settings.transferColor,
                    onColorSelected = { scope.launch { repository.setTransferColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.transferColor
                        currentColorTarget = "transfer"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_maps_navigation),
                    subtitle = stringResource(R.string.color_maps_navigation_desc),
                    icon = Icons.Rounded.Explore,
                    selectedColor = settings.navigationColor,
                    onColorSelected = { scope.launch { repository.setNavigationColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.navigationColor
                        currentColorTarget = "navigation"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_hotspot_tethering),
                    subtitle = stringResource(R.string.color_hotspot_tethering_desc),
                    icon = Icons.Rounded.WifiTethering,
                    selectedColor = settings.hotspotColor,
                    onColorSelected = { scope.launch { repository.setHotspotColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.hotspotColor
                        currentColorTarget = "hotspot"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_bluetooth_device),
                    subtitle = stringResource(R.string.color_bluetooth_device_desc),
                    icon = Icons.Rounded.BluetoothConnected,
                    selectedColor = settings.bluetoothColor,
                    onColorSelected = { scope.launch { repository.setBluetoothColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.bluetoothColor
                        currentColorTarget = "bluetooth"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_flashlight_torch),
                    subtitle = stringResource(R.string.color_flashlight_torch_desc),
                    icon = Icons.Rounded.FlashlightOn,
                    selectedColor = settings.flashlightColor,
                    onColorSelected = { scope.launch { repository.setFlashlightColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.flashlightColor
                        currentColorTarget = "flashlight"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_screen_recording),
                    subtitle = stringResource(R.string.color_screen_recording_desc),
                    icon = Icons.Rounded.Videocam,
                    selectedColor = settings.screenRecordingColor,
                    onColorSelected = { scope.launch { repository.setScreenRecordingColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.screenRecordingColor
                        currentColorTarget = "screen_recording"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_timer_countdown),
                    subtitle = stringResource(R.string.color_timer_countdown_desc),
                    icon = Icons.Rounded.HourglassBottom,
                    selectedColor = settings.timerColor,
                    onColorSelected = { scope.launch { repository.setTimerColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.timerColor
                        currentColorTarget = "timer"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                FeatureColorRow(
                    title = stringResource(R.string.color_stopwatch_laps),
                    subtitle = stringResource(R.string.color_stopwatch_laps_desc),
                    icon = Icons.Rounded.AvTimer,
                    selectedColor = settings.stopwatchColor,
                    onColorSelected = { scope.launch { repository.setStopwatchColor(it) } },
                    onCustomClicked = {
                        initialColor = settings.stopwatchColor
                        currentColorTarget = "stopwatch"
                        colorPickerTitle = ""
                        showDialog = true
                    }
                )

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            repository.setBatteryColor(0xFF10B981L)
                            repository.setNotificationDotColor(0xFF38BDF8L)
                            repository.setMusicVisualizerColor(0xFFFF6B9AL)
                            repository.setHotspotColor(0xFFF59E0BL)
                            repository.setCallColor(0xFF22C55EL)
                            repository.setLiveActivityColor(0xFF8B5CF6L)
                            repository.setTransferColor(0xFF06B6D4L)
                            repository.setNavigationColor(0xFF10B981L)
                            repository.setBluetoothColor(0xFF38BDF8L)
                            repository.setFlashlightColor(0xFFF59E0BL)
                            repository.setScreenRecordingColor(0xFFEF4444L)
                            repository.setTimerColor(0xFFF59E0BL)
                            repository.setStopwatchColor(0xFF06B6D4L)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_reset_colors), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FeatureColorRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    onCustomClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(selectedColor).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(selectedColor),
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Swatches row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PRESET_COLORS.forEach { (colorValue, _) ->
                val isSelected = selectedColor == colorValue
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .then(
                            if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            else Modifier
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(colorValue))
                        .clickable { onColorSelected(colorValue) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Custom RGB button
            val isCustom = PRESET_COLORS.none { it.first == selectedColor }
            val rainbowBrush = remember {
                Brush.linearGradient(
                    listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta)
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (isCustom) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        else Modifier
                    )
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(rainbowBrush)
                    .clickable(onClick = onCustomClicked),
                contentAlignment = Alignment.Center
            ) {
                if (isCustom) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Custom",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RgbColorPickerDialog(
    title: String,
    initialColor: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    val initialColorObj = Color(initialColor)
    var red by remember { mutableStateOf((initialColorObj.red * 255f).toInt()) }
    var green by remember { mutableStateOf((initialColorObj.green * 255f).toInt()) }
    var blue by remember { mutableStateOf((initialColorObj.blue * 255f).toInt()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title.ifEmpty { stringResource(R.string.dialog_color_picker_title) },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val previewColor = Color(red, green, blue)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(previewColor)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val hexCode = String.format("#%02X%02X%02X", red, green, blue)
                    Text(
                        text = hexCode,
                        color = if (red * 0.299 + green * 0.587 + blue * 0.114 > 186) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Red Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.color_red), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("$red", style = MaterialTheme.typography.bodyMedium)
                    }
                    Slider(
                        value = red.toFloat(),
                        onValueChange = { red = it.toInt() },
                        valueRange = 0f..255f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Green Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.color_green), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("$green", style = MaterialTheme.typography.bodyMedium)
                    }
                    Slider(
                        value = green.toFloat(),
                        onValueChange = { green = it.toInt() },
                        valueRange = 0f..255f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Blue Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.color_blue), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("$blue", style = MaterialTheme.typography.bodyMedium)
                    }
                    Slider(
                        value = blue.toFloat(),
                        onValueChange = { blue = it.toInt() },
                        valueRange = 0f..255f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalColor = (0xFF000000L or (red.toLong() shl 16) or (green.toLong() shl 8) or blue.toLong())
                    onSave(finalColor)
                }
            ) {
                Text(stringResource(R.string.btn_save_color), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
