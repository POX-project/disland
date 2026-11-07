# Smart Island (POX-project/disland) — Deep Formal Analysis Handoff

*Generated: 2026-11-06 | Branch: main | Version: 7.0.0 (app) | Commit: 0761285*

This document is a rigorous, trace-based mental model of the codebase as analyzed. Use it for safe evolution/refactors/verification.

## 1) Graph Topology & Centrality Matrix (structural)

Top modules ranked by approximate afferent/structural coupling (based on symbol references). Values are proxies ($C_a$ in-degree of references across repo; $C_e$ out-dependencies inferred from imports/usages).

| Module / File | Role | $C_a$ (ref count) | $C_e$ (out-dep) | Instability $I=C_e/(C_a+C_e)$ | Core Symbols Defined |
| --- | --- | --- | --- | --- | --- |
| `data/SmartIslandSettings.kt` | Configuration schema/lattice | 31 | High (DataStore/JSON) | ~0.8 (volatile/config) | `SmartIslandSettings`, ranges/`fromJson`/`toJson` |
| `data/SmartIslandNotificationRepository.kt` | Central notification state hub (MVI-like) | 2 (+heavy usage) | Med (models/flows) | Mid | `SmartIslandNotificationRepository`, `SmartIslandCommand` (sealed) |
| `data/INotificationRepository.kt`, `INotificationHistoryRepository.kt` | Contracts | High usage | Low | Low | Interfaces |
| `data/SmartIslandSettingsRepository.kt` | Settings persistence (DataStore) | 16 | High | ~0.7 | `SmartIslandSettingsRepository` |
| `model/IslandNotification.kt`, `model/IslandMode.kt`, `model/IslandProfile.kt` | Domain model | 25+ | Low | Low (core) | `IslandNotification`, `IslandChatMessage`, `IslandNotificationAction`, `IslandMode`, `IslandProfile` |
| `service/SmartIslandOverlayService.kt` | Overlay/window manager + accessibility service (entry/orchestrator) | 5 | Very High (WM, Compose, VM, repos, cutout, OEM) | High (~0.9) | `SmartIslandOverlayService`, window/layout/touch-region logic |
| `service/SmartIslandNotificationListenerService.kt` | Notification interception/suppression/parsing/history (entry/orchestrator) | 5 | Very High (NL, parsers, filters, history, media, lyrics) | High (~0.9) | `SmartIslandNotificationListenerService` |
| `ui/IslandViewModel.kt` | Presentation state machine/controller | 3 | Med-High (repos, flows, filters) | Mid-High | `IslandViewModel` |
| `ui/IslandOverlayView.kt`, `ui/OverlayIsland.kt`, `ui/IslandCollapsedContent.kt` | Compose UI | Used widely | Med | Mid | UI composables |
| `ui/expanded/IslandExpandedContent.kt` (+ mode expanders) | Mode dispatcher/renderers | Used | Med | Mid | Dispatcher + per-mode expanded UIs |
| `util/NotificationFilter.kt` | Policy engine (suppress/allow/messaging) | 8 | Med (parsers, packages) | Mid-High | `NotificationFilter` |
| `util/NotificationContentExtractor.kt` | Extraction (styles, actions, messages, pictures) | 4 | Med | Mid | Extraction logic |
| `util/TimerStopwatchParser.kt`, `util/CallStateParser.kt`, `util/NavigationParser.kt`, `util/LiveActivityParser.kt` | Domain parsers | Used | Low-Med | Mid | Parsers |
| `util/OemDeviceRules.kt`, `util/OemAutostartUtil.kt`, `util/ShizukuManager.kt`, `util/SystemServiceRecovery.kt` | OEM/permissions/recovery | Used | Med | Mid-High | OEM rules/utilities |
| `di/AppModule.kt`, `di/SmartIslandRepositories.kt` | Wiring (Hilt/singleton + entry points) | Wiring | Low | Low | DI modules/entry points |
| `receiver/AutostartReceiver.kt`, `service/SystemEventReceiver.kt` | Boot/system events | Used | Med | Mid-High | Receivers |

**Core domain hubs ($I\to0$, high centrality):** `model/*`, `data/INotificationRepository`, `data/INotificationHistoryRepository` — heavily imported, minimal outward deps.  
**Volatile orchestrators/entry points ($I\to1$):** `SmartIslandOverlayService` (AccessibilityService + WindowManager + Compose), `SmartIslandNotificationListenerService` (NotificationListenerService), with heavy side effects and system APIs.

