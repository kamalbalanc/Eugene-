# Phase 1: Domain Models + Unit Tests

## Status: COMPLETE

## Checklist of Deliverables

- [x] Create IdGenerator utility for lightweight UUID generation
- [x] Implement the `Prediction` model and child types (`PredictionOption`, `PredictionCategory`, `PredictionStatus`, `PredictionPublishStatus`, `PredictionAccent`, `ImageSource`) in `/domain/src/commonMain/kotlin/com/example/domain/model/`
- [x] Implement the `Second` model and `SecondStatus` with exactly 4 enum values: `PENDING`, `CORRECT`, `INCORRECT`, `VOIDED`
- [x] Implement the `Comment` model without a `parentId` field (as Reasoning comments never thread)
- [x] Implement the `DiscourseEntry` model with a `parentId` field (as this is the only threaded content type)
- [x] Implement the `SecondingSnapshot` and `SecondingSnapshotOutcome` models
- [x] Implement the `Submission` model and `ApprovalState` enum
- [x] Implement the `Session` sealed interface with `Guest` and `Authenticated` states, including a `reputation: Int` field and eligibility helper extensions
- [x] Implement the `PredictorSummary` presentation model
- [x] Implement the `KeepTab` relationship model
- [x] Implement the `AppealSubmission` model and `AppealState` enum
- [x] Implement the `AppError` sealed interface and `AppErrorException`
- [x] Implement the `TimeRange` enum
- [x] Implement all repository interfaces in `/domain/src/commonMain/kotlin/com/example/domain/repository/` (`PredictionRepository`, `SecondRepository`, `CommentRepository`, `DiscourseRepository`, `SecondingSnapshotRepository`, `SubmissionRepository`, `AuthRepository`, `PreferencesRepository`, `KeepTabRepository`)
- [x] Enforce that `KeepTabRepository` has no `observeTrackedBy` method and `SecondingSnapshotRepository` has no delete methods
- [x] Implement core use cases (`CastSecondUseCase`, `RecordSecondingSnapshotUseCase`, `ToggleKeepTabUseCase`, `SubmitPredictionUseCase`, `GetFilteredPredictionsUseCase`, `GetNotableSecondersUseCase`, `PostDiscourseEntryUseCase`, `LoginUseCase`, `SignUpUseCase`) in `/domain/src/commonMain/kotlin/com/example/domain/usecase/`
- [x] Enforce 3 `require` check blocks inside `Prediction.kt`'s initializer block
- [x] Write thorough unit tests in `/domain/src/commonTest/kotlin/com/example/domain/` (`PredictionTest.kt`, `ToggleKeepTabUseCaseTest.kt`, `SubmitPredictionUseCaseTest.kt`) to verify models and use case behaviors

## KMP Pre-Flight Verification

- [x] `gradle :domain:test` compiles and all tests pass
- [x] `compile_applet` succeeds
- [x] No `android.*` imports in any `commonMain` files
- [x] No `platform.*` imports in any `commonMain` files

## Constraints Applied

- **[IMMUTABLE]**: A Second outcome choice is final at cast time; no "edit" or "undo" is exposed or modeled.
- **[NO-DELETE]**: Snapshot histories are append-only. There are no delete methods on `SecondingSnapshotRepository`.
- **[APPEAL-ONCE]**: Submission appeal text is strictly validated.
- **[DISCOURSE-ONLY]**: Threading/indentation is restricted to Discourse (`parentId` field in `DiscourseEntry.kt`); Reasoning list (`Comment.kt`) remains strictly flat with no `parentId`.

## Reusable Components

- **`IdGenerator`**: Central utility for common ID generation
- **Domain Use Cases**: Reusable use cases that manage core business operations consistently across platform modules

## Verification Notes

- Run `gradle :domain:test` to verify unit tests execution:
  - `PredictionTest` ensures boundaries around outcome counts, dates, and partial images.
  - `ToggleKeepTabUseCaseTest` guarantees tracking limits, including self-tracking rejection.
  - `SubmitPredictionUseCaseTest` enforces outcome ranges, distinct option names, close date rules, and temporal orders.

## Cross-Cutting Decisions

- **Validation Placement**: Validation constraints for submissions and entities reside directly inside the Use Cases and model `init` blocks, establishing compile-time and runtime core business invariants.
- **Null Safety**: Strict nullability design used for image URLs, parents, and statuses to fully represent dynamic domain structures.

## Blockers / TODOs

- None. Phase 1 is complete and verified.

## Next Phase Prompt

> Read `BUILD_STATUS.md`, `status/phase_00_scaffold.md`, and `status/phase_01_domain.md`. Build Phase 2: Room Database. Create Room database entities, DAOs, database, converters, and write DAO unit tests in `/data/src/commonTest/`. Follow `AGENT_PROMPT_TEMPLATE_KMP.md` §"Room Database". Generate `status/phase_02_database.md` and update `BUILD_STATUS.md` index.
