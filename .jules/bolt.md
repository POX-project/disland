## 2024-03-24 - Jetpack Compose Reallocation with SimpleDateFormat
**Learning:** `SimpleDateFormat` is not thread-safe and requires a new allocation every time it is used. When instantiated inside Jetpack Compose composables, this creates significant reallocation overhead and potential concurrency bugs during fast recompositions on multiple threads in parallel.
**Action:** Replace `SimpleDateFormat` with thread-safe alternatives like `java.time.format.DateTimeFormatter` initialized at the file level (static scope) for Jetpack Compose components.
