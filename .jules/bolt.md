## 2024-05-18 - Avoid SimpleDateFormat in Compose
**Learning:** SimpleDateFormat is not thread-safe and instantiating it frequently inside Compose functions or loops causes unnecessary allocation and potential concurrency issues since Compose can run on multiple threads.
**Action:** Use thread-safe java.time.format.DateTimeFormatter cached at the file level.
