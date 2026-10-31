## 2024-05-23 - Avoid SimpleDateFormat in Jetpack Compose
**Learning:** Jetpack Compose recomposes on multiple threads. `SimpleDateFormat` is not thread-safe and instantiating it inside recomposition loops creates allocation overhead. The app uses `minSdk = 26`, so modern `java.time` APIs are available without desugaring.
**Action:** Always prefer caching `java.time.format.DateTimeFormatter` at the file level over creating `SimpleDateFormat` inside composables or utility functions.
