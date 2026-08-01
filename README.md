# NoScroll+

NoScroll+ is a Kotlin Multiplatform app foundation for reducing addictive short-video feeds and infinite scrolling while keeping the rest of each platform useful.

## Vision

Make intentional phone use easier without asking people to abandon the apps they rely on. The first release focuses on a calm, premium control surface; platform blocking is deliberately isolated for a later milestone.

## Current foundation

- Kotlin Multiplatform with shared Compose UI
- Material 3 dark-first visual system
- MVVM-style state holders with `StateFlow`
- Repository boundary for persisted settings and statistics
- Android composition root prepared for Hilt and DataStore
- iOS entry point through the shared Compose screen
- Home, Statistics and Settings screens
- Premium feature placeholders
- No Accessibility Service or blocking engine yet

## Architecture

`commonMain` owns models, repositories, view state and the shared UI. Platform source sets own platform entry points and integrations. Future NoScroll+ blocking implementations should sit behind `BlockingEngine` so Android Accessibility APIs and iOS-approved APIs do not leak into the shared domain layer.

## Run

1. Open this folder in Android Studio Ladybug or newer.
2. Let Gradle sync and install the Android SDK configured in `composeApp/build.gradle.kts`.
3. Run the `composeApp` Android configuration on an emulator or device.
4. For iOS, open `iosApp` in Xcode on macOS and run the iOS scheme.

The repository does not include generated Gradle wrapper binaries. Android Studio can generate them with `gradle wrapper` when a local Gradle installation is available.

## Roadmap

### Version 1

- [x] Initial UI
- [x] Settings surface
- [x] Statistics placeholder
- [ ] Persist settings with DataStore
- [ ] Add real notification preferences

### Version 2

- [ ] Android Accessibility Service integration
- [ ] Platform capability checks and onboarding

### Version 3

- [ ] YouTube Shorts detection and blocking

### Version 4

- [ ] Instagram Reels detection and blocking

### Version 5

- [ ] Snapchat Spotlight and Discover support
- [ ] Facebook Reels and TikTok support

### Version 6

- [ ] Premium entitlements and purchases
- [ ] Scheduling, focus modes and unlimited rules

### Version 7

- [ ] On-device insights and optional AI-assisted focus coaching

## Product notes

Blocking behavior is platform-specific and must follow Google Play, Apple App Store and each platform's privacy rules. No blocking claim is made by this foundation until the platform integrations are implemented and reviewed.
