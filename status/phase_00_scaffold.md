# Phase 0: KMP Project Scaffold

## Status: COMPLETE

## Checklist of Deliverables

- [x] Root `build.gradle.kts` with plugin aliases (all `apply false`)
- [x] `settings.gradle.kts` includes `:composeApp`, `:domain`, `:data`, `:di`
- [x] `gradle/libs.versions.toml` with all versions and libraries
- [x] `:domain/build.gradle.kts` — KMP plugin, `commonMain`/`commonTest`, no Android deps
- [x] `:data/build.gradle.kts` — KMP + Room KSP + `sqlite.bundled`, `kspIos*` entries
- [x] `:di/build.gradle.kts` — KMP plugin, Koin dependencies
- [x] `:composeApp/build.gradle.kts` — Android app plugin, depends on `:domain`, `:data`, `:di`
- [x] `data/schemas/` directory exists (empty)
- [x] Empty package directories with `.gitkeep` in all modules
- [x] Minimal `MainActivity.kt` in `:composeApp` that compiles
- [x] `AndroidManifest.xml` in `:composeApp`

## KMP Pre-Flight Verification

- [x] `./gradlew :domain:build` succeeds
- [x] `./gradlew :data:build` succeeds
- [x] `./gradlew :di:build` succeeds
- [x] `./gradlew :composeApp:build` succeeds
- [x] `./gradlew build` succeeds
- [x] No `android.*` imports in any `commonMain` file
- [x] No `platform.*` imports in any `commonMain` file
- [x] `data/build.gradle.kts` has `kspAndroid`, `kspIosSimulatorArm64`, `kspIosX64`, `kspIosArm64`
- [x] `AppDatabase` does not exist yet (correct — it comes in Phase 2)

## Constraints Applied

- **[NO-MIGRATION-ANIM]**: N/A for scaffold phase
- All other constraint tags: N/A — no business logic exists yet

## Reusable Components

- **`gradle/libs.versions.toml`**: Central version catalog used by all modules
- **Module structure**: `:domain` → `:data` → `:di` → `:composeApp` dependency chain

## Verification Notes

- **Human Spot-Check 1**: Open `settings.gradle.kts`, verify all 4 modules are included
- **Human Spot-Check 2**: Open `data/build.gradle.kts`, verify `kspIos*` entries exist
- **Human Spot-Check 3**: Run `./gradlew build` and confirm BUILD SUCCESSFUL

## Cross-Cutting Decisions

- **KMP from start**: All modules use `kotlin-multiplatform` plugin, not `kotlin-android`
- **Room KMP**: Using `sqlite-bundled` driver, `@ConstructedBy`, `expect/actual` constructors
- **Version catalog**: All dependencies centralized in `libs.versions.toml`

## Blockers / TODOs

- None (scaffold phase complete and fully compiles)

## Next Phase Prompt

> Read `BUILD_STATUS.md` and `status/phase_00_scaffold.md`. Build Phase 1: Domain Models + Unit Tests. Create all models, repository interfaces, and use cases in `domain/src/commonMain/`. Write unit tests in `domain/src/commonTest/`. Follow `AGENT_PROMPT_TEMPLATE_KMP.md` §"Domain Models". Generate `status/phase_01_domain.md` and update `BUILD_STATUS.md` index.
