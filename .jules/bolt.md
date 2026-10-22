## 2024-05-18 - Avoid repeated list allocations in NotificationFilter
**Learning:** `NotificationFilter` functions like `toIslandMode` run for almost every incoming notification and create many lists over and over via `listOf(...)` inside the method calls for keyword matching.
**Action:** Extract these hardcoded `listOf(...)` calls into companion object/file-level `private val` properties to save object allocation overhead on each notification.
