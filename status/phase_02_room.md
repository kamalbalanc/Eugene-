# Phase 2: Room Database Integration

## Status: COMPLETE

## Checklist of Deliverables

- [x] Create Room entity records in `/data/src/commonMain/kotlin/com/example/data/database/entity/`:
  - `PredictionRecord` (no timelineJson/seriesJson)
  - `SecondRecord`
  - `CommentRecord` (no `parentId` column, Reasoning is flat)
  - `DiscourseRecord` (has `parentId` column, Discourse is threaded)
  - `SecondingSnapshotRecord` (primary keys: `predictionId`, `timestamp`)
  - `SecondingSnapshotOutcomeRecord` (foreign key to snapshot record, no `onDelete`)
  - `SecondingSnapshotDownsampledRecord` (primary keys: `predictionId`, `bucketStart`, `outcomeId`, `granularity`)
  - `SubmissionRecord`
  - `UserRecord` (with `reputation: Int`)
  - `KeepTabRecord` (with `syncedAt: Long?`)
  - `AppealSubmissionRecord`
- [x] Create 9 Room Data Access Objects (DAOs) in `/data/src/commonMain/kotlin/com/example/data/database/dao/`:
  - `PredictionDao`
  - `SecondDao`
  - `CommentDao`
  - `DiscourseDao`
  - `SecondingSnapshotDao` (append-only, no delete methods)
  - `SubmissionDao`
  - `UserDao`
  - `KeepTabDao` (includes `@Transaction` method `reconcile` for offline first sync)
  - `AppealSubmissionDao`
- [x] Implement standard type converters in `/data/src/commonMain/kotlin/com/example/data/database/converter/DatabaseConverters.kt` for list-to-JSON serialization and `Instant` ↔ `Long` conversion
- [x] Configure abstract database in `/data/src/commonMain/kotlin/com/example/data/database/AppDatabase.kt` with `@ConstructedBy(AppDatabaseConstructor::class)` and all 11 entities registered
- [x] Define `expect object AppDatabaseConstructor` in `commonMain`
- [x] Rely on Room KSP to automatically generate the platform-specific `actual object AppDatabaseConstructor` declarations
- [x] Implement platform-specific builders (`getDatabaseBuilder`) in:
  - `androidMain` (`getDatabaseBuilder(context)`)
  - `iosMain` (`getDatabaseBuilder()` with `NSFileManager`)
- [x] Set up standard destructive migration configuration on builders (`fallbackToDestructiveMigration(true)`)
- [x] Apply Kotlin KSP multiplatform dependencies under `data/build.gradle.kts` including `kspCommonMainMetadata`

## KMP Pre-Flight Verification

- [x] `./gradlew :data:build` compiles and builds successfully for all targets (Android + common Main)
- [x] `compile_applet` succeeds
- [x] No manual `actual object AppDatabaseConstructor` declarations written, preventing duplicate and redeclaration conflicts with generated code
- [x] No `android.*` imports in any `commonMain` files
- [x] No `platform.*` imports in any `commonMain` files

## Constraints Applied

- **[NO-DELETE]**: Snapshot histories are append-only. No delete methods exist on `SecondingSnapshotDao` or entity relationships.
- **[DISCOURSE-ONLY]**: Reasoning comments (`CommentRecord`) have no `parentId` column, while discourse threads (`DiscourseRecord`) have the optional `parentId` column.
- **[NO-MIGRATION-ANIM]**: Destructive migration is configured via `fallbackToDestructiveMigration` on both Android and iOS builders, avoiding complex migration path structures or migration animations.

## Reusable Components

- **Room Platform Builders**: Isolated and reusable database builders that handle platform-dependent database paths properly.
- **Type Converters**: Reusable converters for serializing lists and enums uniformly across entities.

## Verification Notes

- Run `gradle :data:build` to confirm that the entire data layer builds and Room code generation generates correct platform actuals and database schemas.

## Cross-Cutting Decisions

- **KSP Code Gen Integration**: Removing manual `actual object` configurations to allow KSP's Room compiler to generate the exact necessary constructor actualizations prevents any class redeclaration errors during compilation.
- **Value Serialization**: All enums are serialized as `String` inside entities for durability across code modifications, and all timestamps are mapped to `Long` epoch milliseconds.

## Blockers / TODOs

- None. Phase 2 is complete, verified, and compiles successfully.

## Next Phase Prompt

> Read `BUILD_STATUS.md`, `status/phase_01_domain.md`, and `status/phase_02_room.md`. Build Phase 3: Network & API Layer. Implement DTOs, API Service, Fake API Service with artificial latency/failure rate/scenarios, SeedDataProvider, and a custom testing harness. Follow `AGENT_PROMPT_TEMPLATE_KMP.md` §"Network & API". Generate `status/phase_03_network.md` and update `BUILD_STATUS.md` index.
