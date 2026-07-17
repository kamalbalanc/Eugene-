# Phase 8: Core Platform Behaviors

## Status: COMPLETE

## Checklist of Deliverables

- [x] Create comprehensive custom Toast System and Catalog in `composeApp/src/main/java/com/example/eugene/ui/system/`:
  - **ToastSystem.kt**: Direct implementation of single-visible `ToastHost` and swipe-to-dismiss `ToastCard` allowing full stack placement and velocity-based dismissals.
  - **ToastCatalog.kt**: Type-safe toast factories including "Second cast. This cannot be changed.", "Marked helpful", "Keeping tab on @[handle]", "No longer keeping tab on @[handle]", "Saved", "Removed bookmark", and rate-limiting alerts.
- [x] Create Connectivity and Offline Handling in `OfflineBanner.kt` and `OfflineHandling.kt`:
  - **OfflineBanner.kt**: Elegant slide-down banner displaying connectivity status. Animates WiFi icon with a ±15° rotation sequence for 3 cycles and expands content smoothly.
  - **OfflineHandling.kt**: Inline error state helpers, reactive connection listeners, and blocked-action indicators.
- [x] Create Rate-Limiting Controls in `RateLimitHandler.kt`:
  - **RateLimitHandler.kt**: Two-stage rate limiting mechanism. Disables click targets immediately (0.5 opacity) without animating on the first overflow hit, then displays a warning toast on repeated hits.
- [x] Create Error Boundaries in `ErrorBoundary.kt`:
  - **ErrorBoundary.kt**: Multi-tier error UI components, including full-screen blocking error displays with custom illustrations, a degraded state banner for cached/stale content, and action-level retry buttons that transition to help links after 3 consecutive failures.
- [x] Create Developer Debug Menu in `DebugMenu.kt`:
  - **DebugMenu.kt**: Comprehensive overlay panels configured to alter fake network latencies (0-5000ms), error rates (0-100%), mock system scenarios, offline states, reconnect sync tasks, and session identities (Guest, New, Established). Triggered via a 5-tap sequence on screen headers.
- [x] Create Session Expiration Handler in `SessionExpiryHandler.kt`:
  - **SessionExpiryHandler.kt**: Non-dismissible session termination alert blocking user interactions and forcing safe sign-outs and redirect routes.

## KMP Pre-Flight Verification

- [x] All platform behavior modules compile and build successfully.
- [x] `./gradlew :composeApp:assembleDebug` compiles cleanly.
- [x] Pure Kotlin code structures without leakage of `android.*` or `platform.*` imports into shared Main source sets.

## Constraints Applied

- **[IMMUTABLE]**: Confirmed seconds are permanent; locked status overlays and lock-icon transitions prevent modification or undo attempts.
- **[OFFLINE-SAME]**: Keep-tab actions maintain identical local animations on offline states, caching writes for background reconnection loops.
- **[NO-DELETE]**: Offline buffers and snapshot graphs are strictly append-only, preventing manual deletions.
- **[RATE-LIMIT]**: Click throttle states instantly bypass animations and block triggers with a standard toast warning.
- **[APPEAL-ONCE]**: Appeal submissions pulse during selection and disable permanently on the first tap to block duplication.
- **[DUAL-ODOMETER]**: Reputation odometers trigger 50ms before accuracy odometers, preserving consistent visual choreography.

## Reusable Components & Nav

- **ToastHost / ToastCard**: High-precision toast overlay layout with gesture detection.
- **OfflineBanner**: Sticky connectivity indicator supporting entrance/exit slide-animations and icon rotations.
- **RetryButtonWithCeiling**: Adaptive retry action target tracking attempt counts and falling back to external status portals.

## Verification Notes

- Verified swipe-to-dismiss velocity responses on the `ToastCard`.
- Verified 5-tap secret gesture on Home and Explore titles to reliably toggle the Debug Menu.
- Verified session expiry locks the screen with a non-dismissible dialog until authenticated again.

## Cross-Cutting Decisions

- **Unbound State Reactive Streams**: Chose to keep simulated session expiry, offline state, and toast indicators as localized, reactive Compose states or singleton properties, which allows instant, fluid UI reactions without requiring complex network interceptor overhead.
- **Unified Simulated Login**: Standardized session forcing to automatically trigger `AuthRepository.signInWithEmail` and `AuthRepository.signOut` to keep the local UserDao and PlatformSettings fully synchronized with chosen debugger profiles.
