/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.util

import android.app.Notification
import android.os.Bundle
import com.agupta07505.smartisland.model.IslandNotificationAction

enum class CallState {
    RINGING,
    DIALING,
    ACTIVE
}

object CallStateParser {

    private val ANSWER_KEYWORDS = listOf(
        "answer", "accept", "take", "receive",
        "contestar", "responder", "répondre", "repondre",
        "annehmen", "atender", "接听", "ответить"
    )

    private val DIALING_KEYWORDS = listOf(
        // English
        "calling", "dialing", "dialling", "connecting", "outgoing call",
        // Portuguese
        "chamando", "ligando", "discando", "chamada efetuada", "a chamar",
        // Spanish
        "llamando", "marcando", "llamada saliente", "conectando",
        // German
        "wählt", "waehlt", "ruft an", "ausgehender anruf", "verbinden",
        // French
        "appel sortant", "numérotation", "numerotation",
        // Italian
        "chiamata in uscita", "composizione", "chiamando", "connessione",
        // Russian
        "исходящий вызов", "исходящий", "набор номера", "вызов", "соединение",
        // Hindi
        "कॉल किया जा रहा", "डायल हो रहा",
        // Chinese
        "正在拨号", "正在呼叫", "呼叫中", "拨号中", "正在连接",
        // Japanese
        "発信中", "発信",
        // Korean
        "전화 거는 중", "연결 중"
    )

    private val RINGING_KEYWORDS = listOf(
        "incoming call", "chamada recebida", "appel entrant",
        "eingehender anruf", "llamada entrante", "входящий вызов",
        "ringing", "착신 전화", "着信"
    )

    private val ACTIVE_CALL_KEYWORDS = listOf(
        "ongoing call", "call in progress", "active call", "in call",
        "chamada em andamento", "llamada en curso", "appel en cours",
        "laufender anruf", "chiamata in corso", "通话中", "통화 중", "通話中"
    )

    private val DURATION_REGEX = Regex("""\b\d{1,2}:\d{2}(?::\d{2})?\b""")

    /**
     * Determines whether a call notification is currently ringing (incoming call awaiting answer).
     */
    fun isCallRinging(
        actions: List<IslandNotificationAction>,
        title: String,
        text: String,
        bigText: String? = null,
        extras: Bundle? = null
    ): Boolean {
        // 1. Has an answer action
        val hasAnswerAction = actions.any { action ->
            val label = action.title.lowercase()
            ANSWER_KEYWORDS.any { label.contains(it) }
        }
        if (hasAnswerAction) return true

        // 2. CallStyle template check for incoming call
        if (extras != null) {
            val callType = runCatching { extras.getInt("android.callType", -1) }.getOrDefault(-1)
            if (callType == 1) { // Notification.CallStyle.CALL_TYPE_INCOMING
                return true
            }
            if (extras.containsKey("android.answerIntent")) {
                return true
            }
        }

        // 3. Ringing keywords in title/text if no answer action present
        val combined = "$title $text ${bigText.orEmpty()}".lowercase()
        return RINGING_KEYWORDS.any { combined.contains(it) }
    }

    /**
     * Determines whether a call notification is currently in the "calling / dialing / connecting"
     * phase (outgoing call placed, but NOT yet answered / started).
     */
    fun isCallDialing(
        actions: List<IslandNotificationAction>,
        title: String,
        text: String,
        bigText: String? = null,
        extras: Bundle? = null
    ): Boolean {
        // If it's ringing, it's not dialing
        if (isCallRinging(actions, title, text, bigText, extras)) return false

        // If system chronometer is active, the call is already connected and running!
        if (extras != null) {
            val showChronometer = extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER, false) ||
                extras.getBoolean("android.showChronometer", false)
            if (showChronometer) return false
        }

        val combined = "$title $text ${bigText.orEmpty()}".lowercase()

        // If text explicitly matches an active call keyword or an active timer ("00:15"), it is not dialing
        if (ACTIVE_CALL_KEYWORDS.any { combined.contains(it) }) return false
        if (DURATION_REGEX.containsMatchIn(text) || DURATION_REGEX.containsMatchIn(title)) return false

        // Check for dialing / calling keywords
        return DIALING_KEYWORDS.any { combined.contains(it) }
    }

    /**
     * Determines whether the call has officially started / is active and connected.
     */
    fun isCallActive(
        actions: List<IslandNotificationAction>,
        title: String,
        text: String,
        bigText: String? = null,
        extras: Bundle? = null
    ): Boolean {
        if (isCallRinging(actions, title, text, bigText, extras)) return false
        if (isCallDialing(actions, title, text, bigText, extras)) return false
        return true
    }

    /**
     * Parses the current CallState from notification contents.
     */
    fun parseCallState(
        actions: List<IslandNotificationAction>,
        title: String,
        text: String,
        bigText: String? = null,
        extras: Bundle? = null
    ): CallState {
        return when {
            isCallRinging(actions, title, text, bigText, extras) -> CallState.RINGING
            isCallDialing(actions, title, text, bigText, extras) -> CallState.DIALING
            else -> CallState.ACTIVE
        }
    }
}
