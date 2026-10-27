
## 2023-10-27 - Thread-safety in Jetpack Compose Recomposition
**Learning:** Using `SimpleDateFormat` inside Composables is an anti-pattern. Jetpack Compose executes recompositions on multiple threads in parallel. `SimpleDateFormat` is not thread-safe and can cause concurrency bugs. Furthermore, creating formatters inside a Composable causes unnecessary reallocation overhead.
**Action:** Always prefer caching thread-safe alternatives like `java.time.format.DateTimeFormatter` at the file level to eliminate reallocation overhead and prevent concurrency bugs.
