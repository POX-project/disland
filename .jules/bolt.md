
## 2024-05-14 - WavyMusicSeekBar Rendering Bottleneck
**Learning:** Continuous Canvas drawing with `sin` and `cos` functions evaluated per pixel per frame (60+ FPS) is a major CPU drain in Jetpack Compose, especially for animated music visualizers spanning the screen width.
**Action:** Always step through paths in chunks (e.g. `step 4` pixels) instead of iterating every single pixel. This reduces expensive trigonometric calls dramatically while maintaining visual fidelity, as long as the exact final endpoint is manually drawn to prevent gaps.
