## 2024-05-18 - Avoid SimpleDateFormat in Jetpack Compose
**Learning:** Jetpack Compose executes recompositions on multiple threads in parallel. `SimpleDateFormat` is not thread-safe and instantiating it inside composable `remember` blocks introduces unnecessary reallocation overhead. This codebase has minSdk=26.
**Action:** Use the modern, thread-safe `java.time.format.DateTimeFormatter` instead. Define it at the file-level to share instances, eliminating concurrency issues and reallocation overhead during rapid recompositions.

## 2024-05-18 - Dynamic ZoneId/Locale with Cached DateTimeFormatter
**Learning:** While `DateTimeFormatter` is thread-safe and parsing the pattern at the file-level avoids reallocation overhead, statically chaining `.withZone(ZoneId.systemDefault())` caches the system timezone at class load time. This prevents the app from reflecting runtime timezone/locale changes unless the process is killed.
**Action:** Extract only the pattern parsing (`DateTimeFormatter.ofPattern(...)`) to a static variable. Chain `.withZone(ZoneId.systemDefault()).withLocale(Locale.getDefault())` dynamically at the call site so system settings updates are correctly respected during recomposition.