## 2) Heterogeneous Architecture & Meta-Path Graph

### Component layers
- **Entry points**: `MainActivity` (UI/settings), `SmartIslandOverlayService` (overlay), `SmartIslandNotificationListenerService` (listener), `AutostartReceiver` (boot), `SystemEventReceiver` (battery/BT/torch/screen).
- **Domain**: `IslandMode`, `IslandNotification` (with derived flags: `isCallRinging/Dialing/Active`, `isMessaging`, `isDownloadDone`).
- **State/Repos**: `SmartIslandNotificationRepository` holds `notifications: StateFlow<List<IslandNotification>>`, `commands: SharedFlow<SmartIslandCommand>`, `autoExpandEvent`, `resetTimerEvent`. `SmartIslandSettingsRepository` (DataStore). `NotificationHistoryRepository` (SQLite).
- **Presentation**: `IslandViewModel` combines repos/settings/foreground package -> `visibleNotifications` (filters music when app foreground), manages expand/collapse/selection/auto-collapse/input state; `OverlayIsland` + `IslandOverlayView` + `IslandExpandedContent` render by `mode`.
- **System integration**: Notification extraction/filtering/parsing, media session detection, timer/stopwatch/call parsing, navigation/live activity parsing, OEM rules, Shizuku, accessibility + window management, touch-region passthrough via reflection.

### Typed relation graph (conceptual)

```mermaid
graph TD
  NLS[SmartIslandNotificationListenerService] --posts--> NR[SmartIslandNotificationRepository]
  SER[SystemEventReceiver] --posts--> NR
  NR --commands--> NLS
  NR --autoExpand/reset--> VM[IslandViewModel]
  VM --state--> OI[OverlayIsland/IslandOverlayView]
  OS[SmartIslandOverlayService] --hosts--> OI
  OS --uses--> WM[WindowManager]
  OS --observes--> NLS_state[system connected]
  NF[NotificationFilter] --policy--> NLS
  NCE[NotificationContentExtractor] --extracted--> NLS
  TSP[TimerStopwatchParser] --parses--> NLS
  CSP[CallStateParser] --parses--> NLS
  NP[NavigationParser] --parses--> NLS
  LAP[LiveActivityParser] --parses--> NLS
  NHR[NotificationHistoryRepository] --persists--> NLS
  SR[SmartIslandSettingsRepository] --settings--> VM, OS, NLS
  DI[AppModule/Hilt] --provides--> NR,NHR,SR
  SH[ShizukuManager] --grants--> OS,NLS
  OEM[OemDeviceRules] --guides--> NLS,OS
```

Meta-paths of interest: `NLS --(posts via filters/parsers)--> NR --(flows)--> VM --(renders)--> OS(WindowManager ComposeView)`. System events flow through `SER` into `NR`. Commands flow back `VM/NR -> NLS` (e.g. `CancelNotification`, `SeekTo`).

## 3) Data Lifecycle Lattice & State Machine

### Core data structures
- `IslandNotification` (immutable data): identity/key/package/app, content (title/text/bigText/messages), visuals (icon/largeIcon/picture), actions/RemoteInput, media/session, mode/category/progress, `contentIntent`, call flags. Derived: `isCallRinging/Dialing/Active`, `isMessaging`, `isDownloadDone`.
- `IslandMode` enum: `Empty,Notification,IncomingCall,Music,Battery,LiveActivity,Navigation,DownloadUpload,Hotspot,Bluetooth,Flashlight,ScreenRecording,Timer,Stopwatch`.
- `SmartIslandSettings` (large config lattice): layout/physics/colors/behavior/permissions/features/history/profiles-related fields with ranges and JSON serialization (`fromJson`/`toJson`). Defaults in `companion object`.
- `IslandProfile` serializes subset of settings to JSON with UUID.
- History: `NotificationHistoryEntry` + SQLite via `NotificationHistoryDbHelper`/`NotificationHistoryRepository`.

