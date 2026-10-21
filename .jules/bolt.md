## 2026-06-25 - Prevented Jetpack Compose unnecessary recomposition

**Learning:**
In `IslandOverlayView.kt`, reading animated state properties directly within compose functions triggered full recompositions at 60fps, severely degrading performance. Jetpack Compose requires explicitly defining derived states to observe values over time without triggering large-scale recompositions inside layout modifiers.

**Action:**
Use `derivedStateOf` to wrap state evaluations that frequently update (like `collapsedAlpha > 0f` and `expandedAlpha > 0.01f`). Use these derived values in standard compose branching logic (like `if` statements) to isolate and minimize recomposition to only when the derived state actually changes (e.g. crossing the `0f` or `0.01f` threshold).
