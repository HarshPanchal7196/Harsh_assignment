# Learning Dashboard — Android Implementation Tasks

This checklist is the implementation plan for the Senior Mobile App Developer assignment. Keep the application limited to the required user flow:

```text
Login -> Course Dashboard -> Course Details -> Complete Lesson
```

## Agreed implementation decisions

- Platform: Android
- Language/UI: Kotlin and Jetpack Compose
- Architecture: MVVM with unidirectional data flow
- Data access: Repository coordinating a hosted mock API and Room
- Offline behavior: Room is the UI's source of truth after the first successful API load
- Login behavior: Any valid email and any non-empty password succeeds
- Navigation: Single activity with Navigation Compose
- Dependency injection: Hilt
- Scope: Exactly three screens; no signup, password recovery, profile, search, filters, playback, quizzes, or other extra features

## Target architecture

```text
Compose Screen
    |
    v
Screen ViewModel (StateFlow<UiState>)
    |
    v
Repository
    |-------------------|
    v                   v
Hosted Mock API       Room database
                        |
                        v
                  UI source of truth
```

## Phase 0 — Project foundation

### Dependencies and configuration

- [x] Add Navigation Compose.
- [x] Add Lifecycle ViewModel Compose and Lifecycle Runtime Compose.
- [x] Add Kotlin coroutines.
- [x] Add Retrofit and a JSON converter for the hosted mock API.
- [x] Add Room runtime, Room KTX, and the Room compiler.
- [x] Add Hilt and Hilt Navigation Compose.
- [x] Add coroutine testing dependencies.
- [x] Add the `INTERNET` permission to `AndroidManifest.xml`.
- [x] Create the `Application` class and enable Hilt.
- [x] Register the `Application` class in `AndroidManifest.xml`.

### Package structure

- [x] Define the following package boundaries (packages are materialized as their feature files are added):

```text
com.example.harsh_assignment
├── data
│   ├── local
│   │   └── entity
│   ├── mapper
│   ├── remote
│   │   └── model
│   └── repository
├── di
├── domain
│   ├── model
│   ├── repository
│   └── usecase
└── ui
    ├── login
    ├── dashboard
    ├── details
    ├── navigation
    └── theme
```

### Domain models and business rule

- [x] Create `Course`.
- [x] Create `Lesson`.
- [x] Implement progress calculation:

```text
progress = completed lesson count * 100 / total lesson count
```

- [x] Return `0` progress when a course has no lessons.

### Navigation shell

- [x] Define only these destinations:
  - `login`
  - `dashboard`
  - `course/{courseId}`
- [x] Create `AppNavHost`.
- [x] Make Login the start destination.
- [x] Replace the generated greeting in `MainActivity` with the application theme and `AppNavHost`.
- [x] Pass only `courseId` to Course Details, not a serialized `Course` object.

---

## Screen 1 — Login

### Data and ViewModel

- [x] Create an `AuthRepository` contract.
- [x] Create `FakeAuthRepository`.
- [x] Simulate a short login delay so the loading state is visible.
- [x] Accept any correctly formatted email and any non-empty password.
- [x] Create `LoginUiState` containing:
  - Email value
  - Password value
  - Email validation error
  - Password validation error
  - Loading state
  - General login error
  - Login success state
- [x] Create `LoginViewModel`.
- [x] Keep validation and login behavior in the ViewModel, not in the composable.

### UI

- [x] Create `LoginScreen`.
- [x] Add an email text field.
- [x] Add a password text field with obscured input.
- [x] Add a Login button.
- [x] Show “Email is required” for an empty email.
- [x] Show “Enter a valid email” for an invalid email format.
- [x] Show “Password is required” for an empty password.
- [x] Show a progress indicator during login.
- [x] Disable the Login button while login is in progress.
- [x] Display the error state if the mocked login fails unexpectedly.
- [x] Navigate to Dashboard after a successful login.
- [x] Remove Login from the back stack after successful navigation.

### Login acceptance criteria

- [x] Empty inputs do not submit.
- [x] Invalid email does not submit.
- [x] A valid email and non-empty password show loading and then open Dashboard.
- [x] Pressing Back from Dashboard does not return to Login.

### Session and logout

- [x] Persist the authenticated session after a successful login.
- [x] Restore Dashboard directly while the session remains authenticated.
- [x] Add an accessible logout action to the Dashboard header.
- [x] Cancel an active course refresh before logout cleanup.
- [x] Clear cached courses and lessons from Room during logout.
- [x] End the persisted session only after local cleanup succeeds.
- [x] Return to Login and clear authenticated navigation history after logout.
- [x] Prevent course reads, refreshes, and lesson mutations without an authenticated session.
- [x] Show a retryable error if logout cleanup fails.

---

## Screen 2 — Course Dashboard

### Hosted mock API

- [x] Create a stable hosted JSON endpoint for the course response.
- [x] Include the three requested courses:
  - Python Programming — John Smith
  - Generative AI — Sarah Williams
  - Full Stack Development — David Brown
- [x] Include lesson data for each course because the assignment's sample course JSON does not define lessons.
- [x] Keep the mock lesson data consistent with the displayed lesson count and derived progress.
- [x] Give every course and lesson a stable unique ID.
- [x] Create Retrofit DTOs for courses and lessons.
- [x] Create `CourseApi`.
- [x] Create `CourseRemoteDataSource`.
- [x] Keep API DTOs out of the UI layer.

### Room cache

- [x] Create `CourseEntity`.
- [x] Create `LessonEntity` with `courseId` as its parent key.
- [x] Create the `CourseWithLessons` Room relation.
- [x] Create `CourseDao` with operations to:
  - Observe all courses with lessons
  - Observe one course with its lessons
  - Replace API courses and lessons in a transaction
  - Mark a lesson completed
