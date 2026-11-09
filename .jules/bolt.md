## 2026-11-09 - DateTimeFormatter over SimpleDateFormat in Compose
**Learning:** Jetpack Compose executes recompositions on multiple threads in parallel. `SimpleDateFormat` is not thread-safe and instantiating it frequently inside composables adds unnecessary reallocation overhead.
**Action:** Use thread-safe `java.time.format.DateTimeFormatter`, caching pattern parsing at the file level (`DateTimeFormatter.ofPattern(...)`), applying `.withZone()` and `.withLocale()` at the call site.
