## 2024-05-18 - Avoid SimpleDateFormat in Jetpack Compose
**Learning:** `SimpleDateFormat` is not thread-safe and can cause reallocation overhead or concurrency bugs when used inside Composables, which run on multiple threads in parallel.
**Action:** Prefer caching thread-safe alternatives like `java.time.format.DateTimeFormatter` at the file level. Apply `.withZone()` and `.withLocale()` dynamically at the format call site to correctly respect system timezone and locale changes.
