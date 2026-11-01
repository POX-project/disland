
## 2025-05-18 - Avoid SimpleDateFormat in Jetpack Compose
**Learning:** `SimpleDateFormat` is not thread-safe and creates unnecessary allocations when used inside Composable functions where it might be called on multiple threads simultaneously and repeatedly during recomposition. The Android `minSdk` is 26, so modern `java.time` APIs are natively supported.
**Action:** Replace `SimpleDateFormat` instances inside Composables and helper functions with thread-safe `java.time.format.DateTimeFormatter`. Cache these formatters at the file level to avoid reallocation.
