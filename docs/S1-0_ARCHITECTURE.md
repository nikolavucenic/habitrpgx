# S1-0 Skeleton (Student 1)

## 1) Multi-module Gradle setup
- Modules: `:app`, `:domain`, `:data`.
- `domain` intentionally has no AndroidX runtime dependencies.
- `data` includes Room (SQLite), Firebase Auth/Firestore and Hilt.
- `app` depends on `domain` and `data` and contains the `@HiltAndroidApp` application class.

## 2) Package structure proposal

### app module
- `com.example.habitrpg`
  - `presentation.common`
  - `presentation.account`
  - `presentation.progression`
  - `presentation.inventory`
  - `presentation.alliance`
  - `navigation`
  - `di`

### domain module
- `com.example.domain`
  - `model`
  - `repository`
  - `usecase`
  - `core`

### data module
- `com.example.data`
  - `di`
  - `local.db`
  - `local.prefs`
  - `remote.firebase`
  - `repository`
  - `mapper`

## 3) Minimal DI setup
- `MyApplication` is annotated with `@HiltAndroidApp`.
- `RepositoryModule` binds `AuthRepository` and `SettingsRepository`.
- `StorageModule` provides Room DB/DAO and Firebase singletons.

## 4) SQLite setup rationale
- Room is used as a typed, compile-time validated abstraction over SQLite.
- It still stores data in SQLite while reducing boilerplate and migration risk.
- Minimal table: `user_cache` (`UserCacheEntity`) and DAO (`UserCacheDao`).

## 5) Firebase setup
- `StorageModule` provides `FirebaseAuth` and `FirebaseFirestore`.
- `FirebaseService` is a thin wrapper exposing these SDK clients.

## 6) SharedPreferences setup
- `AppPreferences` wraps app settings only (no session handling):
  - `dark_theme_enabled`
  - `notifications_enabled`
  - `shake_enabled`
