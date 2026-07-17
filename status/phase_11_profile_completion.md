# Phase 11: Profile Screen Completion & Dynamic Milestone Verification

## Status: COMPLETE

## Checklist of Deliverables

- [x] **Reordered, Gated Stats Row**:
  - Reordered the profile stats row to position **Reputation** as the prominent headline stat, with **Accuracy Rate**, **Correct Seconds**, and **Total Seconds** following secondary.
  - Gated both Reputation and Accuracy Rate behind the required 5-resolved-predictions threshold (`stats.isAccuracyEligible`).
  - Below this threshold, both statistics display a secure, clean card indicator showing `"Accuracy shown after 5 resolved predictions"` paired with a lock glyph, while Correct and Total Seconds remain visible.
- [x] **Custom Canvas-Drawn Accuracy Chart**:
  - Developed an inline, custom-drawn `AccuracyTrendChart` using Jetpack Compose Canvas inside a translucent Material 3 Card.
  - Automatically calculates historical accuracy progression over time based on actual resolved Seconds from the repository.
  - Displays a beautifully styled lock placeholder card when the user is below the 5-resolved-prediction threshold, inviting them to make predictions to unlock.
- [x] **Achievement Badges & Reputation Tiers**:
  - Fully implemented the separate, closed sets specified by the PRD:
    - **Reputation Tiers**: *Newcomer* · *Rising* · *Analyst* · *Expert*
    - **Achievement Badges**: *Top 10%* · *Accuracy Master* · *Trend Spotter* · *Early Bird*
  - Dynamically derives earned status from live stats (e.g., accuracy percentages and correct Seconds count).
  - Adheres strictly to container slot requirements, rendering locked states as outline-only circles with a lock glyph and earned states with complete primary container colors.
- [x] **Unified Accordion Sections**:
  - Refined dropdown transitions for Prediction Submissions, Active Seconds Cast, and Milestone Achievements & Reputation Tiers.

## KMP Pre-Flight Verification

- [x] Pure Kotlin Compose code structures build cleanly on standard JVM and Gradle configurations.
- [x] No `android.*` or `platform.*` code leakage exists inside the common main codebases.
- [x] Compilation successfully passes via `./gradlew :composeApp:assembleDebug` check.

## Constraints Applied

- **[NO-GAMIFICATION]**: Strictly tracks non-gamified milestone representations and reputation tiers. No streaks, XP, or levels are used.
- **[NO-RED-OR-GREEN]**: Leverages standard Material 3 primary/secondary/tertiary colors for a cohesive and high-contrast experience, avoiding deep gambling-style reds/greens.
- **[ACCURACY-GATE]**: Safeguards privacy and prediction credibility by locking both Reputation and Accuracy stats until 5 predictions are fully resolved.

---

## Profile Screen Loading & Guest Fix

- **Root Cause Identified**: When starting the app or after logging out, the session defaults to `Session.Guest`. The `ProfileViewModel` stayed stuck in `ProfileUiState.Loading` indefinitely as it had no way to map a guest session into a readable profile.
- **Implemented Fixes**:
  1. **`ProfileUiState.Guest`**: Added a new explicit state to represent an unauthenticated Guest accessing the Profile tab.
  2. **Themed Sign-In UX**: Updated `ProfileScreen.kt` to catch `ProfileUiState.Guest` and show an elegant Material 3 Sign In component containing clear call-to-action buttons to log in as either an "Established Fan" (simulating a fully populated historical profile) or a "New Predictor".
  3. **Auto Sign-In on Launch**: Configured a delicate background coroutine inside `AuthRepositoryImpl.kt`'s initialization that automatically signs the user in as `established@example.com` on the very first start. This prevents any initial empty states while still fully supporting Guest navigation modes if they manually sign out.
  4. **Dynamic Transition**: Clicking Sign-In from the Guest profile UI triggers immediate authentication via `signInAs(type)` in `ProfileViewModel`, transforming the screen smoothly into the fully populated `ProfileUiState.Loaded` state with springy list transitions.

