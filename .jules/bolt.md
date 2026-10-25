## 2026-10-25 - Date Formatting Optimization
**Learning:** In Android/Kotlin, `SimpleDateFormat` is known to be relatively expensive to instantiate, especially in compose functions where it might be recreated often.
**Action:** Lift `SimpleDateFormat` instances outside of loops or hot paths, or use `DateTimeFormatter` from `java.time` if targeting API 26+, or at least cache them to avoid frequent instantiations.
