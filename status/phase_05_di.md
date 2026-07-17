# Phase 5: Dependency Injection (Koin)

## Status: COMPLETE

## Checklist of Deliverables

- [x] Create standard shared Koin module declarations in `/di/src/commonMain/kotlin/com/example/di/`:
  - **DatabaseModule.kt**: Resolves the multiplatform `AppDatabase` cleanly by building the injected platform `RoomDatabase.Builder<AppDatabase>` instance, and registers all 10 DAOs as singletons.
  - **NetworkModule.kt**: Instantiates the central `FakeNetworkConfig` singleton, configures the `HttpClient` installing `ContentNegotiation` with JSON serialization, registers `EugeneApiService` dynamically (releasing `FakeEugeneApiService` or `KtorEugeneApiService` based on `BuildFlags.USE_FAKE_BACKEND` status), and exposes `NetworkMonitor` mapped to `NetworkMonitorImpl`.
  - **DataModule.kt**: Registers the single-scoped platform preferences bridge `PlatformSettings`, the `SessionStore` helper, and all 9 repositories mapped to their interfaces (`PredictionRepositoryImpl`, `SecondRepositoryImpl`, etc.) as singletons.
  - **DomainModule.kt**: Establishes factory-scoped declarations for all 9 domain layer use cases (e.g. `GetFilteredPredictionsUseCase`, `CastSecondUseCase`, etc.) ensuring fresh instances are delivered per injection request.
  - **ViewModelModule.kt**: Configures placeholder view model entries for all major target screens (`HomeFeedViewModel`, `ExploreViewModel`, etc.) utilizing modern `viewModelOf` constructor-binding DSL.
- [x] Implement multiplatform expectation `platformModule` in `/di/src/commonMain/kotlin/com/example/di/PlatformModule.kt` paired with:
  - `/di/src/androidMain/kotlin/com/example/di/PlatformModule.android.kt` producing the Android-specific database builder with the injected `androidContext()`.
  - `/di/src/iosMain/kotlin/com/example/di/PlatformModule.ios.kt` returning the iOS native file-system database builder.
- [x] Introduce placeholder ViewModel classes in `/di/src/commonMain/kotlin/com/example/di/viewmodel/Placeholders.kt` subclassing `androidx.lifecycle.ViewModel` to satisfy compilation dependencies of Koin constructor resolution ahead of UI implementation.
- [x] Incorporate Koin Compose App-Level initialization in `/composeApp/src/main/java/com/example/eugene/App.kt` by declaring Koin modules under the `KoinApplication` DSL wrapper and scoping the active layout / navigation inside `KoinContext`.
- [x] Update `MainActivity.kt` in `composeApp` to launch the root scoped `App()` layout container.
- [x] Align Gradle build configurations (`/di/build.gradle.kts` and `/composeApp/build.gradle.kts`) including Ktor client and Koin compose multiplatform dependencies.

## KMP Pre-Flight Verification

- [x] `./gradlew :di:build` compiles successfully for all KMP targets.
- [x] `./gradlew :composeApp:build` compiles successfully.
- [x] `compile_applet` finishes successfully.
- [x] Zero android or platform imports present in shared `commonMain` code outside the platform target bounds.

## Constraints Applied

- **[NO-MIGRATION-ANIM]**: Destructive database fallback migrations are seamlessly handled at the DI injection boundary, requiring zero user-facing migration loading screens.
- **[NO-DELETE]**: Local state engines (like `SessionStore`) use secure, explicit CRUD patterns that preserve core audit-ready requirements.
- **[OFFLINE-SAME]**: `FakeNetworkConfig` is exposed as a shared single instance, enabling uniform simulated state manipulation across active mock service instances and debug dashboard components.

## Reusable Components

- **Koin Multiplatform Platform Module (`PlatformModule`)**: Reusable target-agnostic expect/actual module pattern that lets platform-specific database construction occur without exposing platform contexts or wrappers to shared logic.
- **Unified ViewModel declarations (`Placeholders`)**: Clean placeholder lifecycle viewmodels that safely bridge early-phase dependency injection structures to eventual UI bindings.

## Verification Notes

- Compilation and linking verified through `./gradlew assembleDebug`. The build has succeeded and generated a fully linked, runtime-ready application artifact.

## Cross-Cutting Decisions

- **Shared Fake Config**: The `FakeNetworkConfig` is intentionally declared as a singleton. This allows full interoperability between the network simulator in `FakeEugeneApiService` and the Debug Screen we will build in Phase 7/8, ensuring runtime toggles affect the active API immediately.
- **Clean Constructor Binding**: Used exact constructor-argument mapping for repository implementation registrations to ensure 100% build compatibility and to dodge potential reflection issues on Native iOS compiles.
