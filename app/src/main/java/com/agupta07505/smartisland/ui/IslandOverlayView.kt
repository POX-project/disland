/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui

import com.agupta07505.smartisland.ui.expanded.IslandExpandedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import com.agupta07505.smartisland.util.CameraCutoutDetector
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.abs
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.agupta07505.smartisland.data.SmartIslandSettings
import com.agupta07505.smartisland.di.SmartIslandRepositories
import com.agupta07505.smartisland.model.IslandMode
import com.agupta07505.smartisland.model.IslandNotification
import com.agupta07505.smartisland.data.LaunchableApp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun IslandOverlayView(
    settings: SmartIslandSettings,
    expanded: Boolean,
    notifications: List<IslandNotification>,
    selectedIndex: Int,
    launcherApps: List<LaunchableApp>?,
    onPageSelected: (Int) -> Unit,
    onOpenNotification: (IslandNotification) -> Unit,
    onLaunchApp: (String) -> Unit,
    onToggleExpanded: () -> Unit,
    onDismissNotification: () -> Unit,
    onOpenFloatingWindow: () -> Unit,
    statusBarHeight: Float,
    modifier: Modifier = Modifier,
    isInputActive: Boolean = false,
    onReplyStateChanged: (Boolean) -> Unit = {},
    onDismissAllNotifications: () -> Unit = {},
    isFullWidth: Boolean = true,
    onExpandedHeightChanged: (Float) -> Unit = {}
) {
    // Fix #1: rememberUpdatedState ensures the lambda is always fresh
    // even though pointerInput(Unit) never restarts its coroutine
    val currentOnToggle by rememberUpdatedState(onToggleExpanded)
    val currentOnDismiss by rememberUpdatedState(onDismissNotification)
    val currentOnDismissAll by rememberUpdatedState(onDismissAllNotifications)
    val currentOnOpenFloatingWindow by rememberUpdatedState(onOpenFloatingWindow)
    val currentOnOpenNotification by rememberUpdatedState(onOpenNotification)
    val currentExpanded by rememberUpdatedState(expanded)
    val haptic = LocalHapticFeedback.current

    val scope = rememberCoroutineScope()
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val density = LocalDensity.current
    val screenWidth = with(density) { displayMetrics.widthPixels.toDp() }
    val screenCenter = screenWidth / 2f
    val expandedWidth = (screenWidth - (settings.expandedScreenMargin * 2f).dp).coerceIn(120.dp, screenWidth)
    val transition = updateTransition(targetState = expanded, label = "islandTransition")

    // Apple & Xiaomi fluid physics spring curves
    val widthSpec = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = settings.animationSpringDamping,
        stiffness = settings.animationSpringStiffness
    )
    val heightSpec = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = (settings.animationSpringDamping - 0.03f).coerceIn(0.66f, 0.85f),
        stiffness = (settings.animationSpringStiffness * 0.84f).coerceIn(300f, 650f)
    )
    val collapseWidthSpec = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = settings.animationSpringDamping,
        stiffness = (settings.animationSpringStiffness * 1.08f).coerceIn(380f, 800f)
    )
    val collapseHeightSpec = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = settings.animationSpringDamping,
        stiffness = (settings.animationSpringStiffness * 1.05f).coerceIn(380f, 800f)
    )
    val radiusSpec = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = 0.74f,
        stiffness = (settings.animationSpringStiffness * 0.95f).coerceIn(350f, 700f)
    )
    val alphaSpec = tween<Float>(
        durationMillis = 190,
        easing = FastOutSlowInEasing
    )

    val safeIndex = selectedIndex.coerceIn(0, (notifications.size - 1).coerceAtLeast(0))
    val activeNotification = notifications.getOrNull(safeIndex)
    val activeMode = activeNotification?.mode ?: IslandMode.Empty

    val initialEstimatedHeight = remember(activeMode, notifications.isEmpty()) {
        if (notifications.isEmpty()) 135.dp else defaultEstimatedHeightForMode(activeMode)
    }
    var expandedHeight by remember { mutableStateOf(initialEstimatedHeight) }

    LaunchedEffect(activeMode, notifications.isEmpty()) {
        if (!expanded) {
            expandedHeight = if (notifications.isEmpty()) 135.dp else defaultEstimatedHeightForMode(activeMode)
            onExpandedHeightChanged(expandedHeight.value)
        }
    }

    val compactGap = COMPACT_INDICATOR_GAP_DP.dp
    val effectiveCollapsedWidth = settings.width.dp
    val effectiveCollapsedHeight = settings.height.dp
    val miniPillWidth = effectiveCollapsedWidth
    val circleSize = effectiveCollapsedHeight
    val compactShapes = compactNotificationShapes(notifications.size, expanded)
    val hasCompanion = settings.splitIslandEnabled && notifications.size >= 2
    val collapsedGroupWidth = effectiveCollapsedWidth + if (hasCompanion) compactGap + circleSize else 0.dp
    val collapsedMainOffset = if (hasCompanion) {
        settings.xOffset.dp - (compactGap + circleSize) / 2f
    } else {
        settings.xOffset.dp
    }
    val collapsedSecondaryOffset = settings.xOffset.dp + (effectiveCollapsedWidth + compactGap) / 2f
    val hasHardwareNotch = remember { CameraCutoutDetector.hasHardwareNotch(context) }
    val isNotchActive = settings.pinchNotchEnabled || hasHardwareNotch
    val effectiveCollapsedY = if (isFullWidth) settings.yOffset.dp else 0.dp
    val expandedTopOffset = settings.expandedTopMargin.dp
    val isIdleHiding = settings.hideWhenIdle && notifications.isEmpty()

    var isAutoHidden by remember { mutableStateOf(false) }
    var userInteractionTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    // Reset auto-hide whenever active notifications change or selection changes
    LaunchedEffect(notifications.map { it.key }, selectedIndex) {
        isAutoHidden = false
        userInteractionTimestamp = System.currentTimeMillis()
    }

    // Auto-hide countdown timer when pill is collapsed and autoHidePill is enabled
    LaunchedEffect(expanded, settings.autoHidePill, settings.autoHideTimeoutSeconds, userInteractionTimestamp) {
        if (expanded || !settings.autoHidePill) {
            isAutoHidden = false
            return@LaunchedEffect
        }
        val timeoutMs = (settings.autoHideTimeoutSeconds.coerceAtLeast(1) * 1000L)
        kotlinx.coroutines.delay(timeoutMs)
        isAutoHidden = true
    }

    val isHiding = isIdleHiding || (settings.autoHidePill && isAutoHidden)

    val animatedExpandedHeight by animateDpAsState(
        targetValue = expandedHeight,
        animationSpec = heightSpec,
        label = "animatedExpandedHeight"
    )

    val width by transition.animateDp(
        transitionSpec = {
            if (targetState) widthSpec else collapseWidthSpec
        },
        label = "islandWidth"
    ) {
        if (it) expandedWidth else if (isHiding) 0.dp else effectiveCollapsedWidth
    }
    val height by transition.animateDp(
        transitionSpec = {
            if (targetState) heightSpec else collapseHeightSpec
        },
        label = "islandHeight"
    ) {
        if (it) animatedExpandedHeight else if (isHiding) 0.dp else effectiveCollapsedHeight
    }
    val yOffset by transition.animateDp(
        transitionSpec = {
            if (targetState) {
                heightSpec
            } else {
                // When collapsing: anchor eagerly to the top of the screen (0.dp)
                // so the island card retracts straight upward into the notch/top bezel
                // without stopping or lingering under the camera cutout!
                tween(durationMillis = 80, easing = FastOutLinearInEasing)
            }
        },
        label = "islandYOffset"
    ) {
        if (it) expandedTopOffset else effectiveCollapsedY
    }
    val radius by transition.animateDp(
        transitionSpec = { radiusSpec },
        label = "islandRadius"
    ) {
        if (it) 34.dp else if (isHiding) 0.dp else settings.cornerRadius.dp
    }
    val animatedXOffset by transition.animateDp(
        transitionSpec = {
            if (targetState) widthSpec else collapseWidthSpec
        },
        label = "islandXOffset"
    ) {
        if (it) 0.dp else collapsedMainOffset
    }

    val pinchProgress by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(durationMillis = 110, easing = FastOutSlowInEasing)
            } else {
                spring(
                    dampingRatio = settings.animationSpringDamping,
                    stiffness = (settings.animationSpringStiffness * 1.05f).coerceIn(380f, 800f)
                )
            }
        },
        label = "pinchProgress"
    ) {
        if (it || !isNotchActive) 0f else 1f
    }

    val collapsedAlpha by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(durationMillis = 75, easing = FastOutLinearInEasing)
            } else {
                val delay = (settings.animationDelayMs.coerceAtLeast(0) * 0.25f).toInt().coerceAtMost(50)
                tween(
                    durationMillis = 140,
                    delayMillis = 40 + delay,
                    easing = FastOutSlowInEasing
                )
            }
        },
        label = "collapsedAlpha"
    ) {
        if (it || isHiding) 0f else 1f
    }

    val collapsedScale by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(durationMillis = 75, easing = FastOutLinearInEasing)
            } else {
                spring(
                    dampingRatio = 0.70f,
                    stiffness = (settings.animationSpringStiffness * 1.05f).coerceIn(400f, 750f)
                )
            }
        },
        label = "collapsedScale"
    ) {
        if (it) 0.82f else 1f
    }

    val expandedAlpha by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                val delay = (settings.animationDelayMs.coerceAtLeast(0) * 0.25f).toInt().coerceAtMost(50)
                tween(
                    durationMillis = 160,
                    delayMillis = 30 + delay,
                    easing = FastOutSlowInEasing
                )
            } else {
                tween(
                    durationMillis = 80,
                    easing = FastOutLinearInEasing
                )
            }
        },
        label = "expandedAlpha"
    ) {
        if (it) 1f else 0f
    }

    val contentScale by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                spring(
                    dampingRatio = (settings.animationSpringDamping - 0.02f).coerceIn(0.68f, 0.82f),
                    stiffness = (settings.animationSpringStiffness * 0.88f).coerceIn(320f, 600f)
                )
            } else {
                tween(durationMillis = 80, easing = FastOutLinearInEasing)
            }
        },
        label = "contentScale"
    ) {
        if (it) 1f else 0.90f
    }

    val contentSlideY by transition.animateDp(
        transitionSpec = {
            if (targetState) {
                spring(
                    dampingRatio = (settings.animationSpringDamping - 0.02f).coerceIn(0.68f, 0.82f),
                    stiffness = (settings.animationSpringStiffness * 0.88f).coerceIn(320f, 600f)
                )
            } else {
                tween(durationMillis = 80, easing = FastOutLinearInEasing)
            }
        },
        label = "contentSlideY"
    ) {
        if (it) 0.dp else (-8).dp
    }

    val safeWidth = width.coerceAtLeast(0.dp)
    val safeHeight = height.coerceAtLeast(0.dp)
    val safeRadius = radius.coerceAtLeast(0.dp)

    val islandShape = remember(pinchProgress, safeRadius, isNotchActive) {
        if (isNotchActive || pinchProgress > 0f) {
            PinchIslandShape(pinchProgress, safeRadius)
        } else {
            RoundedCornerShape(safeRadius)
        }
    }

    // Tactile spring scale bounce animation only when user switches between active notifications
    val switchScaleAnim = remember { androidx.compose.animation.core.Animatable(1f) }
    var isInitialComposition by remember { mutableStateOf(true) }
    var lastSelectedIndex by remember { mutableStateOf(selectedIndex) }
    LaunchedEffect(selectedIndex) {
        if (isInitialComposition) {
            isInitialComposition = false
            lastSelectedIndex = selectedIndex
            return@LaunchedEffect
        }
        if (lastSelectedIndex != selectedIndex) {
            lastSelectedIndex = selectedIndex
            switchScaleAnim.animateTo(
                targetValue = 0.92f,
                animationSpec = tween(40, easing = FastOutSlowInEasing)
            )
            switchScaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = 650f
                )
            )
        }
    }

    // Dual Pill (Multi-Tasking Split Island) Detection:
    // When 2 or more notifications exist and split island is enabled, split into Main Pill + Secondary Bubble
    val secondaryNotification = if (settings.splitIslandEnabled && notifications.size >= 2) {
        notifications.firstOrNull { it.key != activeNotification?.key }
    } else null
    val secondaryIndex = if (secondaryNotification != null) {
        notifications.indexOfFirst { it.key == secondaryNotification.key }
    } else -1
    val tertiaryNotification = if (settings.splitIslandEnabled && notifications.size >= 3) {
        notifications.firstOrNull {
            it.key != activeNotification?.key && it.key != secondaryNotification?.key
        }
    } else null
    val tertiaryIndex = if (tertiaryNotification != null) {
        notifications.indexOfFirst { it.key == tertiaryNotification.key }
    } else -1
    val isSplitMode = secondaryNotification != null
    val secondaryIsPill = compactShapes.singleOrNull() == CompactNotificationShape.MiniPill
    val showTertiaryPill = compactShapes.size == 2 && tertiaryNotification != null

    val secondaryAlpha by animateFloatAsState(
        targetValue = if (isSplitMode && !isHiding) 1f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "secondaryAlpha"
    )
    val secondaryScale by animateFloatAsState(
        targetValue = if (isSplitMode && !isHiding) 1f else 0.3f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 480f),
        label = "secondaryScale"
    )
    val secondaryBubbleWidth by animateDpAsState(
        targetValue = if (secondaryIsPill) miniPillWidth else circleSize,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 520f),
        label = "secondaryBubbleWidth"
    )
    val secondaryPillProgress = (miniPillWidth - circleSize).value.let { widthDelta ->
        if (widthDelta == 0f) {
            if (secondaryIsPill) 1f else 0f
        } else {
            ((secondaryBubbleWidth - circleSize).value / widthDelta).coerceIn(0f, 1f)
        }
    }
    val secondaryBubbleCorner by animateDpAsState(
        targetValue = if (secondaryIsPill) settings.cornerRadius.dp else circleSize / 2f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 520f),
        label = "secondaryBubbleCorner"
    )
    val tertiaryAlpha by animateFloatAsState(
        targetValue = if (showTertiaryPill && !isHiding) 1f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "tertiaryAlpha"
    )
    val tertiaryScale by animateFloatAsState(
        targetValue = if (showTertiaryPill && !isHiding) 1f else 0.3f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 480f),
        label = "tertiaryScale"
    )

    val expandedCompactX = screenCenter + settings.xOffset.dp - effectiveCollapsedWidth / 2f
    val secondaryOffset by animateDpAsState(
        targetValue = when {
            !expanded -> collapsedSecondaryOffset
            secondaryIsPill -> if (isFullWidth) (expandedCompactX - screenCenter + miniPillWidth / 2f) else 0.dp
            else -> if (isFullWidth) (expandedCompactX + miniPillWidth + compactGap - screenCenter + circleSize / 2f) else ((miniPillWidth + compactGap) / 2f)
        },
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 520f),
        label = "secondaryOffset"
    )

    // Outer Box: Does not intercept touches outside the island card so underlying items receive them
    val outerModifier = modifier.fillMaxSize()

    Box(
        modifier = outerModifier,
        contentAlignment = Alignment.TopCenter
    ) {

        // Invisible touch target over the pill location when hiding, so tapping the area reveals the pill or opens shortcuts
        if (isHiding && !currentExpanded) {
            Box(
                modifier = Modifier
                    .width(settings.width.dp)
                    .height(settings.height.dp)
                    .graphicsLayer {
                        translationX = collapsedMainOffset.toPx()
                        translationY = effectiveCollapsedY.toPx()
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (settings.autoHidePill && isAutoHidden) {
                                // First tap on auto-hidden pill: awaken and reveal the pill
                                isAutoHidden = false
                                userInteractionTimestamp = System.currentTimeMillis()
                            } else {
                                // Empty notifications idle hiding: expand favorite shortcuts
                                currentOnToggle()
                            }
                        }
                    }
            )
        }

        // Inner Box: The actual visible pill container, managing the black background shape and size animations
        Box(
            modifier = Modifier
                .width(safeWidth)
                .height(safeHeight)
                .graphicsLayer {
                    translationX = animatedXOffset.toPx() + dragOffsetX
                    translationY = yOffset.toPx() + dragOffsetY
                    scaleX = switchScaleAnim.value
                    scaleY = switchScaleAnim.value
                }
                .then(
                    if (settings.enableShadow && !isHiding && settings.shadowElevation > 0f) {
                        Modifier.shadow(
                            elevation = if (currentExpanded) (settings.shadowElevation * 1.25f).dp else settings.shadowElevation.dp,
                            shape = islandShape,
                            clip = false,
                            ambientColor = Color(settings.shadowColor).copy(alpha = settings.shadowAlpha),
                            spotColor = Color(settings.shadowColor).copy(alpha = settings.shadowAlpha)
                        )
                    } else Modifier
                )
                .clip(islandShape)
                .background(Color(settings.islandBackgroundColor).copy(alpha = settings.opacity))
                .then(
                    if (settings.borderWidth > 0f && settings.borderAlpha > 0f) {
                        Modifier.border(
                            width = settings.borderWidth.dp,
                            color = Color(settings.borderColor).copy(alpha = settings.borderAlpha),
                            shape = islandShape
                        )
                    } else Modifier
                )
                .drawWithContent {
                    drawContent()
                    if (settings.innerShadowRadius > 0f && settings.innerShadowAlpha > 0f) {
                        val strokeRadiusPx = settings.innerShadowRadius.dp.toPx()
                        val steps = 4
                        val innerColor = Color(settings.innerShadowColor)
                        for (step in 1..steps) {
                            val stepFraction = step.toFloat() / steps
                            val currentStrokePx = strokeRadiusPx * stepFraction * 2f
                            val currentAlpha = (settings.innerShadowAlpha / steps) * (1f - stepFraction * 0.4f)
                            drawShapeOutline(
                                outline = islandShape.createOutline(size, layoutDirection, this),
                                color = innerColor.copy(alpha = currentAlpha),
                                style = Stroke(width = currentStrokePx)
                            )
                        }
                    }
                }
                .pointerInput(displayMetrics.density, isInputActive) {
                    if (isInputActive) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        userInteractionTimestamp = System.currentTimeMillis()
                        val pressTimeMs = System.currentTimeMillis()
                        var isHoldRegistered = false
                        var dragAccumulatorX = 0f
                        var dragAccumulatorY = 0f
                        var isDragging = false
                        var isDraggingHorizontal = false

                        val holdJob = scope.launch {
                            kotlinx.coroutines.delay(HOLD_GESTURE_THRESHOLD_MS)
                            isHoldRegistered = true
                            triggerHapticVibration(context)
                        }

                        val pointerId = down.id

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                            if (change.changedToUp()) {
                                change.consume()
                                holdJob.cancel()
                                val totalElapsedMs = System.currentTimeMillis() - pressTimeMs
                                val swipeUpThreshold = -SWIPE_THRESHOLD_DP * displayMetrics.density
                                val swipeDownThreshold = SWIPE_THRESHOLD_DP * displayMetrics.density
                                val swipeDismissThreshold = 45f * displayMetrics.density

                                if (isDraggingHorizontal) {
                                    // DEDICATED HORIZONTAL SWIPE TO DISMISS
                                    // Completely distinct from the vertical collapse gesture!
                                    if (abs(dragOffsetX) > swipeDismissThreshold) {
                                        triggerHapticVibration(context)
                                        val exitTarget = if (dragOffsetX > 0) displayMetrics.widthPixels.toFloat() else -displayMetrics.widthPixels.toFloat()
                                        scope.launch {
                                            androidx.compose.animation.core.Animatable(dragOffsetX).animateTo(
                                                targetValue = exitTarget,
                                                animationSpec = tween(120, easing = FastOutLinearInEasing)
                                            )
                                            currentOnDismiss()
                                            dragOffsetX = 0f
                                        }
                                    } else {
                                        scope.launch {
                                            androidx.compose.animation.core.Animatable(dragOffsetX).animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            ) {
                                                dragOffsetX = value
                                            }
                                        }
                                    }
                                } else if (isDragging) {
                                    // VERTICAL SWIPE: COLLAPSE OR EXPAND (PRESERVES NOTIFICATION)
                                    if (currentExpanded) {
                                        if (dragOffsetY < swipeUpThreshold) {
                                            // Swipe UP: COLLAPSE THE ISLAND! (Preserves notification)
                                            triggerHapticVibration(context)
                                            currentOnToggle()
                                        } else if (dragOffsetY > swipeDownThreshold) {
                                            currentOnOpenFloatingWindow()
                                        }
                                    } else {
                                        if (dragOffsetY > swipeDownThreshold) {
                                            // Swipe DOWN: EXPAND THE ISLAND!
                                            triggerHapticVibration(context)
                                            currentOnToggle()
                                        } else if (dragOffsetY < swipeUpThreshold) {
                                            // Swipe UP in collapsed mode: tuck bounce, PRESERVE notification!
                                            triggerHapticVibration(context)
                                        }
                                    }
                                    scope.launch {
                                        androidx.compose.animation.core.Animatable(dragOffsetY).animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        ) {
                                            dragOffsetY = value
                                        }
                                    }
                                } else {
                                    // Tap gesture: cleanly toggles expand/collapse while preserving notifications
                                    if (!isHoldRegistered) {
                                        currentOnToggle()
                                    }
                                }
                                break
                            } else if (change.isConsumed) {
                                holdJob.cancel()
                                break
                            } else {
                                val posChange = change.positionChange()
                                dragAccumulatorX += posChange.x
                                dragAccumulatorY += posChange.y

                                if (!isDragging && (abs(dragAccumulatorX) > 6f * displayMetrics.density || abs(dragAccumulatorY) > 6f * displayMetrics.density)) {
                                    isDragging = true
                                    isDraggingHorizontal = abs(dragAccumulatorX) > abs(dragAccumulatorY)
                                    holdJob.cancel()
                                }

                                if (isDragging) {
                                    change.consume()
                                    if (isDraggingHorizontal) {
                                        dragOffsetX = (dragOffsetX + posChange.x * 0.7f).coerceIn(
                                            -displayMetrics.widthPixels.toFloat(),
                                            displayMetrics.widthPixels.toFloat()
                                        )
                                    } else {
                                        dragOffsetY = (dragOffsetY + posChange.y * 0.4f).coerceIn(
                                            -DRAG_MAX_OFFSET_DP * displayMetrics.density,
                                            DRAG_MAX_OFFSET_DP * displayMetrics.density
                                        )
                                    }
                                }
                            }
                        }

                        holdJob.cancel()
                        if (dragOffsetY != 0f) {
                            scope.launch {
                                androidx.compose.animation.core.Animatable(dragOffsetY).animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) {
                                    dragOffsetY = value
                                }
                            }
                        }
                        if (dragOffsetX != 0f) {
                            scope.launch {
                                androidx.compose.animation.core.Animatable(dragOffsetX).animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) {
                                    dragOffsetX = value
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.TopCenter
        ) {
            // Collapsed content layer (pinned to fixed pill bounds at top-center, cancelling yOffset)
            if (collapsedAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .width(effectiveCollapsedWidth)
                        .height(effectiveCollapsedHeight)
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            alpha = collapsedAlpha
                            scaleX = collapsedScale
                            scaleY = collapsedScale
                        }
                ) {
                    IslandCollapsedContent(
                        mode = activeMode,
                        notification = activeNotification,
                        collapsedAlpha = collapsedAlpha,
                        settings = settings
                    )
                }
            }

            // Expanded content layer — smoothly fade out while collapsing
            if (expanded || expandedAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .graphicsLayer {
                            alpha = expandedAlpha
                            scaleX = contentScale
                            scaleY = contentScale
                            translationY = contentSlideY.toPx()
                        }
                ) {
                    IslandExpandedContent(
                        notifications = notifications,
                        launcherApps = launcherApps,
                        selectedIndex = selectedIndex,
                        onPageSelected = onPageSelected,
                        onOpenNotification = onOpenNotification,
                        onLaunchApp = onLaunchApp,
                        onCollapse = onToggleExpanded,
                        statusBarHeight = statusBarHeight.dp,
                        // Each mode owns its natural height. The launcher already
                        // supplies its own loading height and must not impose that
                        // minimum on compact call or battery content.
                        onHeightMeasured = {
                            expandedHeight = it
                            onExpandedHeightChanged(it.value)
                        },
                        settings = settings,
                        onReplyStateChanged = onReplyStateChanged,
                        onDismissNotification = currentOnDismiss
                    )
                }
            }
        }

        // Collapsed: secondary circle. Expanded with 2: the same item morphs
        // into a full-size pill. Expanded with 3+: it stays the circle on the right.
        if (secondaryAlpha > 0f && secondaryNotification != null) {
            Box(
                modifier = Modifier
                    .absoluteOffset {
                        IntOffset(
                            secondaryOffset.roundToPx(),
                            yOffset.roundToPx()
                        )
                    }
                    .width(secondaryBubbleWidth)
                    .height(circleSize)
                    .graphicsLayer {
                        alpha = secondaryAlpha
                        scaleX = secondaryScale * switchScaleAnim.value
                        scaleY = secondaryScale * switchScaleAnim.value
                    }
                    .then(
                        if (settings.enableShadow && settings.shadowElevation > 0f) {
                            Modifier.shadow(
                                elevation = (settings.shadowElevation * 0.75f).dp,
                                shape = RoundedCornerShape(secondaryBubbleCorner),
                                clip = false,
                                ambientColor = Color(settings.shadowColor).copy(alpha = settings.shadowAlpha),
                                spotColor = Color(settings.shadowColor).copy(alpha = settings.shadowAlpha)
                            )
                        } else Modifier
                    )
                    .clip(RoundedCornerShape(secondaryBubbleCorner))
                    .background(Color(settings.islandBackgroundColor).copy(alpha = settings.opacity))
                    .then(
                        if (settings.borderWidth > 0f && settings.borderAlpha > 0f) {
                            Modifier.border(
                                width = settings.borderWidth.dp,
                                color = Color(settings.borderColor).copy(alpha = settings.borderAlpha),
                                shape = RoundedCornerShape(secondaryBubbleCorner)
                            )
                        } else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (secondaryIndex >= 0) {
                            onPageSelected(secondaryIndex)
                        }
                        if (!currentExpanded) {
                            currentOnToggle()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 1f - secondaryPillProgress },
                    contentAlignment = Alignment.Center
                ) {
                    SecondaryBubbleContent(
                        notification = secondaryNotification,
                        settings = settings
                    )
                }
                Box(
                    modifier = Modifier
                        .requiredWidth(miniPillWidth)
                        .height(circleSize)
                        .graphicsLayer { alpha = secondaryPillProgress },
                    contentAlignment = Alignment.Center
                ) {
                    IslandCollapsedContent(
                        mode = secondaryNotification.mode,
                        notification = secondaryNotification,
                        collapsedAlpha = 1f,
                        settings = settings
                    )
                }
            }
        }

        if (tertiaryAlpha > 0f && tertiaryNotification != null) {
            Box(
                modifier = Modifier
                    .absoluteOffset {
                        IntOffset(
                            (expandedCompactX - screenCenter + miniPillWidth / 2f).roundToPx(),
                            yOffset.roundToPx()
                        )
                    }
                    .width(miniPillWidth)
                    .height(circleSize)
                    .graphicsLayer {
                        alpha = tertiaryAlpha
                        scaleX = tertiaryScale * switchScaleAnim.value
                        scaleY = tertiaryScale * switchScaleAnim.value
                    }
                    .then(
                        if (settings.enableShadow && settings.shadowElevation > 0f) {
                            Modifier.shadow(
                                elevation = (settings.shadowElevation * 0.75f).dp,
                                shape = RoundedCornerShape(settings.cornerRadius.dp),
                                clip = false,
                                ambientColor = Color(settings.shadowColor).copy(alpha = settings.shadowAlpha),
                                spotColor = Color(settings.shadowColor).copy(alpha = settings.shadowAlpha)
                            )
                        } else Modifier
                    )
                    .clip(RoundedCornerShape(settings.cornerRadius.dp))
                    .background(Color(settings.islandBackgroundColor).copy(alpha = settings.opacity))
                    .then(
                        if (settings.borderWidth > 0f && settings.borderAlpha > 0f) {
                            Modifier.border(
                                width = settings.borderWidth.dp,
                                color = Color(settings.borderColor).copy(alpha = settings.borderAlpha),
                                shape = RoundedCornerShape(settings.cornerRadius.dp)
                            )
                        } else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (tertiaryIndex >= 0) {
                            onPageSelected(tertiaryIndex)
                        }
                        if (!currentExpanded) {
                            currentOnToggle()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                IslandCollapsedContent(
                    mode = tertiaryNotification.mode,
                    notification = tertiaryNotification,
                    collapsedAlpha = 1f,
                    settings = settings
                )
            }
        }
    }
}

