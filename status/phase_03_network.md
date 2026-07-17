# Phase 3: Network & API Layer

## Status: COMPLETE

## Checklist of Deliverables

- [x] Create Remote DTO records with `@Serializable` and `@SerialName` in `/data/src/commonMain/kotlin/com/example/data/remote/dto/`:
  - `PredictionDto` & `OptionDto` (snake_case mappings for dates and numeric values)
  - `SecondDto`
  - `CommentDto` (no `parent_id` column, flat reasoning list format)
  - `DiscourseDto` (retains `parent_id` column for threaded discussion formatting)
  - `KeepTabDto`
  - `SecondingSnapshotDto` & `SecondingSnapshotOutcomeDto` (timestamped outcome distributions)
  - `UserDto` (returns accuracy and reputation aggregates)
  - No `SubmissionDto` or `AppealSubmissionDto` (handled via map payloads to preserve fire-and-forget characteristics)
- [x] Create `EugeneApiService` client interface in `/data/src/commonMain/kotlin/com/example/data/remote/EugeneApiService.kt`
- [x] Implement Ktor integration client `KtorEugeneApiService` in `/data/src/commonMain/kotlin/com/example/data/remote/KtorEugeneApiService.kt`
- [x] Implement the simulated in-memory service `FakeEugeneApiService` in `/data/src/commonMain/kotlin/com/example/data/remote/FakeEugeneApiService.kt`
- [x] Implement network simulation settings in `FakeNetworkConfig.kt`:
  - `latencyMs` delay settings
  - `failureRate` probability setting
  - `forcedScenario` deterministic state simulation
  - `isOffline` flag for direct airplane-mode/disconnect verification
  - `isSessionExpired` flag to trigger non-dismissible session expires
- [x] Implement the `FakeScenario` enum containing all 11 forced criteria:
  - `SUCCESS`, `SLOW_SUCCESS`, `EMPTY_RESULT`, `NETWORK_ERROR`, `SERVER_ERROR`, `VALIDATION_ERROR`, `TRANSIENT_ERROR`, `PERSISTENT_ERROR`, `CONFLICT_STALE_CLOSE`, `RATE_LIMITED`, `NO_ROLLUP_YET`
- [x] Create the mock database engine `SeedDataProvider.kt` providing robust happy-path and edge-case dataset seeds:
  - Predictions spanning exactly 2, 3, 4, 5, and 6 outcomes with and without hero images
  - Full discourse thread 3 levels deep (`parent_id` reply-nesting)
  - Predictors at threshold boundaries (`resolvedPredictionCount` at exactly 4 vs. exactly 5)
  - KeepTab tracking on a profile below the threshold
  - Raw snapshot history with irregular intervals and no rollup computed yet
  - Moderation cards (Content Removed, Reasoning Flagged at exact 50 boundary, and Account Limited indicators)
- [x] Implement Clean Architecture model conversion mappers in `/data/src/commonMain/kotlin/com/example/data/remote/mapper/RemoteMappers.kt`
- [x] Create `BuildFlags.kt` and default `USE_FAKE_BACKEND = true` for active development
- [x] Write thorough mock testing suites in `FakeEugeneApiServiceTest.kt` to guarantee all simulation edge cases and forced states behave identically to a production back-end

## KMP Pre-Flight Verification

- [x] `gradle :data:build` executes and compiles successfully for all multiplatform targets
- [x] `gradle :data:test` executes successfully and all 9 unit tests pass perfectly
- [x] `compile_applet` succeeds
- [x] No `android.*` or `platform.*` imports exist inside any `commonMain` network package, preserving total platform agnostic separation

## Constraints Applied

- **[IMMUTABLE]**: Simulated APIs treat Seconds as immutable. No edit endpoints exist on seconds, and adding a second re-evaluates percentages in memory.
- **[OFFLINE-SAME]**: Fake configs expose offline and reconnection triggers to evaluate queues.
- **[NO-DELETE]**: Snapshots and Seconds lists do not support removal queries, maintaining strict append-only rules.
- **[RATE-LIMIT]**: Full rate-limiting 429 exceptions can be forced deterministically via the `RATE_LIMITED` scenario.

## Reusable Components

- **Simulation Engine (`FakeEugeneApiService`)**: Fully stateful, in-memory mock engine that serves lists of DTOs, validates inputs, and triggers any network behavior on demand.
- **Data Seed Fixtures (`SeedDataProvider`)**: Uniform fixtures representing happy-paths and edge-cases that can be re-utilized for UI layouts, database testing, and previews.

## Verification Notes

- Run `gradle :data:test` to verify unit test behaviors. All scenarios and mock properties pass automatically with no compilation anomalies.

## Cross-Cutting Decisions

- **Map Serialization on Forms**: Submissions and Appeals use standard Map entries for payloads to prevent unnecessary schema definitions for fire-and-forget models.
- **State Mutation in Simulations**: Casting seconds automatically recalculates option aggregates and totals in-memory on the simulated server, reflecting instant mock client updates.
