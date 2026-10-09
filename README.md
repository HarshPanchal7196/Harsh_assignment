# Learning Dashboard

## 1. Architecture

The application uses **MVVM with unidirectional data flow**, a repository layer, Hilt dependency injection, and a single-activity Jetpack Compose UI. ViewModels expose immutable `StateFlow` UI states, while interfaces separate authentication, course data, local storage, and remote access. This structure keeps business logic out of composables, follows SOLID principles, improves testability, and allows the API or database implementation to change without affecting the UI.

## 2. Offline Support

The hosted mock API is fetched through Retrofit and successful responses are stored transactionally in Room. Room is the UI's source of truth, and its `Flow` streams automatically update Dashboard and Course Details. Previously loaded courses and lessons remain available without internet, and lesson completion is saved locally. Refreshes preserve locally completed lessons. Cached course data is accessible only while authenticated and is deleted on logout.

## 3. Security

This assignment uses mock authentication and stores only a session flag—never a password. In production, short-lived access tokens and refresh tokens would be stored using the **Android Keystore**, typically through a Keystore-backed encrypted storage implementation. Tokens would never be logged or stored as plain text, and communication would use HTTPS with appropriate token rotation and revocation.

## 4. Scale

For one million users and hundreds of courses, I would:

1. Add authenticated, paginated backend APIs with server-side filtering and stable cursor pagination.
2. Use incremental Room synchronization, cache expiry rules, and background refresh with WorkManager instead of replacing the complete catalogue.
3. Introduce CDN-backed media delivery, image caching, and adaptive content formats.
4. Add resilient networking with request deduplication, retry/backoff, rate limiting, and observability for crashes, latency, and sync failures.
5. Modularize features and add automated UI, database migration, contract, performance, and accessibility testing in CI/CD.

## 5. Second Platform

On iOS/macOS, I would use **SwiftUI** with MVVM and observable state, `NavigationStack` for navigation, `URLSession` with `async/await` for networking, and Core Data or SwiftData as the offline source of truth. Dependency injection would be protocol-based, authentication tokens would be stored in Keychain, and repository streams would keep the course list and details views synchronized across lifecycle changes.
