
## 2024-05-17 - Compose Recomposition Formatting Overhead
**Learning:** Instantiating `SimpleDateFormat` inside Jetpack Compose UI code (like `NotificationHistorySection.kt`) causes repeated allocations on every recomposition. Because Compose is multithreaded and `SimpleDateFormat` is not thread-safe, it cannot be safely shared.
**Action:** Always replace `SimpleDateFormat` with Java 8's `DateTimeFormatter`. Cache `DateTimeFormatter` at the file level but apply `.withLocale()` and `.withZone()` dynamically at the `.format()` call site to ensure the app reacts to system settings changes without process restart.
