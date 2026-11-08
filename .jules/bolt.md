## 2026-11-08 - Replaced SimpleDateFormat and Calendar with java.time APIs
**Learning:** Legacy java.util.Calendar and SimpleDateFormat cause significant allocation overhead, and SimpleDateFormat is not thread-safe, making it risky in parallel Compose recompositions.
**Action:** Used java.time.format.DateTimeFormatter cached at file level (with dynamic locale/zone to support runtime changes) and java.time.LocalDate for grouping logic to improve performance and thread safety.
