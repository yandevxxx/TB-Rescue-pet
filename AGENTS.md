# PawBuddy — AGENTS.md

## Build

Requires `JAVA_HOME` pointing to a full JDK (not JRE from VS Code).  
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"; ./gradlew installDebug
```

Versions: Gradle 9.2.1 · AGP 9.0.1 · Kotlin 2.0.21 · Compose BOM 2024.09.00  
Pinned in `gradle/libs.versions.toml`.

## Setup

5 Appwrite fields **required** in `local.properties` (gitignored); build fails if missing:
```
appwrite.endpoint=https://cloud.appwrite.io/v1
appwrite.project.id=xxx
appwrite.database.id=xxx
appwrite.collection.animals=xxx
appwrite.bucket.id=xxx
```
Values injected as `BuildConfig` fields → `Constants` object.

## App Flow

```
LoginActivity (launcher)
  ├─ checks existing session (AuthRepository.getCurrentUser)
  │   └─ has session → MainActivity
  └─ login form → AuthRepository.login (Appwrite Email/Password)
RegisterActivity → AuthRepository.register (create user + session)
MainActivity (dashboard)
  ├─ animal list (paginated, cursor-based, 20/page)
  ├─ real-time updates via Appwrite Realtime channel
  ├─ type/chip filter + category filter
  ├─ infinite scroll (loadMore)
  ├─ FAB → PostAnimalActivity
  ├─ search → SearchActivity
  └─ logout → AuthRepository.logout → LoginActivity
PostAnimalActivity → upload image to Storage → create doc in Databases
SearchActivity → GPS permission → FusedLocationProviderClient → searchNearby (10km radius)
AnimalDetailActivity → detail view → owner can updateStatus / deleteAnimal
```

## Architecture

- **Single module `:app`**, package `com.yarsi.rescuepet`
- **MVVM + Repository**: ViewModel → Repository → Appwrite SDK
- **100% Jetpack Compose** — no XML/Fragments
- **Appwrite BaaS** (Account, Databases, Storage, Realtime) — no local DB
- **Sealed `Result<T>`** (`Success | Error | Loading`) across all ViewModels/repos
- **`LiveData`** observed via `observeAsState()`
- **`AppwriteClient`** singleton: `initialize(context)` in `AppwriteApplication.onCreate`, then `getInstance()`
- **UI strings in Indonesian (Bahasa)**

## Key Directories

| Path | Role |
|---|---|
| `data/repository/` | `AnimalRepository`, `AuthRepository`, `StorageRepository` |
| `data/remote/` | `AppwriteClient` (Appwrite SDK wrapper) |
| `data/model/` | `Animal`, `UserData` data classes |
| `utils/` | `Result<T>`, `ErrorMapper`, `Constants` |
| `ui/auth/` | `LoginActivity`, `RegisterActivity`, `AuthViewModel` |
| `ui/home/` | `MainActivity`, `HomeViewModel` |
| `ui/detail/` | `AnimalDetailActivity`, `DetailViewModel` |
| `ui/post/` | `PostAnimalActivity`, `PostViewModel` |
| `ui/search/` | `SearchActivity`, `SearchViewModel`, `AnimalWithDistance` |
| `ui/theme/` | `Color.kt`, `Theme.kt`, `Type.kt` — terracotta/sage/ocean palette |
| `stitch_remix_of_rescuepet_mobile_app_design/` | M3 design spec & screenshots |

## Animal Model

`type`: free text (e.g. "Kucing", "Anjing")  
`category`: `"adoption"` | `"hilang"` | `"ditemukan"`  
`status`: `"available"` (default) — owner can update  
`imageId`: Appwrite Storage file ID; URL built via `StorageRepository.getImageUrl()`  
`latitude`/`longitude`: GPS coords for nearby search

## Key Libraries

- **Appwrite SDK** `io.appwrite:sdk-for-android:6.0.0`
- **Coil** `io.coil-kt:coil-compose:2.6.0` — `AsyncImage` for Storage images
- **Google Play Services Location** `21.1.0` — `FusedLocationProviderClient`
- **material-icons-extended** (via BOM) — includes `Icons.Default.Pets`
- **runtime-livedata** — `observeAsState()` bridge

## Nearby Search

Bounding box (`Query.greaterThan`/`lessThan` on lat/lon) in `AnimalRepository.searchNearby` (10km default), then Haversine distance filtering in `SearchViewModel.filterByDistance`.

## Testing

Only trivial example tests (`ExampleUnitTest`, `ExampleInstrumentedTest`). No CI workflows.