- [x] Create `LearningDatabase`.
- [x] Create mappings between API DTOs, Room entities, and domain models.

### Offline-first repository

- [x] Create `CourseRepository` with:

```kotlin
fun observeCourses(): Flow<List<Course>>
fun observeCourse(courseId: Long): Flow<Course?>
suspend fun refreshCourses()
suspend fun markLessonCompleted(lessonId: Long)
```

- [x] Implement `OfflineFirstCourseRepository`.
- [x] Make Room the stream observed by the ViewModels.
- [x] Fetch the hosted API during refresh.
- [x] Save a successful response to Room in a transaction.
- [x] Continue exposing cached Room data when refresh fails.
- [x] Return an error when refresh fails and the cache is empty.
- [x] Allow an empty API response to produce the required empty state.

### Dashboard ViewModel

- [x] Define exclusive `DashboardUiState` values:
  - Loading
  - Success with courses
  - Empty
  - Error with a message
- [x] Create `DashboardViewModel`.
- [x] Observe courses from Room through the repository.
- [x] Trigger the initial remote refresh.
- [x] Expose a retry action for the error state.
- [x] Do not discard visible cached courses because a refresh failed.

### Dashboard UI

- [x] Create `DashboardScreen`.
- [x] Create a reusable `CourseCard`.
- [x] For every course, display:
  - Course name
  - Instructor name
  - Progress percentage
  - Progress indicator
  - Number of lessons
  - Continue button
- [x] Render a full-screen loading state before the first result.
- [x] Render the course list for success.
- [x] Render “No courses available” for an empty response.
- [x] Render an error message and Retry button when there is no cached data.
- [x] Open `course/{courseId}` when Continue is selected.

### Dashboard acceptance criteria

- [x] Loading, success, empty, and API-failure states can all be verified.
- [x] A successful response is persisted before it is displayed from Room.
- [x] Every course displays all fields requested in the assignment.
- [x] Continue opens the selected course.
- [x] Returning from Course Details displays the latest progress.

---

## Screen 3 — Course Details

### Details ViewModel

- [x] Define `CourseDetailsUiState` values:
  - Loading
  - Success with the selected course and lessons
  - Error
- [x] Create `CourseDetailsViewModel`.
- [x] Read `courseId` from `SavedStateHandle`.
- [x] Observe the selected course from Room through `CourseRepository`.
- [x] Expose `markLessonCompleted(lessonId)`.
- [x] Ignore completion requests for lessons that are already completed.

### Details UI

- [x] Create `CourseDetailsScreen`.
- [x] Create `LessonItem`.
- [x] Add a Back button.
- [x] Display the course name.
- [x] Display the current progress percentage and indicator.
- [x] Display lessons in their defined order.
- [x] Clearly display `Completed` or `Pending` for every lesson.
- [x] Allow a pending lesson to be marked completed.
- [x] Disable the completion action for a completed lesson.

### Lesson completion behavior

- [x] Update the selected lesson in Room.
- [x] Recalculate progress from the updated lesson list.
- [x] Allow the Room flow to refresh Course Details automatically.
- [x] Allow the same Room update to refresh Dashboard progress automatically.
- [x] Ensure lesson completion survives navigation and app restart.

### Details acceptance criteria

- [x] The selected course name and progress are correct.
- [x] Completed and pending lessons are visually distinguishable.
- [x] Completing a pending lesson changes its status immediately.
- [x] Progress updates immediately after completion.
- [x] The Dashboard shows the same updated progress.

---

## Offline verification

- [x] Clear app data before the first verification run.
- [x] Launch the app with internet access.
- [x] Log in with a valid email and non-empty password.
- [x] Wait for the course list to load from the hosted mock API.
- [x] Confirm the response has been cached in Room.
- [x] Turn off device/emulator internet access.
- [x] Restart the app.
- [x] Log in again.
- [x] Confirm previously loaded courses appear from Room.
- [x] Open Course Details and confirm lessons appear.
- [x] Complete a lesson while offline.
- [x] Confirm lesson status and progress update locally.
- [x] Restart again and confirm the offline change remains stored.

---

## Testing

- [x] Replace the generated example unit test with a meaningful progress-calculation test.
- [x] Test that no lessons produces `0%`.
- [x] Test that some completed lessons produce the expected percentage.
- [x] Test that all completed lessons produce `100%`.
- [x] Run all unit tests successfully.
- [x] Test that a remote failure does not remove cached courses.

---

## README and submission

- [x] Keep the README to a maximum of one page.
- [x] Briefly explain the MVVM and repository architecture.
- [x] Explain that Room is the offline source of truth.
- [x] State that production authentication tokens would use Android Keystore-backed secure storage.
- [x] List three to five scale improvements for one million users and hundreds of courses.
- [x] Briefly explain the equivalent iOS/macOS implementation.
- [ ] Add project build/run instructions.
- [x] Build and install the APK.
- [x] Verify the complete user flow on an emulator or device.
- [ ] Record a demo video of no more than two minutes showing:
  - Login
  - Course list
  - Course details
  - Lesson completion
  - Offline behavior
- [ ] Push the complete source to GitHub or GitLab.

## Definition of done

The assignment is complete only when:

- [x] The app has exactly the three required screens.
- [x] Login validation, loading, and error states work.
- [x] Dashboard loading, success, empty, and API-failure states work.
- [x] Course Details displays lessons and permits completion.
- [x] Progress stays consistent across Dashboard and Course Details.
- [x] Previously loaded courses and lessons work without internet.
- [x] Lesson completion persists locally.
- [x] At least one meaningful unit test passes.
- [ ] The README, APK, repository, and demo video are ready.