### Notification state machine (repo + VM)
**Repository (`SmartIslandNotificationRepository`)**:
- State: `_notifications: MutableStateFlow<List<IslandNotification>>` (capped `takeLast(MAX_STORED_NOTIFICATIONS)`, default large-ish but bounded).
- Events: `_autoExpandEvent` (SharedFlow), `_resetTimerEvent`, `_commands` (SharedFlow of `SmartIslandCommand`).
- Transitions:
  - `postNotification(n, autoExpand)`: if key exists -> merge (icons/pictures/messages via `mergeChatMessages`), suppress auto-expand for progress/time updates/download/music/screenrecord/timer/stopwatch; only auto-expand on genuine new incoming chat message or title change under conditions. If new -> add (bounded). Emits auto-expand if triggered.
  - `appendUserReply(key, replyText)`: appends user `IslandChatMessage(isFromUser=true)`, updates text/time, trims to last 30.
  - `removeNotification/removeNotificationsForPackage/removeAllNotifications`: update list; for package removals (non-music) emit `CancelNotification` commands.
  - `resetTimer()`, `sendCommand(c)`, `showDemo(mode)`.
Commands: `CancelNotification(key)`, `SeekTo(packageName, positionMs)`.

**ViewModel (`IslandViewModel`)**:
- Derived: `visibleNotifications = combine(notifications, foregroundPackage)` filters out music for foreground app. `mode = combine(visibleNotifications, selectedIndex) -> list[idx]?.mode ?: Empty`.
- UI state: `expanded`, `expandedHeightDp`, `selectedIndex`, `isLocked`, `isInputActive`, `isFullWidth`.
- Behaviors: on new items grows size -> select last; index clamped; auto-expand on `autoExpandEvent` (with optional delayMs); auto-collapse timer (5s) started on expand unless input active, stopped on collapse/input active; `toggleExpanded` debounced (350ms); `dismissCurrent/dismissAll` remove from repo and may send cancel commands (skips messaging packages for cancel); `resetAutoCollapseTimer()`.

### Overlay service lifecycle/state
`SmartIslandOverlayService` (AccessibilityService):
- Holds `windowManager`, `ComposeView` islandView, `OverlayViewTreeOwners`, `IslandViewModel`, repos injected via Hilt entry/accessor pattern; `SystemEventReceiver` registered.
- States/flags: `isLockScreenActive`, `isWindowExpanded`, `isTouchableRegionSupported`, `foregroundStarted`, `destroyed`.
- Touch passthrough: reflection-based `OnComputeInternalInsetsListener` sets touchable region to pill bounds when collapsed, or to expanded card bounds when expanded (non-blocking), empty when hidden. Falls back gracefully if unsupported.
- Window layout: computed from settings/cutout/orientation/lock state; uses `TYPE_ACCESSIBILITY_OVERLAY`, `LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS/SHORT_EDGES`, flags for not-focusable unless input active, optional blur behind on S+ when expanded; updates only on param changes.
- Observes VM: `expanded`, `notifications`, `isInputActive`, `expandedHeightDp` -> adjusts layout/touch region.
- System events: screen on/off/user present update lock state/layout; accessibility events update lock/foreground package; torch callback posts/removes flashlight notification.
- Lifecycle: `onServiceConnected` starts/stops overlay session based on settings; foreground service notification; cleanup on destroy/unbind.

### Notification listener lifecycle/state
`SmartIslandNotificationListenerService` (NotificationListenerService):
- Injected repos/settings/history; `currentSettings` cached via flow.
- Tracks `suppressedKeys` (keys canceled by us to be island-only), `pendingRemovals`, `pendingSuppressionJobs`, icon cache.
- On posted: immediate synchronous attempt to cancel island-only notifications from system shade (if enabled/hideFromNotificationShade and policy allows); group summary handling (cancel redundant summaries if children active, else process standalone). Posts to repo after async processing.
- On removed: distinguishes `REASON_LISTENER_CANCEL` (we canceled) -> preserve island copy; otherwise removes from repo after delay.
- On connected: processes active notifications (suppress island-only from shade, post to repo). On disconnected: requests rebind.
- Auto-cancel/delayed removal for completed states: download/upload complete, screen recording done, timer finished (with delays), incoming call ended.
- History: saves entries (non-music) if enabled, periodic cleanup.

