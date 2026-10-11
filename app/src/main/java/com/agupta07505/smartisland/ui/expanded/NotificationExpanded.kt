/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui.expanded

import android.app.ActivityOptions
import android.app.RemoteInput
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agupta07505.smartisland.R
import com.agupta07505.smartisland.data.SmartIslandSettings
import com.agupta07505.smartisland.di.SmartIslandRepositories
import com.agupta07505.smartisland.model.IslandChatMessage
import com.agupta07505.smartisland.model.IslandNotification
import com.agupta07505.smartisland.ui.bounceClick
import com.agupta07505.smartisland.ui.triggerHapticVibration
import com.agupta07505.smartisland.util.NotificationContentExtractor
import com.agupta07505.smartisland.util.NotificationFilter
import com.agupta07505.smartisland.util.formatNotificationTime
import kotlin.math.abs

@Composable
fun NotificationExpanded(
    notification: IslandNotification?,
    bottomPadding: Dp,
    onOpenNotification: () -> Unit,
    onCollapse: () -> Unit,
    showActions: Boolean = true,
    settings: SmartIslandSettings = SmartIslandSettings.Default,
    onReplyStateChanged: (Boolean) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isReplying by remember(notification?.key) { mutableStateOf(false) }
    var replyText by remember(notification?.key) { mutableStateOf("") }
    var isFullTextExpanded by remember(notification?.key) { mutableStateOf(false) }
    val rawText = notification?.text?.trim()?.takeIf { it.isNotBlank() }
    val rawBigText = notification?.bigText?.trim()?.takeIf { it.isNotBlank() }
    val fullPortion = when {
        !rawBigText.isNullOrBlank() -> rawBigText
        !rawText.isNullOrBlank() -> rawText
        else -> "New activity"
    }
    val smallPortion = when {
        !rawText.isNullOrBlank() && !rawBigText.isNullOrBlank() && rawText != rawBigText -> {
            if (rawText.length > 140) rawText.take(135).trimEnd() + "..." else rawText
        }
        else -> {
            val firstLine = fullPortion.substringBefore('\n').trim()
            if (firstLine.length in 1..110 && fullPortion.contains('\n')) {
                firstLine
            } else if (fullPortion.length > 110) {
                fullPortion.take(105).trimEnd() + "..."
            } else {
                fullPortion
            }
        }
    }

    val isMessaging = notification != null &&
        notification.mode == com.agupta07505.smartisland.model.IslandMode.Notification &&
        notification.isMessaging

    LaunchedEffect(isReplying) {
        onReplyStateChanged(isReplying)
        if (isReplying) {
            kotlinx.coroutines.delay(60)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    DisposableEffect(notification?.key) {
        onDispose {
            onReplyStateChanged(false)
        }
    }

    val density = LocalDensity.current
    val verticalPadding = maxOf(settings.expandedContentVerticalPadding.dp, bottomPadding, 18.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                start = settings.expandedContentHorizontalPadding.dp,
                top = verticalPadding,
                end = settings.expandedContentHorizontalPadding.dp,
                bottom = verticalPadding
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Top Header Row: Avatar, Contact / App Name, Time, Chevron, Dismiss Button ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    var totalDragY = 0f
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (totalDragY < -15f * density.density) {
                                triggerHapticVibration(context)
                                keyboardController?.hide()
                                onCollapse()
                            }
                            totalDragY = 0f
                        },
                        onDragCancel = { totalDragY = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            if (dragAmount < 0f) {
                                change.consume()
                                totalDragY += dragAmount
                            }
                        }
                    )
                },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val largeIcon = notification?.largeIcon
            val icon = notification?.icon
            val mainIcon = largeIcon ?: icon
            if (mainIcon != null) {
                val clipShape = if (largeIcon != null) CircleShape else RoundedCornerShape(8.dp)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .bounceClick { onOpenNotification() }
                ) {
                    Image(
                        bitmap = mainIcon.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(clipShape)
                    )
                    if (largeIcon != null && icon != null) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color.Black, CircleShape)
                                .padding(1.5.dp)
                        ) {
                            Image(
                                bitmap = icon.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(settings.notificationDotColor))
                        .bounceClick { onOpenNotification() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = notification?.appName?.firstOrNull()?.uppercase() ?: "S",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Contact Name / Title and App Subtitle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onOpenNotification()
                    }
            ) {
                Text(
                    text = notification?.title?.takeIf { it.isNotBlank() } ?: notification?.appName ?: "Notification",
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (notification?.appName != null && notification.appName != notification.title) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = notification.appName,
                        color = Color(0xFF9EABB8),
                        maxLines = 1,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Right header buttons: Time, Dismiss 'X'
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                // Time text
                Text(
                    text = notification?.let { formatNotificationTime(it.timeMillis) } ?: "",
                    color = Color(0xFFB7C0CA),
                    fontSize = 11.sp
                )

                // Collapse Button (Tap to collapse back into island without dismissing notification)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .bounceClick {
                            triggerHapticVibration(context)
                            keyboardController?.hide()
                            onCollapse()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Collapse island",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Dismiss Button (Tap or Swipe to dismiss)
                var buttonDragX by remember { mutableFloatStateOf(0f) }
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer { translationX = buttonDragX }
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (abs(buttonDragX) > 20f * density.density) {
                                        triggerHapticVibration(context)
                                        onDismiss()
                                    }
                                    buttonDragX = 0f
                                },
                                onDragCancel = {
                                    buttonDragX = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    buttonDragX += dragAmount * 0.7f
                                }
                            )
                        }
                        .bounceClick {
                            triggerHapticVibration(context)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss notification",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // --- 2. Middle Content: Live Conversation Bubbles OR Standard Notification Text ---
        if (isMessaging && notification != null && notification.messages.isNotEmpty()) {
            val displayMessages = if (isFullTextExpanded) {
                notification.messages.takeLast(12)
            } else {
                notification.messages.takeLast(2)
            }
            val msgScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = 0.75f,
                            stiffness = 460f
                        )
                    )
                    .heightIn(max = if (isFullTextExpanded) 220.dp else 140.dp)
                    .then(
                        if (isFullTextExpanded) {
                            Modifier.verticalScroll(msgScrollState)
                        } else Modifier
                    ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                displayMessages.forEach { msg ->
                    ChatBubbleItem(
                        message = msg,
                        isGroup = !msg.sender.isNullOrBlank() && msg.sender != notification.title,
                        maxLines = if (isFullTextExpanded) Int.MAX_VALUE else 3
                    )
                }
                if (notification.picture != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        bitmap = notification.picture.asImageBitmap(),
                        contentDescription = "Attached photo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isFullTextExpanded) 110.dp else 70.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        } else {
            val textScrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = 0.72f,
                            stiffness = 460f
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        triggerHapticVibration(context)
                        isFullTextExpanded = !isFullTextExpanded
                    }
            ) {
                Box(
                    modifier = if (isFullTextExpanded) {
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(textScrollState)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    }
                ) {
                    Text(
                        text = if (isFullTextExpanded) fullPortion else smallPortion,
                        color = Color(0xFFD5DAE0),
                        maxLines = if (isFullTextExpanded) Int.MAX_VALUE else 3,
                        overflow = if (isFullTextExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )
                }
                if (notification?.picture != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Image(
                        bitmap = notification.picture.asImageBitmap(),
                        contentDescription = "Attached photo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isFullTextExpanded) 110.dp else 70.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }

        // --- 3. Bottom Section: Inline Quick Reply OR Action Buttons ---
        if (isReplying && notification != null) {
            val quickReplyAction = notification.actionIntents.firstOrNull { it.isQuickReply && it.pendingIntent != notification.contentIntent }
                ?: notification.actionIntents.firstOrNull { NotificationContentExtractor.isReplyTitle(it.title) && it.pendingIntent != notification.contentIntent }

            val sendReplyAction: () -> Unit = {
                if (replyText.isNotBlank()) {
                    val submittedText = replyText.trim()
                    if (quickReplyAction?.pendingIntent != null) {
                        val intent = Intent()
                        val bundle = Bundle()
                        val key = quickReplyAction.remoteInputKey ?: "key_text_reply"
                        bundle.putCharSequence(key, submittedText)
                        val remoteInput = RemoteInput.Builder(key).build()
                        RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, bundle)

                        runCatching {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                val options = ActivityOptions.makeBasic()
                                    .setPendingIntentBackgroundActivityStartMode(
                                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_DENIED
                                    )
                                    .toBundle()
                                quickReplyAction.pendingIntent.send(context, 0, intent, null, null, null, options)
                            } else {
                                quickReplyAction.pendingIntent.send(context, 0, intent)
                            }
                        }
                    } else {
                        // Generic confirmation only — never echo typed content (shoulder-surfing safe).
                        Toast.makeText(context, "Reply sent", Toast.LENGTH_SHORT).show()
                    }

                    // Append user reply directly to repository so it appears immediately in the conversation!
                    val repo = SmartIslandRepositories.notificationRepository(context)
                    repo.appendUserReply(notification.key, submittedText)
                    repo.resetTimer()

                    // Clear input text and exit reply mode so user stays in the island
                    replyText = ""
                    isReplying = false
                    keyboardController?.hide()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF222222))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        singleLine = true,
                        cursorBrush = SolidColor(Color.White),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { sendReplyAction() }
                        ),
                        decorationBox = { innerTextField ->
                            if (replyText.isEmpty()) {
                                Text(
                                    text = "Reply to ${notification.title.ifBlank { notification.appName }}...",
                                    color = Color(0xFF888888),
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }

                // Send Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (replyText.isNotBlank()) MaterialTheme.colorScheme.primary else Color(0xFF333333)
                        )
                        .bounceClick {
                            sendReplyAction()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send Reply",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Cancel Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222222))
                        .bounceClick {
                            isReplying = false
                            onReplyStateChanged(false)
                            keyboardController?.hide()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cancel",
                        tint = Color(0xFFB7C0CA),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left part: Action Buttons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    // Only show prominent Reply button for authentic conversation notifications with quick reply capability!
                    val hasReplyCapability = isMessaging && notification != null &&
                        notification.isConversation &&
                        notification.actionIntents.any { it.isQuickReply }
                    if (hasReplyCapability) {
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                                .bounceClick {
                                    triggerHapticVibration(context)
                                    isReplying = true
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Reply,
                                    contentDescription = "Reply",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = stringResource(R.string.expanded_action_reply),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Prominent Dismiss Button chip (always visible in action row)
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .bounceClick {
                                triggerHapticVibration(context)
                                onDismiss()
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.expanded_action_dismiss),
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = stringResource(R.string.expanded_action_dismiss),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Render other action chips (excluding duplicate "Reply" if already shown)
                    if (showActions && notification != null && notification.actionIntents.isNotEmpty()) {
                        val otherActions = notification.actionIntents.filter { action ->
                            !action.isQuickReply && !NotificationContentExtractor.isReplyTitle(action.title)
                        }
                        otherActions.forEach { action ->
                            Box(
                                modifier = Modifier
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE2E8F0))
                                    .bounceClick {
                                        if (action.pendingIntent != null) {
                                            triggerAction(context, notification.packageName, action.pendingIntent, action.title, notification.contentIntent)
                                        } else {
                                            Toast.makeText(context, "Clicked: ${action.title}", Toast.LENGTH_SHORT).show()
                                        }
                                        val repo = SmartIslandRepositories.notificationRepository(context)
                                        repo.removeNotification(notification.key)
                                        if (!NotificationFilter.isMessagingPackage(notification.packageName)) {
                                            repo.sendCommand(com.agupta07505.smartisland.data.SmartIslandCommand.CancelNotification(notification.key))
                                        }
                                        onCollapse()
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = action.title,
                                    color = Color(0xFF1F2937),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Arrow to open target app (matching LiveActivityExpanded and NavigationExpanded)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .bounceClick {
                            triggerHapticVibration(context)
                            onOpenNotification()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Open App",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: IslandChatMessage,
    isGroup: Boolean,
    maxLines: Int = Int.MAX_VALUE
) {
    if (message.isFromUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 40.dp, max = 250.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 4.dp,
                            bottomStart = 14.dp,
                            bottomEnd = 14.dp
                        )
                    )
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.88f))
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp,
                        maxLines = maxLines,
                        overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "You",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 40.dp, max = 250.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 14.dp,
                            bottomStart = 14.dp,
                            bottomEnd = 14.dp
                        )
                    )
                    .background(Color(0xFF26262B))
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Column {
                    if (isGroup && !message.sender.isNullOrBlank()) {
                        Text(
                            text = message.sender,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = message.text,
                        color = Color(0xFFE5E7EB),
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp,
                        maxLines = maxLines,
                        overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
