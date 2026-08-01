# Platform capability and policy gate

This document is the review gate for platform-specific NoScroll+ work. Before adding an Android or Apple integration, verify the current official documentation and store policy again. Do not assume an API or entitlement remains available because an older sample used it.

## Android

Current references reviewed on 2026-08-01:

- [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- [Dependency injection with Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [Android app architecture](https://developer.android.com/topic/architecture)
- [AccessibilityService reference](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService)
- [Google Play Accessibility API policy](https://support.google.com/googleplay/android-developer/answer/9888379)

Implications for this project:

- DataStore must be a singleton per file and accessed through the data layer, not directly from composables.
- Hilt's current Android guidance uses KSP for code generation and Java 17.
- An Accessibility Service is not a generic way to block arbitrary app UI. It requires a user-visible permission flow, a clear core accessibility purpose, privacy disclosures and Play policy review.
- No Accessibility Service is implemented in the current milestone.

## Apple

Current references reviewed on 2026-08-01:

- [Family Controls](https://developer.apple.com/documentation/familycontrols)
- [Managed Settings](https://developer.apple.com/documentation/managedsettings)
- [Device Activity](https://developer.apple.com/documentation/deviceactivity)
- [Requesting the Family Controls entitlement](https://developer.apple.com/documentation/familycontrols/requesting-the-family-controls-entitlement)
- [Family Controls distribution request](https://developer.apple.com/contact/request/family-controls-distribution)

Implications for this project:

- Screen Time-style controls use Family Controls, Managed Settings and Device Activity together.
- The Family Controls capability and entitlement are required before authorization APIs can be called.
- Apple requires a distribution request for the entitlement before App Store submission.
- User selections are represented through privacy-preserving tokens; the app should not assume it can identify all selected apps directly.
- No Screen Time extension or blocking implementation is included in the current milestone.

## Decision rule

If official documentation or store policy blocks a requested capability, keep the shared `BlockingEngine` interface and implement the closest supported alternative, such as focus schedules, reminders, user-controlled app selections or local statistics. Record the decision here before changing platform code.