@Composable
private fun SecondaryBubbleContent(
    notification: IslandNotification,
    settings: SmartIslandSettings
) {
    when (notification.mode) {
        IslandMode.Bluetooth -> {
            Image(
                painter = painterResource(id = com.agupta07505.smartisland.R.drawable.ic_bluetooth_device),
                contentDescription = "Bluetooth Device",
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
            )
        }
        IslandMode.Flashlight -> {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF59E0B).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.FlashlightOn,
                    contentDescription = "Flashlight",
                    tint = Color(0xFFFACC15),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        IslandMode.Hotspot -> {
            HotspotCollapsedGlyph(notification = notification, settings = settings)
        }
        IslandMode.Battery -> {
            BatteryCollapsedGlyph(notification = notification, settings = settings)
        }
        IslandMode.LiveActivity -> {
            LiveActivityCollapsedGlyph(notification = notification, settings = settings)
        }
        IslandMode.Navigation -> {
            NavigationCollapsedGlyph(notification = notification, settings = settings)
        }
        IslandMode.IncomingCall -> {
            val icon = notification.largeIcon ?: notification.icon
            if (icon != null) {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    Icons.Rounded.Call,
                    contentDescription = null,
                    tint = Color(settings.callColor),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        IslandMode.Music -> {
            val artwork = notification.largeIcon ?: notification.icon
            if (artwork != null) {
                Image(
                    bitmap = artwork.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(settings.musicVisualizerColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        IslandMode.ScreenRecording -> {
            ScreenRecordingCollapsedGlyph(settings = settings)
        }
        IslandMode.Timer -> {
            TimerCollapsedGlyph(notification = notification, settings = settings)
        }
        IslandMode.Stopwatch -> {
            StopwatchCollapsedGlyph(notification = notification, settings = settings)
        }
        IslandMode.Notification, IslandMode.DownloadUpload, IslandMode.Empty -> {
            NotificationGlyph(notification = notification, settings = settings)
        }
    }
}

// Animation specs
private const val EXPANDED_WIDTH_RATIO = 0.95f
private const val SWIPE_THRESHOLD_DP = 35f
private const val DRAG_MAX_OFFSET_DP = 100f
private const val COMPACT_INDICATOR_GAP_DP = 8f
private const val HOLD_GESTURE_THRESHOLD_MS = 300L

internal fun triggerHapticVibration(context: android.content.Context) {
    runCatching {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vm = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            val vibrator = vm?.defaultVibrator
            if (vibrator?.hasVibrator() == true) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(60L, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                return
            }
        }
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        if (vibrator?.hasVibrator() == true) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(60L, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(60L)
            }
        }
    }
}

internal enum class CompactNotificationShape { MiniPill, Circle }

internal fun defaultEstimatedHeightForMode(mode: IslandMode?): Dp {
    return when (mode) {
        IslandMode.Music -> 175.dp
        IslandMode.Notification -> 135.dp
        IslandMode.IncomingCall, IslandMode.Battery -> 115.dp
        IslandMode.LiveActivity, IslandMode.Navigation -> 180.dp
        IslandMode.DownloadUpload, IslandMode.Hotspot -> 160.dp
        IslandMode.Bluetooth, IslandMode.Flashlight, IslandMode.ScreenRecording,
        IslandMode.Timer, IslandMode.Stopwatch -> 115.dp
        IslandMode.Empty, null -> 135.dp
    }
}

internal fun compactNotificationShapes(
    notificationCount: Int,
    expanded: Boolean
): List<CompactNotificationShape> = when {
    notificationCount < 2 -> emptyList()
    !expanded -> listOf(CompactNotificationShape.Circle)
    notificationCount == 2 -> listOf(CompactNotificationShape.MiniPill)
    else -> listOf(CompactNotificationShape.MiniPill, CompactNotificationShape.Circle)
}

private fun DrawScope.drawShapeOutline(
    outline: Outline,
    color: Color,
    style: DrawStyle
) {
    when (outline) {
        is Outline.Rectangle -> drawRect(color = color, topLeft = Offset(outline.rect.left, outline.rect.top), size = outline.rect.size, style = style)
        is Outline.Rounded -> drawRoundRect(
            color = color,
            topLeft = Offset(outline.roundRect.left, outline.roundRect.top),
            size = Size(outline.roundRect.width, outline.roundRect.height),
            cornerRadius = CornerRadius(outline.roundRect.topLeftCornerRadius.x, outline.roundRect.topLeftCornerRadius.y),
            style = style
        )
        is Outline.Generic -> drawPath(path = outline.path, color = color, style = style)
    }
}

