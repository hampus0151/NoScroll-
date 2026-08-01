# Architecture

```text
NoScroll+ Compose UI (commonMain)
        |
NoScrollViewModel / StateFlow
        |
NoScrollRepository
        |
Platform persistence and blocking adapters
   Android: DataStore + future Accessibility Service
   iOS: platform-approved Screen Time APIs, subject to review
```

The shared NoScroll+ layer owns product concepts and user-facing state. Platform code owns permissions, persistence adapters and OS integrations. `BlockingEngine` is intentionally an interface in version 0.1 so the blocking implementation can be added without coupling the UI to Android Accessibility APIs.

## Dependency injection

The Android application uses Hilt to provide an Android DataStore-backed repository. The iOS shell currently uses the in-memory repository until a native persistence adapter is added behind `NoScrollRepository`.

## Privacy boundary

NoScroll+ must request only the permissions required for its active features. Accessibility and Screen Time integrations should be explained in onboarding and never treated as silently granted. No content should be uploaded for classification in the initial product.
