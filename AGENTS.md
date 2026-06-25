# Project Knowledge

## Build Commands
- Build: `.\gradlew.bat assembleDebug`
- Clean: `.\gradlew.bat clean`

## Compose BOM & Version Issues
- BOM `2024.09.00` pins Foundation to 1.7.0 on compile classpath
- Runtime overrides Foundation to 1.9.2 (via transitive deps), but compile uses 1.7.0
- `Modifier.animateItem()` / `animateItemPlacement()` **not available** at compile time in Foundation 1.7.0 — do NOT use
- `@OptIn(ExperimentalFoundationApi::class)` can be safely removed if not used

## Architecture
- MVVM with `LiveData` + Coroutines
- Navigation via explicit `Intent` (no Navigation Component)
- Appwrite SDK for backend (DB, Storage, Auth)
- Reverse geocoding via Nominatim API (no Google Play Services)
- Compose BOM 2024.09.00, Material3 1.3+, Kotlin 2.0.21
