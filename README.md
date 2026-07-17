<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/12d734cd-0e01-4010-9414-833284506ca9

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device

---

## The Eugene Platform

**Eugene** is a highly disciplined, prediction-market-style mobile forecasting application built entirely in Kotlin Multiplatform (Android + iOS) using Jetpack Compose Multiplatform.

### Core Product Guidelines

1. **No Gambling/Betting Terminology**: Standard forecasting copy strictly centers around *Predictions*, *Outcomes*, *Seconds*, *Discourse*, and *Reasoning*.
2. **Strict Immutability**: All cast Seconds are final and permanent. There are no undo grace periods or draft saves.
3. **One-Way Keep Tabs Graph**: Connection tracking is strictly one-directional. Reciprocal follower lists or vanity graphs are absent.
4. **No Gamification**: Avoids superficial dopamine vectors (streaks, levels, or XP). User progression is represented strictly by computed *Accuracy Rates* and *Reputation*.
5. **Color Standards**: Restricts standard red/green highlights, opting instead for **Sage** (correct/leading), **Orange** (incorrect/trailing), and **Amber** (pending) statuses.

### Developer Debug Menu

A robust Developer Debug Menu is integrated directly into the app for verifying complex network, validation, and session states.

*   **How to Access**: Navigate to either the **Home** feed or **Explore** search screen, and perform a **5-tap sequence** directly on the screen's title header ("Home" or "Explore").
*   **What you can control**:
    *   **Fake Network Configuration**: Adjust network latency sliders (0–5000ms) and artificial failure rates (0–100%).
    *   **Deterministic Scenarios**: Force-trigger 11 distinct network/validation scenarios (e.g. Rate-Limiting, No Downsampled Rollup, Transient Conflicts).
    *   **Connectivity Emulation**: Simulate offline modes with functional reconnection banners.
    *   **Session Switching**: Instantly switch identities between Guest, New User (0 resolved), and Established Forecaster (5+ resolved).

### Running Tests

Execute the complete multi-module Kotlin JVM unit and integration test suite by running:

```bash
gradle test
```

For platform lints and APK compilation outputs, standard configurations can be built using:

```bash
gradle :composeApp:assembleDebug
```