### Fixed-point/termination notes
- Flows are hot (`StateFlow`/`SharedFlow`) with collection in coroutines; no obvious unbounded recursion. Lists bounded (`takeLast(MAX_STORED_NOTIFICATIONS)`, message trims to 30). Jobs cancelled on state changes/destroy. Delays are fixed/timeouts. Touch-region reflection is best-effort. Icon cache has `evictAll()` on destroy. Timer-based auto-collapse/cleanup/retries use fixed delays.

## 4) Program Slices & Symbolic Execution Table

### Critical execution flows (end-to-end)

#### Flow 1: Notification posted -> island display (island-only)
`NLS.onNotificationPosted(sbn)`  
-> group summary handling  
-> immediate `suppressSystemNotification`/`cancelNotification` if island-only eligible  
-> `serviceScope.launch { handleNotificationPosted(sbn, settings) }`  
-> `shouldSuppressFromIsland` check (policy)  
-> extract via `NotificationContentExtractor` (title/text/messages/actions/picture/conversation)  
-> parse mode via `toIslandMode` (+ filters/parsers context), determine call state/timer/stopwatch/music  
-> compute time/flags/progress/media  
-> `notificationRepository.postNotification(n, autoExpand)` (merge/dedupe, auto-expand logic)  
-> history save if enabled (non-music)  
-> sound/play rules; schedule auto-removals if completed  
-> `NR` updates flow -> `VM` observes -> `OS` updates window/layout/touch region -> UI renders.

#### Flow 2: Expand/collapse/touch passthrough
`VM.toggleExpanded()/expand()/collapse()` updates state -> `OS` observes `expanded`/`notifications`/`isInputActive`/`expandedHeightDp` -> `updateWindowLayoutParams` changes size/flags/position/softInput/blur; `setupTouchableRegion` uses reflection to set touchable insets to pill/card/empty. Touches outside region pass through.

#### Flow 3: Dismiss/remove
User dismiss via gestures/UI -> `VM.dismissCurrent/dismissAll` calls `NR.removeNotification/removeAllNotifications()` and may send `SmartIslandCommand.CancelNotification` (skipping messaging). `NLS` command collector calls `forceCancelNotification`. Also `NLS` onNotificationRemoved handles reason (preserves if we canceled).

#### Flow 4: System events (battery/torch/boot)
`SystemEventReceiver` posts battery/BT/torch/screen events to `NR` as system notifications (e.g. flashlight). `AutostartReceiver` on boot/package replace checks settings + Shizuku permission and may auto-grant. `OS` listens to screen state/lock/user present.

### Program slices (key criteria)

**Slicing criteria C1**: `IslandNotification` posted into repo with `mode==Timer` and completion.  
- Backward slice ($S_b$): `TimerStopwatchParser.isTimer/isTimerFinished/parseTimerRemainingSeconds`, `NotificationContentExtractor`, `NotificationFilter.shouldSuppressFromIsland/shouldBeIslandOnly`, `NLS.onNotificationPosted/handleNotificationPosted`, notification extras/actions/category/package.  
- Forward slice ($S_f$): `NR.postNotification` merge/auto-expand rules -> `VM` state -> `OS` layout/auto-collapse -> `NLS` schedules delayed removal on finish -> `NR.removeNotification`.

**Slicing criteria C2**: Island-only suppression of messaging notification.  
- Backward: `NotificationFilter.isMessagingNotification/isMessagingPackage/isAuthenticConversation`, `NLS.shouldBeIslandOnly`, `NLS.suppressSystemNotification` logic.  
- Forward: `cancelNotification` calls (suppressedKeys tracking), `onNotificationRemoved` reason handling preserves island copy; repo state remains.

**Slicing criteria C3**: Touch passthrough when expanded.  
- Backward: `OS.setupTouchableRegion` reflection, `VM.expanded/notifications`, `settings` (split/pinch/notch), `updateWindowLayoutParams`.  
- Forward: WindowManager insets affect touch dispatch (system passes touches outside region through).

### Symbolic execution table (top paths)

