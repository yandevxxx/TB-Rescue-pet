# RescuePet — AGENTS.md

## Build & Run

```powershell
./gradlew assembleDebug          # Build debug APK
./gradlew test                   # Unit tests (ExampleUnitTest only)
./gradlew connectedAndroidTest   # Instrumented tests (needs emulator/device)
```

Gradle 9.2.1 · AGP 9.0.1 · Kotlin 2.0.21 · Compose BOM 2024.09.00

## Setup Required

5 Appwrite fields **must** be in `local.properties` (file is gitignored):

```
appwrite.endpoint=https://cloud.appwrite.io/v1
appwrite.project.id=xxx
appwrite.database.id=xxx
appwrite.collection.animals=xxx
appwrite.bucket.id=xxx
```

Build fails with a clear Gradle exception if any are missing.

## Architecture

- **Single module** `:app` (settings.gradle.kts only includes `:app`)
- **MVVM + Repository**: each screen has a ViewModel; data access through `AnimalRepository`, `AuthRepository`, `StorageRepository`
- **100% Jetpack Compose** — no XML layouts, no Fragments
- **Appwrite BaaS** — no local database; all data via Appwrite Databases, Storage, Account, Realtime
- **Sealed `Result<T>`** (`Success | Error | Loading`) used consistently across all ViewModels and repos
- **LiveData** observed via `observeAsState()` in composables
- **BuildConfig** for Appwrite config (not `BuildConfig` fields)
- **Indonesian (Bahasa)** for all UI strings, error messages, tags

## Entry Points

| Activity | Role |
|---|---|
| `LoginActivity` | **Launcher** (checks existing session → navigates to MainActivity) |
| `RegisterActivity` | Registration form |
| `MainActivity` | Dashboard / home feed |
| `PostAnimalActivity` | Post new animal form |
| `SearchActivity` | Nearby animals (GPS-based) |
| `AnimalDetailActivity` | Detail view + owner controls |

## Key Libraries

- **Coil** (`coil-compose:2.6.0`) — `AsyncImage` for loading images from Appwrite Storage
- **Google Play Services Location** (21.1.0) — `FusedLocationProviderClient` for GPS
- **Appwrite SDK** (`io.appwrite:sdk-for-android:6.0.0`)
- **material-icons-extended** — for iconography
- **runtime-livedata** — `observeAsState()` bridge

## UI-stitch

`ui-stitch/` contains HTML/CSS prototypes and screenshots per feature. `rescuepet/DESIGN.md` is the active design system spec (terracotta/sage/ocean palette, Inter font). `pawbuddy/DESIGN.md` is an earlier/alternative brand — do not use.

## Permissions (Manifest)

`INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `CAMERA` (optional hardware feature).

## Testing

Only trivial example tests exist (`ExampleUnitTest`, `ExampleInstrumentedTest`). No integration/UI test suites.

## CI

None — no `.github/` workflows, no pre-commit hooks, no lint/typecheck config beyond default Android Lint.
