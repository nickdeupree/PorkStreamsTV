# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PorkStreams is an Android TV application built with Kotlin and Jetpack Compose. It is early-stage, with TMDB API integration planned for streaming content.

## Build Commands

This project uses Gradle with Kotlin DSL. Run from the project root on Windows:

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build and install debug APK to connected device/emulator
./gradlew assembleRelease        # Build release APK
./gradlew build                  # Full build (compile + test)
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests on device/emulator
./gradlew lint                   # Run lint checks
```

On Windows, use `gradlew.bat` if the shell doesn't resolve `./gradlew`.

## Architecture

**Single-Activity Compose app** — no Fragments. `MainActivity.kt` is the sole entry point and hosts all Compose UI.

**Tech Stack:**
- Kotlin 2.0.0 with Jetpack Compose (Compose BOM 2024.04.01)
- `androidx.tv:tv-foundation` and `androidx.tv:tv-material` for TV-optimized components
- Min SDK 21, Target SDK 35, JVM target 11

**Package structure:** `com.ncwd.porkstreams`

**UI Theme:** Centralized in `ui/theme/` — `Color.kt`, `Theme.kt`, `Type.kt`. The root composable wraps content in `PorkStreamsTheme`.

**TV-specific considerations:**
- The app uses `LEANBACK_LAUNCHER` intent filter so it appears in Android TV launchers
- `android.hardware.touchscreen` is declared as `required=false` (TVs use D-pad/remote)
- Prefer `androidx.tv.*` components over standard Material 3 equivalents for focus/D-pad handling

**Environment:** TMDB API key is stored in `.env` as `TMDB_API_KEY`. When wiring up network calls, load this key via `BuildConfig` (add it to `app/build.gradle.kts` as a `buildConfigField`).

## Dependency Management

Dependencies are declared in `gradle/libs.versions.toml` (version catalog). Add new libraries there first, then reference them in `app/build.gradle.kts` via `libs.*` aliases.
