# Architecture

```text
Compose UI (commonMain)
        |
NoScrollViewModel / StateFlow
        |
NoScrollRepository
        |
Platform persistence and blocking adapters
   Android: DataStore + future Accessibility Service
   iOS: platform-approved Screen Time APIs, subject to review
```

The shared layer owns product concepts and user-facing state. Platform code owns permissions, persistence adapters and OS integrations. `BlockingEngine` is intentionally an interface in version 0.1 so the blocking implementation can be added without coupling the UI to Android Accessibility APIs.

## Dependency injection

The Android application is prepared for Hilt. The current shared repository is in-memory so the UI can be developed before persistence is finalized. The next implementation should provide a DataStore-backed Android repository and a native iOS persistence adapter behind `NoScrollRepository`.

## Privacy boundary

NoScroll must request only the permissions required for its active features. Accessibility and Screen Time integrations should be explained in onboarding and never treated as silently granted. No content should be uploaded for classification in the initial product.
