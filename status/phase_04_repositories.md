# Phase 4: Repository Implementations

## Status: COMPLETE

## Checklist of Deliverables

- [x] Create bidirectional local database mappers in `/data/src/commonMain/kotlin/com/example/data/local/mapper/LocalMappers.kt` mapping:
  - `PredictionRecord` ↔ `Prediction` (handles options serialization via `kotlinx.serialization`)
  - `SecondRecord` ↔ `Second` (handles instant mapping to epoch milliseconds)
  - `CommentRecord` ↔ `Comment` (validates flat structure with no parenting)
  - `DiscourseRecord` ↔ `DiscourseEntry` (preserves threading `parentId`)
  - `KeepTabRecord` ↔ `KeepTab`
  - `SubmissionRecord` ↔ `Submission` (handles outcome string list serialization)
  - `SecondingSnapshot` ↔ `SecondingSnapshotRecord` + `SecondingSnapshotOutcomeRecord`
- [x] Create platform-agnostic helper `PlatformSettings.kt` inside `/data/src/commonMain/kotlin/com/example/data/local/` for lightweight, target-agnostic preference and session storage with in-memory fallback for unit testing.
- [x] Create 9 reactive Repository Implementations inside `/data/src/commonMain/kotlin/com/example/data/repository/`:
  - **PredictionRepositoryImpl**: Implements `PredictionRepository`. Observes all predictions or singular prediction flows from `PredictionDao`, maps records to domain models, and syncs remote prediction DTOs to local database records.
  - **SecondRepositoryImpl**: Implements `SecondRepository`. Observes seconds, maps local records, and executes optimistic casts to the local database before transmitting DTOs via API.
  - **CommentRepositoryImpl**: Implements `CommentRepository`. Exposes a flat list of comments per prediction, writing to the DAO and pushing to the remote API.
  - **DiscourseRepositoryImpl**: Implements `DiscourseRepository`. Supports threaded discourse flows, entry creation, helpful count tracking, and flag escalations to both the DAO and remote endpoints.
  - **SecondingSnapshotRepositoryImpl**: Implements `SecondingSnapshotRepository`. Combines snapshot records and outcome lists seamlessly, filters snapshot logs by `TimeRange` (Day/Week/All), groups downsampled outcome buckets, and calls downsampling rollups. Implements no destructive delete methods.
  - **SubmissionRepositoryImpl**: Implements `SubmissionRepository`. Directs submissions to local record databases as local-only, pushing Map payloads to the remote API in a fire-and-forget fashion, and updates submission statuses.
  - **AuthRepositoryImpl**: Implements `AuthRepository`. Manages user sessions dynamically by tracking the active user record inside `UserDao` reactively, saving profile tokens inside `PlatformSettings`, and syncing profile adjustments.
  - **PreferencesRepositoryImpl**: Implements `PreferencesRepository`. Provides dynamic, reactive access to app settings (e.g. `darkTheme`) leveraging a StateFlow initialized with platform preferences.
  - **KeepTabRepositoryImpl**: Implements `KeepTabRepository`. Utilizes optimistic local actions for tracking and untracking predictors. Syncing executes reconciliation with a server-wins strategy.

## KMP Pre-Flight Verification

- [x] `gradle :data:build` executes and compiles successfully for all multiplatform targets
- [x] `gradle :data:test` executes successfully and all tests pass perfectly
- [x] `compile_applet` succeeds
- [x] No target-specific references exist inside `commonMain` of the repository layer, maintaining complete decoupling from physical platforms

## Constraints Applied

- **[IMMUTABLE]**: Casting a second immediately commits an immutable transaction locally and emits reactive UI results before executing remote synchronization.
- **[OFFLINE-SAME]**: KeepTab toggle logic executes identical local mutations regardless of network status. Unsynced changes are flagged (e.g. `syncedAt = null`) and reconciled on the next active sync.
- **[NO-DELETE]**: Snapshot records contain absolutely no deletion logic, adhering strictly to audit-ready append-only structures.
- **[RATE-LIMIT]**: Submission and repository operations gracefully escalate standard network exceptions (such as 429 Too Many Requests) through `Result` wrappers.

## Reusable Components

- **Bidirectional Local Mappers (`LocalMappers`)**: Uniform mappings that cleanly isolate Room Entity structures from Clean Architecture Domain Models.
- **Platform Settings Bridge (`PlatformSettings`)**: Lightweight, compile-safe platform preferences bridge that ensures 100% test compatibility and compile stability across KMP targets.

## Verification Notes

- Run `gradle :data:test` to verify database, mappers, and repository behaviors. All suites compiled and executed successfully.

## Cross-Cutting Decisions

- **Reactive State Flow in Auth**: The auth repository maps active user status reactively to the actual `UserDao` observer, meaning the active session model updates automatically whenever the database record changes.
- **Server-Wins in Sync**: KeepTab sync triggers best-effort local pushes for pending actions, then replaces local logs with the remote authoritative list, adhering to server-wins rules.
