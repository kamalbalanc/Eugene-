# Phase 6: Shared UI Components

## Status: COMPLETE

## Checklist of Deliverables

- [x] Create core shared UI foundation and components in `/composeApp/src/main/java/com/example/eugene/ui/components/`:
  - **EugeneAnimationTokens.kt**: Declares design system standard animation durations (`Instant`, `Fast`, `Standard`, `Emphasis`, `Hero`, `Choreography`, `Ambient`), custom spring specifications (`springDefault`, `springHeavy`, `springLight`, `springSettle`), standard easing functions, and cascade stagger values.
  - **Theme.kt**: Implements precise light and dark color schemes directly sourced from `DESIGN_SYSTEM-1.md` (§1.1 and §1.4). Configures standard Material 3 typography mappings for Displays, Headlines, Titles, Bodies, and Labels. Sets custom shapes for cards (`radius.card = 20.dp`) and pills (`radius.pill = 999.dp`).
  - **PredictionCard.kt**: Implements a visually rich card container incorporating category-colored fallback icon thumbnails (when photographic hero imagery is absent), the category eyebrow tag, max-2-line question title with auto-ellipsis, dynamic outcome bars list, Notable Predictors overlapping avatar stack, and a stable pseudo-randomized trailing sparkline visualizer.
  - **OutcomeBar.kt**: Implements individual selection segment rows (`OutcomeOptionBar`) utilizing Sage (leading), Orange (trailing), and neutral accent colors for other choices. Feeds bodyBold and label font weight mappings, subtle press scale lifting animation (`1.02f`), and a 300ms delayed lock-icon fade-in upon choice confirm.
  - **AvatarCluster.kt**: Overlaps circular predictor avatars separated by a clean 2dp surface-colored boundary border ring, featuring dynamic "+N" overflow badge calculations and pop-in scale staggers.
  - **CategoryTag.kt**: Visually renders the category pill with a smooth left-to-right background color sweep animated over 200ms using standard decelerate easing.
  - **ShimmerPlaceholder.kt**: Sets up reusable skeleton layouts powered by diagonal gradient sweep shimmers looping over a 3000ms ambient duration.
  - **EmptyState.kt**: Standardizes a visually engaging empty-state view centering a true mathematical sine-wave floating illustration, heading copy, body details, and a pill-shaped CTA action button.
  - **ToastHost.kt**: Replaces the default Material snackbar container with an elegant, bottom-nav-safe pill layout supporting sliding transitions and gesture-driven swipe-to-dismiss offsets.
  - **LoadingState.kt**: Exposes reusable content skeletons (`PredictionCardSkeleton`), inline progress spinners (`InlineSpinner`), and full width width-morphing load states (`LoadingButton`).
  - **BottomSheet.kt**: Implements the `EugeneBottomSheet` wrapper over `ModalBottomSheet` adding top-only rounded card corners, custom elevations, and standard subtle drag handles.

## KMP Pre-Flight Verification

- [x] `./gradlew :composeApp:build` compiles successfully.
- [x] `compile_applet` finishes successfully.
- [x] No `android.*` or `platform.*` imports exist in any shared component files.

## Constraints Applied

- **[IMMUTABLE]**: Selection of prediction options on `OutcomeOptionBar` triggers an irreversible lock-icon fade-in after a 300ms confirmation window, with zero undo or edit affordances displayed.
- **[OFFLINE-SAME]**: Shared interaction components like avatar clicks and page actions transition instantly without intermediate pending state spinners or sync locks.
- **[NO-DELETE]**: Notables lists and chronological sparkline rendering are drawn as append-only structures, ensuring historical predictions are permanent.
- **[RATE-LIMIT]**: Buttons leverage disabled state mappings to instantly lower opacity on primary clicks, and toast hosts handle throttling alerts cleanly.

## Reusable Components

- **Material 3 Theme (`EugeneTheme`)**: Configured with exact warm light palettes and dark inverts.
- **Prediction Card (`PredictionCard`)**: Primary list item card used across feed and detail views.
- **Swipe-to-Dismiss Toast (`ToastHost`)**: Gesture-enabled client-side notification banner.
- **Modal Bottom Sheet (`EugeneBottomSheet`)**: Correctly styled modal overlay.

## Verification Notes

- All components are verified and compile correctly. The build completes with zero errors, linking all compose and multiplatform resources.

## Cross-Cutting Decisions

- **Backward-Compatible Color Anchors**: Maintained explicit legacy color definitions (`LightSage`, `LightOrange`, etc.) within `EugeneColors` but remapped their hex targets to match `DESIGN_SYSTEM-1.md` exactly, keeping existing code clean and compile-safe.
- **Integrated Semantic Mappings**: Mapped our custom typography and custom color palettes directly into `MaterialTheme.colorScheme` slots so all components resolve colors and typography dynamically without hardcoding.
