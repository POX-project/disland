
## 2024-05-24 - SimpleDateFormat Allocation in Compose
**Learning:** `SimpleDateFormat` is non-thread-safe and expensive to instantiate. Using it inside Compose functions (even within `remember`) or loops leads to unnecessary allocations.
**Action:** Always replace `SimpleDateFormat` and `Date` with cached, file-level, thread-safe `java.time.format.DateTimeFormatter` and `Instant`/`ZonedDateTime`.
