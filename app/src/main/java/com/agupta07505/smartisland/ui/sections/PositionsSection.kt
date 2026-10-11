/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui.sections

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.automirrored.rounded.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.rounded.AlignHorizontalRight
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.agupta07505.smartisland.R
import com.agupta07505.smartisland.data.SmartIslandSettings
import com.agupta07505.smartisland.data.SmartIslandSettingsRepository
import com.agupta07505.smartisland.ui.SliderSettingItem
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PositionsSection(
    settings: SmartIslandSettings,
    repository: SmartIslandSettingsRepository,
    onNavigateToProfiles: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    val hasSavedTemplate by repository.hasSavedTemplate.collectAsState(initial = false)
    val activeProfile by repository.activeProfile.collectAsState(initial = null)

    // Ensure the user's current settings are captured as a template on startup if none exists yet!
    LaunchedEffect(Unit) {
        if (!hasSavedTemplate) {
            repository.saveCurrentAsTemplate()
        }
    }

    var localWidth by remember(settings.width) { mutableFloatStateOf(settings.width) }
    var localHeight by remember(settings.height) { mutableFloatStateOf(settings.height) }
    var localXOffset by remember(settings.xOffset) { mutableFloatStateOf(settings.xOffset) }
    var localYOffset by remember(settings.yOffset) { mutableFloatStateOf(settings.yOffset) }
    var localCornerRadius by remember(settings.cornerRadius) { mutableFloatStateOf(settings.cornerRadius) }
    var localOpacity by remember(settings.opacity) { mutableFloatStateOf(settings.opacity) }
    var localHorizontalPadding by remember(settings.contentHorizontalPadding) { mutableFloatStateOf(settings.contentHorizontalPadding) }
    var localVerticalPadding by remember(settings.contentVerticalPadding) { mutableFloatStateOf(settings.contentVerticalPadding) }
    var localSpringStiffness by remember(settings.animationSpringStiffness) { mutableFloatStateOf(settings.animationSpringStiffness) }
    var localSpringDamping by remember(settings.animationSpringDamping) { mutableFloatStateOf(settings.animationSpringDamping) }
    var localAnimationDelay by remember(settings.animationDelayMs) { mutableFloatStateOf(settings.animationDelayMs.toFloat()) }
    var localCameraClearance by remember(settings.pinchCenterClearance) { mutableFloatStateOf(settings.pinchCenterClearance) }
    var localExpandedScreenMargin by remember(settings.expandedScreenMargin) { mutableFloatStateOf(settings.expandedScreenMargin) }
    var localExpandedTopMargin by remember(settings.expandedTopMargin) { mutableFloatStateOf(settings.expandedTopMargin) }
    var localExpandedContentHorizontalPadding by remember(settings.expandedContentHorizontalPadding) { mutableFloatStateOf(settings.expandedContentHorizontalPadding) }
    var localExpandedContentVerticalPadding by remember(settings.expandedContentVerticalPadding) { mutableFloatStateOf(settings.expandedContentVerticalPadding) }
    var localSplitIslandEnabled by remember(settings.splitIslandEnabled) { mutableStateOf(settings.splitIslandEnabled) }

    // Dynamically calculate responsive notch coordinates for the current device screen
    val screenWidthDp = with(density) { windowInfo.containerSize.width.toDp().value }
    val calculatedLeftX = (-(screenWidthDp / 2f - 40f)).coerceIn(
        SmartIslandSettings.MIN_X_OFFSET,
        -30f
    )
    val calculatedRightX = ((screenWidthDp / 2f - 40f)).coerceIn(
        30f,
        SmartIslandSettings.MAX_X_OFFSET
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Quick Responsive Layout Presets
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.presets_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.presets_desc, screenWidthDp.toInt()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Presets 2x2 Responsive Grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: Center Hole & Wide Island
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isCenterSelected = !settings.pinchNotchEnabled && abs(settings.xOffset) < 5f && abs(settings.width - 112f) < 8f
                        PresetCardItem(
                            title = stringResource(R.string.preset_center_hole),
                            subtitle = stringResource(R.string.preset_center_hole_desc),
                            icon = Icons.Rounded.CenterFocusStrong,
                            isSelected = isCenterSelected,
                            onClick = {
                                scope.launch {
                                    repository.setPinchNotchEnabled(false)
                                    repository.setPosition(
                                        width = 112f,
                                        height = 34f,
                                        xOffset = 0f,
                                        yOffset = 10f
                                    )
                                    repository.setCornerRadius(20f)
                                }
                                Toast.makeText(context, context.getString(R.string.toast_applied_center_preset), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        val isWideSelected = !settings.pinchNotchEnabled && abs(settings.xOffset) < 5f && abs(settings.width - 150f) < 8f
                        PresetCardItem(
                            title = stringResource(R.string.preset_wide_island),
                            subtitle = stringResource(R.string.preset_wide_island_desc),
                            icon = Icons.Rounded.FitScreen,
                            isSelected = isWideSelected,
                            onClick = {
                                scope.launch {
                                    repository.setPinchNotchEnabled(false)
                                    repository.setPosition(
                                        width = 150f,
                                        height = 38f,
                                        xOffset = 0f,
                                        yOffset = 12f
                                    )
                                    repository.setCornerRadius(22f)
                                }
                                Toast.makeText(context, context.getString(R.string.toast_applied_wide_preset), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Left Corner & Right Corner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isLeftSelected = !settings.pinchNotchEnabled && settings.xOffset < -30f
                        PresetCardItem(
                            title = stringResource(R.string.preset_left_corner),
                            subtitle = stringResource(R.string.preset_left_corner_desc),
                            icon = Icons.AutoMirrored.Rounded.AlignHorizontalLeft,
                            isSelected = isLeftSelected,
                            onClick = {
                                scope.launch {
                                    repository.setPinchNotchEnabled(false)
                                    repository.setPosition(
                                        width = 105f,
                                        height = 34f,
                                        xOffset = calculatedLeftX,
                                        yOffset = 10f
                                    )
                                    repository.setCornerRadius(20f)
                                }
                                Toast.makeText(context, context.getString(R.string.toast_applied_left_preset, calculatedLeftX.toInt()), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        val isRightSelected = !settings.pinchNotchEnabled && settings.xOffset > 30f
                        PresetCardItem(
                            title = stringResource(R.string.preset_right_corner),
                            subtitle = stringResource(R.string.preset_right_corner_desc),
                            icon = Icons.AutoMirrored.Rounded.AlignHorizontalRight,
                            isSelected = isRightSelected,
                            onClick = {
                                scope.launch {
                                    repository.setPinchNotchEnabled(false)
                                    repository.setPosition(
                                        width = 105f,
                                        height = 34f,
                                        xOffset = calculatedRightX,
                                        yOffset = 10f
                                    )
                                    repository.setCornerRadius(20f)
                                }
                                Toast.makeText(context, context.getString(R.string.toast_applied_right_preset, calculatedRightX.toInt()), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 3: Compact Pill & Pinch Notch (Redmi Note 8 Pro)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isCompactSelected = !settings.pinchNotchEnabled && abs(settings.xOffset) < 5f && abs(settings.width - 92f) < 6f
                        PresetCardItem(
                            title = stringResource(R.string.preset_compact_pill),
                            subtitle = stringResource(R.string.preset_compact_pill_desc),
                            icon = Icons.Rounded.Smartphone,
                            isSelected = isCompactSelected,
                            onClick = {
                                scope.launch {
                                    repository.setPinchNotchEnabled(false)
                                    repository.setPosition(
                                        width = 92f,
                                        height = 30f,
                                        xOffset = 0f,
                                        yOffset = 8f
                                    )
                                    repository.setCornerRadius(18f)
                                }
                                Toast.makeText(context, context.getString(R.string.toast_applied_compact_preset), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        val isPinchSelected = settings.pinchNotchEnabled
                        PresetCardItem(
                            title = stringResource(R.string.preset_pinch_notch),
                            subtitle = stringResource(R.string.preset_pinch_notch_desc),
                            icon = Icons.Rounded.CenterFocusStrong,
                            isSelected = isPinchSelected,
                            onClick = {
                                scope.launch {
                                    repository.setPinchNotchEnabled(true)
                                    repository.setPosition(
                                        width = 175f,
                                        height = 34f,
                                        xOffset = 0f,
                                        yOffset = 0f
                                    )
                                    repository.setCornerRadius(18f)
                                    repository.setContentHorizontalPadding(14f)
                                    repository.setContentVerticalPadding(0f)
                                }
                                Toast.makeText(context, context.getString(R.string.toast_applied_pinch_preset), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 4: Custom Saved Template Preset Card
                    PresetCardItem(
                        title = stringResource(R.string.preset_custom_template),
                        subtitle = stringResource(R.string.preset_custom_template_desc),
                        icon = Icons.Rounded.Bookmark,
                        isSelected = false,
                        onClick = {
                            scope.launch {
                                if (hasSavedTemplate) {
                                    repository.applySavedTemplate()
                                    Toast.makeText(context, context.getString(R.string.toast_template_applied), Toast.LENGTH_SHORT).show()
                                } else {
                                    repository.saveCurrentAsTemplate()
                                    Toast.makeText(context, context.getString(R.string.toast_template_saved), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Custom Template Action Card (Save & Apply Controls)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (hasSavedTemplate) Icons.Rounded.BookmarkAdded else Icons.Rounded.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.template_section_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.template_section_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.saveCurrentAsTemplate()
                                        Toast.makeText(context, context.getString(R.string.toast_template_saved), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_save_template),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        if (hasSavedTemplate) {
                                            repository.applySavedTemplate()
                                            Toast.makeText(context, context.getString(R.string.toast_template_applied), Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, context.getString(R.string.toast_no_template_saved), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_apply_template),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        if (onNavigateToProfiles != null) {
                            OutlinedButton(
                                onClick = onNavigateToProfiles,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bookmarks,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Manage Island Profiles (${activeProfile?.name ?: "Presets"})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card 2: Precision Dimensions & Offsets
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.precision_tuning_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.precision_tuning_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                SliderSettingItem(
                    label = stringResource(R.string.slider_island_width),
                    value = localWidth,
                    range = SmartIslandSettings.MIN_WIDTH..SmartIslandSettings.MAX_WIDTH,
                    onValueChange = { localWidth = it },
                    onValueChangeFinished = { scope.launch { repository.setWidth(localWidth) } }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_island_height),
                    value = localHeight,
                    range = SmartIslandSettings.MIN_HEIGHT..SmartIslandSettings.MAX_HEIGHT,
                    onValueChange = { localHeight = it },
                    onValueChangeFinished = { scope.launch { repository.setHeight(localHeight) } }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_horizontal_offset),
                    value = localXOffset,
                    range = SmartIslandSettings.MIN_X_OFFSET..SmartIslandSettings.MAX_X_OFFSET,
                    onValueChange = { localXOffset = it },
                    onValueChangeFinished = { scope.launch { repository.setXOffset(localXOffset) } }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_vertical_offset),
                    value = localYOffset,
                    range = SmartIslandSettings.MIN_Y_OFFSET..SmartIslandSettings.MAX_Y_OFFSET,
                    onValueChange = { localYOffset = it },
                    onValueChangeFinished = { scope.launch { repository.setYOffset(localYOffset) } }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_corner_radius),
                    value = localCornerRadius,
                    range = SmartIslandSettings.MIN_CORNER_RADIUS..SmartIslandSettings.MAX_CORNER_RADIUS,
                    onValueChange = { localCornerRadius = it },
                    onValueChangeFinished = { scope.launch { repository.setCornerRadius(localCornerRadius) } }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SliderSettingItem(
                        label = stringResource(R.string.slider_island_opacity),
                        value = (localOpacity * 100f),
                        range = (SmartIslandSettings.MIN_OPACITY * 100f)..(SmartIslandSettings.MAX_OPACITY * 100f),
                        suffix = "%",
                        step = 5f,
                        onValueChange = { localOpacity = (it / 100f).coerceIn(SmartIslandSettings.MIN_OPACITY, SmartIslandSettings.MAX_OPACITY) },
                        onValueChangeFinished = { scope.launch { repository.setOpacity(localOpacity) } }
                    )

                    // Quick Opacity Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            1.0f to "100%",
                            0.85f to "85%",
                            0.70f to "70%",
                            0.50f to "50%"
                        ).forEach { (targetVal, label) ->
                            val isSelected = abs(localOpacity - targetVal) < 0.04f
                            Surface(
                                shape = RoundedCornerShape(8.dp),
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
                                        .padding(vertical = 6.dp)
                                        .fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.toggle_drop_shadow_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.toggle_drop_shadow_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = settings.enableShadow,
                        onCheckedChange = { checked ->
                            scope.launch { repository.setEnableShadow(checked) }
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.toggle_pinch_notch_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.toggle_pinch_notch_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = settings.pinchNotchEnabled,
                        onCheckedChange = { checked ->
                            scope.launch {
                                repository.setPinchNotchEnabled(checked)
                                if (checked && settings.yOffset > 4f) {
                                    repository.setYOffset(0f)
                                }
                            }
                        }
                    )
                }

                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = {
                        scope.launch { repository.resetPosition() }
                        Toast.makeText(context, context.getString(R.string.toast_reset_position), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_reset_position), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Card 3: Content Spacing & Paddings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.title_paddings_tuning),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.desc_paddings_tuning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                SliderSettingItem(
                    label = stringResource(R.string.slider_content_horizontal_padding),
                    value = localHorizontalPadding,
                    range = SmartIslandSettings.MIN_CONTENT_PADDING..SmartIslandSettings.MAX_CONTENT_PADDING,
                    suffix = "dp",
                    step = 1f,
                    onValueChange = { localHorizontalPadding = it },
                    onValueChangeFinished = {
                        scope.launch { repository.setContentHorizontalPadding(localHorizontalPadding) }
                    }
                )

                // Quick Padding Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        8f to "8dp (Tight)",
                        12f to "12dp (Default)",
                        16f to "16dp (Comfort)",
                        22f to "22dp (Wide)"
                    ).forEach { (padVal, label) ->
                        val isSelected = abs(localHorizontalPadding - padVal) < 1f
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    localHorizontalPadding = padVal
                                    scope.launch { repository.setContentHorizontalPadding(padVal) }
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_content_vertical_padding),
                    value = localVerticalPadding,
                    range = SmartIslandSettings.MIN_VERTICAL_PADDING..SmartIslandSettings.MAX_VERTICAL_PADDING,
                    suffix = "dp",
                    step = 1f,
                    onValueChange = { localVerticalPadding = it },
                    onValueChangeFinished = {
                        scope.launch { repository.setContentVerticalPadding(localVerticalPadding) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_camera_clearance),
                    value = localCameraClearance,
                    range = SmartIslandSettings.MIN_PINCH_CENTER_CLEARANCE..SmartIslandSettings.MAX_PINCH_CENTER_CLEARANCE,
                    suffix = "dp",
                    step = 1f,
                    onValueChange = { localCameraClearance = it },
                    onValueChangeFinished = {
                        scope.launch { repository.setPinchCenterClearance(localCameraClearance) }
                    }
                )
            }
        }

        // Card 4: Detailed (Expanded) Mode Spacing & Paddings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.title_expanded_mode_tuning),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.desc_expanded_mode_tuning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                SliderSettingItem(
                    label = stringResource(R.string.slider_expanded_screen_margin),
                    value = localExpandedScreenMargin,
                    range = SmartIslandSettings.MIN_EXPANDED_SCREEN_MARGIN..SmartIslandSettings.MAX_EXPANDED_SCREEN_MARGIN,
                    suffix = "dp",
                    step = 1f,
                    onValueChange = { localExpandedScreenMargin = it },
                    onValueChangeFinished = {
                        scope.launch { repository.setExpandedScreenMargin(localExpandedScreenMargin) }
                    }
                )

                // Quick Margin Chips (Edge-to-Edge, Compact, Floating)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        0f to "0dp (Edge)",
                        8f to "8dp (Snug)",
                        12f to "12dp (Default)",
                        20f to "20dp (Float)"
                    ).forEach { (marginVal, label) ->
                        val isSelected = abs(localExpandedScreenMargin - marginVal) < 1f
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    localExpandedScreenMargin = marginVal
                                    scope.launch { repository.setExpandedScreenMargin(marginVal) }
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_expanded_top_margin),
                    value = localExpandedTopMargin,
                    range = SmartIslandSettings.MIN_EXPANDED_TOP_MARGIN..SmartIslandSettings.MAX_EXPANDED_TOP_MARGIN,
                    suffix = "dp",
                    step = 1f,
                    onValueChange = { localExpandedTopMargin = it },
                    onValueChangeFinished = {
                        scope.launch { repository.setExpandedTopMargin(localExpandedTopMargin) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_expanded_content_horizontal_padding),
                    value = localExpandedContentHorizontalPadding,
                    range = SmartIslandSettings.MIN_EXPANDED_CONTENT_PADDING..SmartIslandSettings.MAX_EXPANDED_CONTENT_PADDING,
                    suffix = "dp",
                    step = 1f,
                    onValueChange = { localExpandedContentHorizontalPadding = it },
                    onValueChangeFinished = {
                        scope.launch { repository.setExpandedContentHorizontalPadding(localExpandedContentHorizontalPadding) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_expanded_content_vertical_padding),
                    value = localExpandedContentVerticalPadding,
                    range = SmartIslandSettings.MIN_EXPANDED_CONTENT_PADDING..SmartIslandSettings.MAX_EXPANDED_CONTENT_PADDING,
                    suffix = "dp",
                    step = 1f,
                    onValueChange = { localExpandedContentVerticalPadding = it },
                    onValueChangeFinished = {
                        scope.launch { repository.setExpandedContentVerticalPadding(localExpandedContentVerticalPadding) }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        14f to "14dp (Snug)",
                        18f to "18dp (Default)",
                        22f to "22dp (Plush)",
                        28f to "28dp (Tall)"
                    ).forEach { (paddingVal, label) ->
                        val isSelected = abs(localExpandedContentVerticalPadding - paddingVal) < 1f
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    localExpandedContentVerticalPadding = paddingVal
                                    scope.launch { repository.setExpandedContentVerticalPadding(paddingVal) }
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                // Split Island Switch Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.toggle_split_island_title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.toggle_split_island_desc),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Switch(
                        checked = localSplitIslandEnabled,
                        onCheckedChange = { checked ->
                            localSplitIslandEnabled = checked
                            scope.launch { repository.setSplitIslandEnabled(checked) }
                        }
                    )
                }
            }
        }

        // Card 5: Animation & Physics
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.title_animation_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.desc_animation_settings),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Physics Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("APPLE", stringResource(R.string.anim_preset_apple), abs(localSpringStiffness - 460f) < 30f && abs(localSpringDamping - 0.72f) < 0.04f),
                        Triple("XIAOMI", stringResource(R.string.anim_preset_xiaomi), abs(localSpringStiffness - 580f) < 30f && abs(localSpringDamping - 0.68f) < 0.04f),
                        Triple("GENTLE", stringResource(R.string.anim_preset_gentle), abs(localSpringStiffness - 340f) < 30f && abs(localSpringDamping - 0.82f) < 0.04f)
                    ).forEach { (presetKey, label, isSelected) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    scope.launch {
                                        repository.setAnimationPreset(presetKey)
                                        when (presetKey) {
                                            "XIAOMI" -> {
                                                localSpringStiffness = 580f
                                                localSpringDamping = 0.68f
                                                localAnimationDelay = 0f
                                            }
                                            "GENTLE" -> {
                                                localSpringStiffness = 340f
                                                localSpringDamping = 0.82f
                                                localAnimationDelay = 50f
                                            }
                                            else -> {
                                                localSpringStiffness = 460f
                                                localSpringDamping = 0.72f
                                                localAnimationDelay = 0f
                                            }
                                        }
                                    }
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
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_animation_delay),
                    value = localAnimationDelay,
                    range = SmartIslandSettings.MIN_ANIMATION_DELAY_MS.toFloat()..SmartIslandSettings.MAX_ANIMATION_DELAY_MS.toFloat(),
                    suffix = "ms",
                    step = 25f,
                    onValueChange = { localAnimationDelay = it },
                    onValueChangeFinished = {
                        scope.launch { repository.setAnimationDelay(localAnimationDelay.toInt()) }
                    }
                )

                // Quick Delay Preset Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0 to "0ms",
                        75 to "75ms",
                        150 to "150ms",
                        250 to "250ms",
                        400 to "400ms"
                    ).forEach { (delayVal, label) ->
                        val isSelected = abs(localAnimationDelay - delayVal) < 15f
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    localAnimationDelay = delayVal.toFloat()
                                    scope.launch { repository.setAnimationDelay(delayVal) }
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_spring_stiffness),
                    value = localSpringStiffness,
                    range = SmartIslandSettings.MIN_SPRING_STIFFNESS..SmartIslandSettings.MAX_SPRING_STIFFNESS,
                    step = 20f,
                    onValueChange = { localSpringStiffness = it },
                    onValueChangeFinished = {
                        scope.launch { repository.setAnimationSpringStiffness(localSpringStiffness) }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                SliderSettingItem(
                    label = stringResource(R.string.slider_spring_damping),
                    value = localSpringDamping,
                    range = SmartIslandSettings.MIN_SPRING_DAMPING..SmartIslandSettings.MAX_SPRING_DAMPING,
                    step = 0.02f,
                    onValueChange = { localSpringDamping = it },
                    onValueChangeFinished = {
                        scope.launch { repository.setAnimationSpringDamping(localSpringDamping) }
                    }
                )
            }
        }
    }
}

@Composable
private fun PresetCardItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        label = "presetBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "presetBg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "presetIconTint"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