| Execution Path | Accumulated Path Constraint ($\pi$) | Symbolic State Transformation ($\sigma \to \sigma'$) | Terminal Outcome / Side Effect |
| --- | --- | --- | --- |
| `NLS.post` -> island-only cancel | $settings.enabled \land settings.hideFromNotificationShade \land \lnot isMessaging \land \lnot shouldSuppressFromIsland \land shouldBeIslandOnly(mode, pkg)$ | $sbn \in SystemShade \to$ marked `suppressedKeys[key]`, `cancelNotification(key)` called; island copy not yet posted or posted async | Notification removed from system shade; appears only in island if posted to repo. |
| `NLS.post` -> messaging allowed in shade | $isMessagingNotification \lor mode \in \{Music,Navigation,Timer,Stopwatch\}$ (and not forced island-only) | No immediate cancel by island-only rule; posted to repo per normal flow | Appears in island (peek) and remains in system shade. |
| `NR.post` new non-progress -> auto-expand | $key \notin existing \land autoExpand \land \lnot isDownloadOrProgress \land \lnot isProgressOrTimeRemainingUpdate(text)$ | `notifications$ += n$`; `_autoExpandEvent.tryEmit(key)` | VM receives event -> expands after `animationDelayMs`. |
| `NR.post` existing progress update | $key \in existing \land (progress/time-update/download/music/timer/stopwatch)$ | Merge icons/pictures/messages only; no auto-expand emitted | State updated silently, no auto-expand. |
| `VM.expand` with input inactive | $expanded:false \to true \land \lnot isInputActive$ | Start `autoCollapseJob` (delay 5000ms) -> calls `collapse()` on timeout | Auto-collapses after inactivity. |
| `VM.expand` with input active | $expanded:false \to true \land isInputActive$ | No auto-collapse timer started | Stays expanded until input deactivated/collapsed manually. |
| `OS.touchRegion` expanded | $expanded \land isTouchableRegionSupported$ | Set insets region to expanded card bounds only | Touches outside card pass through to underlying apps/system. |
| `NLS.removed` by us | $reason == REASON_LISTENER_CANCEL \lor key \in suppressedKeys$ | Do not remove from `NR.notifications` | Island copy preserved. |
| `NLS.removed` by user/app | $reason \neq REASON_LISTENER_CANCEL \land key \notin suppressedKeys$ | After delay remove from `NR.notifications` | Removed from island. |
| Timer finish | `TimerStopwatchParser.isTimerFinished` true | Schedule delayed removal (e.g. 5000ms) after clearing suppression | Auto-removed from island/shade state. |

## 5) Hoare Logic Contracts & Vulnerability/Invariant Audit

### Formal contracts ({P} C {Q}), weakest preconditions (wp)

#### C1: `SmartIslandNotificationRepository.postNotification(n, autoExpand)`
- **Precondition P**: `n != null`, `n.key` is string id, `n.packageName/appName/title` non-null (model), flows are active.
- **Command**: merge-or-add into bounded list with auto-expand rules.
- **Postcondition Q**: `notifications.value` contains `n` (merged) at end; list size <= `MAX_STORED_NOTIFICATIONS`; if auto-expand triggered then `autoExpandEvent` emitted for `n.key`; no auto-expand on progress/time/download/music/timer/stopwatch updates.
- **wp audit**: Callers (NLS, SystemEventReceiver) must ensure they don't rely on auto-expand for progress updates. Repo enforces this. If caller passes `autoExpand=true` for progress, repo still suppresses emission — correct. Potential gap: if `isProgressOrTimeRemainingUpdate` misses new phrases, could incorrectly auto-expand (mitigated by mode checks too).

#### C2: `SmartIslandNotificationListenerService.shouldBeIslandOnly(notification, mode, pkg)`
- **Precondition P**: valid notification/mode/pkg.
- **Command**: policy decision.
- **Postcondition Q**: returns `false` if `mode==Music||Navigation||Timer||Stopwatch` or `isMessagingNotification` true; returns `false` if incoming call not ringing/active in certain states? (checks `isIncomingCall` for IncomingCall mode -> returns false if not incoming call); else returns `true`.
- **wp audit**: Must hold that messaging apps never become island-only. Correctly checks `NotificationFilter.isMessagingNotification`. Timer/stopwatch/music/navigation explicitly excluded from island-only. Call handling: only treats as island-only in incoming ringing/answerable states? Logic says if IncomingCall mode and not incoming call -> return false (keeps in system). Correct for ongoing calls.

#### C3: `SmartIslandNotificationListenerService.suppressSystemNotification(key)`
- **Precondition P**: `settings.enabled && settings.hideFromNotificationShade`, key exists or may exist.
- **Command**: mark suppressed, cancel via `cancelNotification`, retry up to 3 times with delays.
- **Postcondition Q**: notification removed from system shade if successful; `suppressedKeys` contains key (timestamp). On removal, if reason is listener-cancel -> island copy preserved.
- **wp audit**: Only uses `cancelNotification` (not `snoozeNotification`) — correct per comments. Retries mitigate timing issues. Gap: some OEMs may ignore `cancelNotification` or have different behavior; code has fallbacks/retries and `SystemServiceRecovery`. Also checks disabled packages.

#### C4: `SmartIslandOverlayService.updateWindowLayoutParams(expanded, settings)`
- **Precondition P**: service initialized, windowManager/view/viewModel exist, not destroyed.
- **Command**: compute flags/size/position based on state (lock/landscape/hide), update view layout if changed.
- **Postcondition Q**: window params reflect current state; visibility set correctly; flags match focus/input/blur needs; only updates if params changed (optimization).
- **wp audit**: Touch passthrough depends on `isTouchableRegionSupported` and reflection. If reflection fails, falls back to different sizing (still tries to be non-blocking where possible). Layout in display cutout modes set for API levels. Potential issue: `FLAG_NOT_FOCUSABLE` removed when input active for keyboard — correct.

#### C5: `IslandViewModel.visibleNotifications` filtering
- **Precondition P**: `notifications` and `foregroundPackage` flows valid.
- **Command**: filter out music notifications where `notif.packageName == fgPkg`.
- **Postcondition Q**: music hidden when app is in foreground; other modes unaffected.
- **wp audit**: Prevents duplicate island music when music app is open. Correct.

### Invariant violations & hazard report

| Category | Issue/Risk | Location | Severity | Notes/Recommendation |
| --- | --- | --- | --- | --- |
| Touch passthrough | Reflection on hidden APIs (`OnComputeInternalInsetsListener`, `InternalInsetsInfo`) | `SmartIslandOverlayService.setupTouchableRegion` | Medium | May break on some OEMs/Android versions; guarded by `runCatchingLogged` with fallback. Test coverage needed across devices. |
| Notification suppression | Reliance on `cancelNotification` from listener; some OEMs restrict | `NLS.suppressSystemNotification` | Medium | Retries + disabled package checks + recovery exist. Messaging/timer/music/navigation correctly excluded from island-only. |
| Group summaries | Complex handling to cancel redundant summaries vs process standalone | `NLS.onNotificationPosted` group summary block | Medium | Logic tries to detect active children; edge cases possible if timing differs. Needs testing with WhatsApp/Group apps. |
| Auto-expand logic | Heuristic based on text/progress/mode | `NR.postNotification` | Low-Med | Could misclassify unusual notifications; mitigated by multiple checks. `isProgressOrTimeRemainingUpdate` is keyword-based. |
| Message merging | Deduplication logic for chat messages | `NR.mergeChatMessages` | Low | Handles echoes/replies with time windows; could miss edge cases with identical messages far apart. |
| Window focus/input | Keyboard focus switching for inline reply | `OS` + `VM.isInputActive` | Low-Med | Correctly sets flags/softInput when input active; relies on proper focus handling in expanded UI. |
| History storage | SQLite writes from listener service | `NLS` + `NotificationHistoryRepository` | Low | Non-blocking via coroutines; cleanup periodic. Only saves non-music if enabled. |
| Memory/leaks | Caches (icon cache), jobs, receivers | Multiple services/repos | Low | `evictAll()` on destroy, jobs cancelled, receivers unregistered. Compose view tree owners managed. |
| OEM compatibility | Heavy OEM-specific logic | `OemDeviceRules`, autostart | Medium | Broad coverage but OEMs change behavior; Shizuku helps on some devices. |
| Permissions | Many privileged perms (overlay, accessibility, notification listener, usage stats, Shizuku) | Manifest + setup | Info | Expected for this functionality; privacy-first design stated. |

### Invariants (must hold)
- Messaging notifications are never made island-only (`shouldBeIslandOnly` + `NotificationFilter.isMessagingNotification`).
- Timer/stopwatch/music/navigation remain in system shade (not island-only).
- We only use `cancelNotification` for island-only suppression (no snooze).
- If we cancel a notification ourselves, island copy is preserved on removal.
- Lists are bounded (repo caps, message trims).
- Touch passthrough only when supported; falls back safely.
- Auto-expand never triggers for progress/time/download/music/timer/stopwatch updates.

## Handoff for next agents

### Immediate priorities
1. **Touch passthrough robustness**: Test reflection on target OEMs; add more defensive checks/logging; consider alternative approaches if broken.
2. **Group summary edge cases**: Verify WhatsApp/group message behavior across Android versions.
3. **Auto-expand false positives**: Audit `isProgressOrTimeRemainingUpdate` keywords against real notifications.
4. **OEM autostart/restrictions**: Validate on Xiaomi/HyperOS, Samsung, OnePlus/Oppo/Realme, Vivo, Huawei.

### Files to read first for changes
- `service/SmartIslandNotificationListenerService.kt` (suppression + parsing + lifecycle)
- `service/SmartIslandOverlayService.kt` (window + touch + lifecycle)
- `data/SmartIslandNotificationRepository.kt` (state/merge/auto-expand)
- `ui/IslandViewModel.kt` (state machine)
- `util/NotificationFilter.kt` + `util/NotificationContentExtractor.kt` (policy/extraction)
- `util/TimerStopwatchParser.kt` + `util/CallStateParser.kt` (parsers)

### Build/verification
- Build: `./gradlew assembleDebug` (or `assembleRelease` with signing env vars)
- Lint: `./gradlew lint` (abortOnError=false per config)
- Tests: `./gradlew test` / instrumented tests present
- CI: `.github/workflows/android.yml`, `auto-review.yml`

### Key contracts to preserve
- Island-only only for non-messaging, non-music/nav/timer/stopwatch notifications
- Cancel via listener reason tracking preserves island copy
- No auto-expand on progress/time updates
- Touch passthrough best-effort with safe fallback
- Bounded state (lists/messages)

### Next steps
- Map full `IslandExpandedContent` dispatcher and each expanded renderer for UI details
- Trace `NotificationContentExtractor` and `toIslandMode` extension implementations fully
- Analyze `SmartIslandSettingsRepository` DataStore schema/migrations
- Deep dive into media session handling and lyrics service
- Create unit tests for critical slices (suppression policy, auto-expand rules, timer parsing)

## Build & Footprint Verification (environment setup)

**Build environment (installed for verification):**
- Android SDK at `/opt/android-sdk` (cmdline-tools, platform-tools, platform android-36, build-tools 36.0.0)
- JDK 21 (OpenJDK), Gradle 9.7.1 (wrapper)
- `ANDROID_SDK_ROOT=/opt/android-sdk JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`

**Release build (`assembleRelease`) result:**
- `minifyEnabled=true`, `shrinkResources=true`, R8 (`proguard-android-optimize.txt`) → all succeeded.
- Kotlin/Hilt (`kaptReleaseKotlin`, `compileReleaseKotlin`, `hiltJavaCompileRelease`) → clean compile; only deprecation/type-mismatch warnings (pre-existing, no feature impact).
- `minifyReleaseWithR8` → **succeeded** (ProGuard keep rules verified working; no missing-class errors).
- `lintVitalRelease` → **passed** (`abortOnError=false` but no errors emitted).
- APK packaging: **BUILD SUCCESSFUL** (exit code 0, 13m 47s, 57 tasks: 10 executed / 47 up-to-date). Unsigned release APK produced at `app/build/outputs/apk/release/app-release-unsigned.apk` (**7.9 MB**). Signing skipped since no release keystore env vars set (`SIGNING_STORE_*`).

**No feature, language, icon, or permission removed.** Assets (`logo.png` + 16 screenshots, ~7 MB) are not referenced in code and are not bundled into the APK. All 32 `res/` files are referenced; manifest components are auto-kept by R8/AAPT2. Icons (`Icons.Rounded.*` from `material-icons-extended`) are tree-shaken to used glyphs only.

**Verification commands:**
- `./gradlew assembleRelease` — build minified release APK
- `./gradlew lint` — run lint
- `./gradlew test` — run unit tests

**Build log captured at:** `/tmp/disland_build.log`

**Note:** Release APK is unsigned (no `SIGNING_STORE_*` env vars). For Play Store publishing, set `SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD` and re-run `assembleRelease`.
