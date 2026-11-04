# Architecture

> **Status: partly implemented.** The foundation, authentication and profile are
> built; the rest describes the target architecture, and each section says what
> exists. Items marked *Open* need the project owner's approval (see
> [DECISIONS.md](DECISIONS.md)).

## Principles

Clean Architecture, MVVM, unidirectional data flow, repository pattern,
dependency inversion. Dependencies always point inward.

```
presentation (Compose, ViewModel)  ->  domain  <-  data (Room, Supabase, sync)
```

## Layers

**Presentation.** Compose screens, ViewModels, UI state and events,
navigation. State is immutable and exposed through `StateFlow`; screens collect
it in a lifecycle-aware way. Composables are stateless where reasonable and
contain no business logic.

**Domain.** Entities, repository interfaces, use cases, validation and business
calculations (project progress, habit streaks, recurrence). Pure Kotlin: no
Android, Room, Retrofit, Supabase or Compose.

**Data.** Repository implementations, Room entities and DAOs, DTOs, mappers,
remote data sources and the synchronization engine. Data models never reach the
domain.

## Data flow

Reads: the UI observes Room through the repository (`Flow`). Remote data is
written into Room, never shown directly.

Writes: validate, persist locally, mark the record pending, schedule a sync,
push when connectivity allows. See [SYNC.md](SYNC.md).

## Module structure

The nine `core/*` modules exist and `:app` depends on all of them. `common`,
`network` and `ui` are still empty. `feature/*` modules are created one at a time:
`feature:auth`, `feature:profile` and `feature:tasks` exist, the others do not yet. Each one is created
when its feature is built. `core:common` and `core:domain` are pure Kotlin (JVM)
modules; the others are Android libraries (`designsystem`, `ui` and `navigation` with
Compose). Target layout:

```
app/                 composition root, navigation host, Hilt application
core/
  common/            result/error types, dispatchers, utilities
  domain/            shared entities, repository contracts, use cases
  data/              repository implementations, sync engine
  database/          Room database, entities, DAOs, migrations
  network/           Supabase client and remote data sources
  datastore/         local preferences
  designsystem/      theme, tokens, base components
  ui/                shared composables and UI utilities
  navigation/        routes and navigation contracts
feature/
  auth/ dashboard/ tasks/ projects/ habits/ goals/
  calendar/ statistics/ profile/
build-logic/         Gradle convention plugins
```

Feature modules contain their own presentation layer and, where appropriate,
their own data implementation. Shared domain concepts live in `core/domain`.

### Dependency rules

- `feature/*` may depend on `core/*`, never on another `feature/*`.
- `core/domain` depends only on `core/common` (exposed through its API, since `Outcome` appears
  in the contracts), and is pure Kotlin.
- `core/datastore` depends on `core/domain`, because it implements a domain contract
  (`UserPreferencesRepository`).
- `core/database` and `core/network` know nothing about the domain: they hold the
  persistence and transport models. The mapping to domain models happens in `core/data`.
- `core/data` depends on `core/domain`, `core/database` and `core/network`.
- `core/ui` depends on `core/designsystem`.
- `app` is the only module that knows every other module (composition root).

These rules are currently enforced only by the dependency declarations in each
module and by review; there is no automatic check yet.

## Design system

Lives in `core:designsystem` (package `it.edon.lifeos.core.designsystem`). Screens
never use literal colors, dp values or text styles: they read tokens from
`LifeOSTheme`.

| Token | Access | Notes |
|---|---|---|
| Colors | `LifeOSTheme.colors` | Material 3 `ColorScheme`, "Calma" palette: deep indigo primary, teal accent, cool neutral surfaces; light and dark |
| Extra colors | `LifeOSTheme.extendedColors` | Task priorities (low, medium, high, urgent) and success. Priority is never conveyed by color alone |
| Typography | `LifeOSTheme.typography` | Material 3 scale in `sp` (follows font scale), system font for now |
| Shapes | `LifeOSTheme.shapes` | Soft rounded corners (6 to 28 dp) |
| Spacing | `LifeOSTheme.spacing` | `xxs` 2 dp to `xxxl` 48 dp, plus `minTouchTarget` (48 dp) |
| Elevation | `LifeOSTheme.elevation` | Hierarchy mainly through tonal surfaces, not shadows |
| Motion | `LifeOSTheme.motion` | Short, medium and long durations and a shared easing |

`LifeOSTheme { ... }` applies the theme. The LifeOS palette is always used by
default; dynamic color (Android 12+) exists as an opt-in parameter, intended to be
driven by a user preference later.

Base components (`component` package): `LifeOSButton`, `LifeOSTextButton`,
`LifeOSTextField`, `LifeOSCard` (tonal, no border or shadow), `LifeOSLinearProgress`
and the screen states `LifeOSLoadingState`, `LifeOSEmptyState`, `LifeOSErrorState`.
Components receive already-resolved strings; string resources belong to the screens.
New components are added when a screen needs them. `@Preview` functions in
`Previews.kt` show them in light and dark.

Not done yet: the Material icon set is not added (only `material-icons-core` comes
with Compose), no custom font, and no accessibility pass with a screen reader.

## Navigation

Navigation Compose with type-safe routes: each destination is a `@Serializable`
object or class, never a string.

- **Bottom bar, five tabs:** Home, Tasks, Calendar, Habits, More. Projects, Goals,
  Statistics and Profile/settings are meant to live under *More*, which also gives
  room to the future life modules without changing the bar.
- **`core:navigation`:** the routes of the top-level destinations, the
  `TopLevelDestination` enum (label, icons, deep link) and helpers:
  `navigateToTopLevel` (one copy of each tab, state saved and restored per tab) and
  `isInHierarchyOf` (selection also when a detail screen of a tab is open).
- **`core:designsystem`:** `LifeOSNavigationBar` and `LifeOSNavigationBarItem`; they
  know nothing about destinations.
- **`app`:** `LifeOSApp` (scaffold and bar) and `LifeOSNavHost` (graph). The bar is
  shown only on top-level destinations. Feature modules will contribute their own
  graphs to the host; they never depend on each other.
- **Deep links:** scheme `lifeos://` with `home`, `tasks`, `calendar`, `habits` and
  `more`, declared in the manifest and in the graph. The activity is `singleTop` so a
  link opened while the app is running reuses it. Links only select a tab and carry no
  data; links to inner screens will need validation of their arguments.

Status: every destination is a **placeholder** (`LifeOSEmptyState` saying the section
is not available yet). Each one is replaced when its feature is built.

## Extensibility: life modules

After version 1.0.0 new areas (finance, sport, nutrition, journal, work,
hobbies) are added as separate `feature/*` modules. Each one brings its own
tables, following the same pattern as the core ones (owner column, tombstone,
RLS) and contributes widgets to the dashboard through a contract defined in the
core. The shared timeline, goals and categories are the connection points
between modules. The exact contract is *Open* and will be designed when the
dashboard is built.

## Dependency injection

Hilt (with KSP), constructor injection, no service locators or manual construction
in ViewModels. Each module that owns an implementation also owns its Hilt module:

- `core:database`: `LifeOSDatabase` (singleton) and the DAOs.
- `core:data`: binds `CategoryRepository` to its implementation and provides `Clock`
  (injected so time-dependent logic can be tested).
- `core:datastore`: the preferences `DataStore` (singleton) and the binding of
  `UserPreferencesRepository`.
- `app`: `LifeOSApplication` (`@HiltAndroidApp`), `MainActivity` (`@AndroidEntryPoint`)
  and `MainViewModel` (`@HiltViewModel`).

Implementations are `internal` to their module; only the domain interface is visible
to the rest of the app.

## Configuration

The Supabase URL and public key reach the app as `SupabaseConfig` (`core:network`), a
class that rejects blank values and hides the key in its string form. `app` builds it
from `BuildConfig` and provides it through Hilt (`AppModule`); the network layer never
reads build properties itself. How the values are resolved, for debug and release, is
described in [DECISIONS.md](DECISIONS.md), D-20. `NetworkModule` uses it to create the
Supabase client.

## Authentication

Layers, from the inside out:

- **`core:common`:** `Outcome<T, E>` (`Success` or `Failure`), the way expected failures are
  returned instead of thrown.
- **`core:domain`:** `AuthRepository` (`authState`, `signUp`, `signIn`, `signOut`,
  `requestPasswordReset`), `AuthState` (`Loading`, `SignedOut`, `SignedIn(user)`), `AuthUser`
  (its `id` is the owner stored on every user-owned record), `SignUpResult` (whether the email
  must be confirmed) and the sealed `AuthError`.
- **`core:network`:** `AuthRemoteDataSource` and its Supabase implementation, the only place that
  knows `supabase-kt`. It turns library, HTTP and I/O errors into `RemoteAuthException`s through a
  pure, tested classifier, and exposes the session state as a `Flow`. The Supabase client, the
  `Auth` plugin and the data source are provided by `NetworkModule`.
- **`core:data`:** `AuthRepositoryImpl` maps remote states and exceptions to domain ones.

Session handling is described in [DECISIONS.md](DECISIONS.md), D-21: an encrypted file in
`noBackupFilesDir` (`EncryptedSessionManager` with a Keystore-backed `SessionCipher`), a user
who stays signed in offline, and a session that cannot be read is discarded.

### Sign-up and sign-in (step 1.2)

- **Rules and use cases** (`core:domain`): `AuthValidation` holds the input rules (a
  well-formed email, a password of at least 8 characters with no composition rules and never
  trimmed, an optional display name of up to 100 characters). `SignInUseCase` and
  `SignUpUseCase` check them before anything is sent and return `AuthFailure`: either
  `InvalidInput` (which fields, and why) or `Rejected(AuthError)`. Signing in only checks that
  the password is not empty, because a password set earlier may predate the current rules. The
  minimum length must be set to the same value in the Supabase dashboard.
- **Display name:** optional at sign-up and stored in the user's metadata (`full_name`).
- **`feature:auth`:** the first feature module, built with the `lifeos.android.feature`
  convention plugin. Sign-in and sign-up each have a `ViewModel` with an immutable UI state and
  an event type, and a screen split into a thin stateful part and a stateless `Content` that is
  previewed and tested alone. States covered: idle, submitting (the button shows progress and
  the form is disabled), field problems, an inline error banner announced by screen readers,
  and, after a sign-up that needs it, a "check your email" screen. `AuthNavHost` holds the two
  destinations.
- **The gate** (`app`): `SessionViewModel` exposes `AuthState`; `LifeOSRoot` cross-fades between
  a loading state, the auth flow and the main content (`MainContent`, the bottom bar and the
  tabs). When sign-in or sign-up succeeds the state changes and the screen is replaced; the
  screens never navigate away themselves. With a stored session the app opens signed in even
  without a connection.

Not done yet: email confirmation and password recovery links (step 1.2b) and Google sign-in
(step 1.3). The email confirmation works on the server (the link confirms the account) but the app does not yet
handle the link, so the redirect lands in the browser.

## Profile and account

- **`feature:profile`:** one screen reached from the "More" tab (which lists only the sections
  that exist). It shows the email, edits the display name, chooses the theme (system, light,
  dark, and wallpaper colors from Android 12) and offers sign-out and account deletion. It has
  the same shape as the auth screens: a `ViewModel` combining the account state, the saved
  preferences and what only the screen knows (an unsaved name, the dialog, an error), and a
  stateless `Content`.
- **Display name:** changed with `auth.updateUser` on the user's metadata (`full_name`), so the new
  value reaches every screen through `AuthState`. The `profiles` table is not written; it is
  reconciled by the synchronization phase.
- **Account deletion:** `DeleteAccountUseCase` calls the server function `delete_my_account`
  (through `postgrest-kt`), then removes the local rows of that owner, then signs out. Nothing
  local is touched until the server has confirmed, because the local data may be the only copy.
  Every table with user-owned rows must be added to `UserDataRepository.deleteAllFor`. There is no
  avatar yet, so no files need deleting first.
- **Sign-out works offline:** if the server cannot be told, the local session is still removed.
- New design system pieces: top bar, confirmation dialog, radio and switch rows.

## Tasks (steps 1.5a and 1.5b)

- **Domain** (`core:domain`): `Task` checks the same rules as the backend (title of 1 to 300
  characters, positive duration) and one more: it has a completion time exactly when it is
  completed. `DueDate` is either `AllDay(day)` or `At(instant)`; `Task.isOverdue` is the single
  place that decides what overdue means (open tasks only; a day is overdue once it is over in
  the person's time zone). `TaskRepository` returns `Flow`s.
- **Use cases:** `SaveTaskUseCase` validates the form input (it reports every problem at once) and
  creates or edits, keeping what the person cannot change (owner, creation time, category and an
  earlier completion time); `SetTaskCompletedUseCase` completes or reopens. Listing and deleting go
  through the repository, because a use case there would add nothing.
- **Data:** `TaskEntity` and `TaskDao` in `core:database` (database version 2, see
  [DATABASE.md](DATABASE.md)), `TaskRepositoryImpl` in `core:data` with the same local-first
  behavior as categories. An all-day due date is stored as midnight UTC. `UserDataRepository` now
  clears the tasks of the owner too.
- **`feature:tasks`:** `TasksScreen` (the Tasks tab: list with loading, empty, error with retry
  and content states, a checkbox to complete, overdue marked in words as well as color, a button
  to add) and `TaskEditorScreen` (a form with validation messages, priority and status chips, a
  date picker and an optional time picker, delete with confirmation). The tab itself and the
  editor route are wired in `app`'s navigation graph: the editor is a destination without bottom
  bar. The editor knows its task from the navigation argument and closes itself once it has
  saved or deleted.
- **Design system:** `LifeOSChoiceChip`, `LifeOSDatePickerDialog`, `LifeOSTimePickerDialog` and
  `LifeOSAddButton`. Material hides the label of an extended floating button from screen readers,
  so the add button repeats it as its description.
- **Time zones:** dates are shown and picked in the device's current time zone; an all-day date has
  no time zone. Dates use `java.time` through `atZone`, not `LocalDate.ofInstant`, which needs API 34
  (minSdk is 29) and which lint does not check in the pure Kotlin modules.

### Views, sorting, filters and categories (step 1.5b)

- **The query** (`core:domain`): `TaskListQuery` holds a `TaskView` (`ALL`, `TODAY`, `UPCOMING`,
  `OVERDUE`, `COMPLETED`), a `TaskFilter` (priorities, one category), a `TaskSort` (field and
  direction) and whether to group by priority. `apply(tasks, now, zone)` is a pure function that
  returns groups, so it is tested with fixed clocks and several time zones. It works on the
  tasks Room already emits; there are no per-view SQL queries.
- **View rules:** `TODAY` is open tasks due today that are not late yet, `UPCOMING` is open tasks due
  from tomorrow, `OVERDUE` is `Task.isOverdue`, `COMPLETED` is the completed status only, `ALL` is
  everything. A moment already passed today is overdue, not today, so the views other than `ALL` never
  overlap. Sorting puts finished tasks after open ones and undated tasks after dated ones in both
  directions; a whole-day due date counts as the end of that day. Equal tasks are ordered by creation
  time and id, so refreshes do not shuffle them.
- **State:** the chosen query lives in `TasksViewModel` and is not saved: it is reset when the process
  ends (decision D-25). A view or filter that matches nothing keeps the content state, with a message
  and a reset button; the empty state is only for having no tasks at all.
- **Screen:** view chips above the list and a "Filter and sort" bottom sheet (sort field and direction,
  group by priority, priorities, category). The button says when something is on.
- **Categories:** `CreateTaskCategoryUseCase` creates a category of type task, refusing blank or long
  names and a name already used by the person for tasks, ignoring case (the backend has the same unique
  rule among non-deleted rows). The task editor offers the categories as chips with a "None" choice and a
  "New category" dialog (name and one of eight colors from `CategoryColors`). The new category is
  selected at once. The color is only a visual aid: names are always written out. The list shows the
  category of each task and can filter by it. There is no screen to rename or delete categories yet.
- **Design system:** `LifeOSBottomSheet`, `LifeOSFormDialog`, `LifeOSColorSwatch`, `LifeOSColorDot`, the
  `CategoryColors` palette, and a color marker option on `LifeOSChoiceChip`.

Not done yet: managing categories (rename, delete), projects, tags, reminders, recurrence, search.
Database errors on save are not turned into a screen state: they would
surface as a crash, as for categories.

## Persistence conventions

`Category` is the first end-to-end example and the template for the other synchronized
entities.

- **Domain** (`core:domain`): `Category` is an immutable data class that checks the same
  rules as the backend constraints, and `CategoryRepository` is an interface returning
  `Flow`. No persistence detail (tombstones, sync state) appears in the domain model.
- **Room** (`core:database`): `CategoryEntity` carries owner, timestamps in epoch
  milliseconds, `deletedAt` (tombstone), `syncStatus` and `remoteUpdatedAt` (see
  [DATABASE.md](DATABASE.md)). The DAO returns only non-deleted rows. The schema is exported
  to `core/database/schemas` and committed; destructive migration is not enabled.
- **Repository** (`core:data`): writes only to Room and marks the row for upload.
  Deleting a row that never reached the server removes it; deleting a synced row leaves
  a tombstone. Mappers are `internal` extension functions.
- **Preferences**: `UserPreferences` (theme mode and dynamic color) in DataStore. A read
  failure falls back to the defaults, and a corrupted file is reset, because these are
  cosmetic preferences. `MainViewModel` exposes them and `MainActivity` applies them to the
  theme, including the system bar icons.

There is still no screen to change the theme, no use case, and no synchronization:
`categories` is not used by any feature yet.

## Concurrency and errors

Coroutines and Flow with structured concurrency and proper cancellation.
Expected failures (network, authentication, validation, database, unexpected)
are modelled explicitly and translated into user messages at the presentation
layer.

## Build

Version catalog in `gradle/libs.versions.toml`. Convention plugins in
`build-logic/convention` centralize Android and Kotlin configuration:

| Plugin | Purpose |
|---|---|
| `lifeos.android.application` | SDK levels (from the catalog), Java level, test runner; applies `lifeos.quality` |
| `lifeos.android.library` | Same as above for Android library modules |
| `lifeos.kotlin.library` | Pure Kotlin (JVM) modules, Java 11 target |
| `lifeos.android.compose` | Compose compiler plugin, `buildFeatures.compose`, Compose BOM (application and library modules) |
| `lifeos.android.hilt` | KSP, the Hilt plugin, `hilt-android` and the Hilt compiler |
| `lifeos.android.room` | KSP, Room runtime and compiler, schema export to `schemas/` |
| `lifeos.android.environment` | Supabase URL and public key as `BuildConfig` fields (debug and release), from `local.properties` or environment variables; fails a release build without real values |
| `lifeos.android.feature` | A feature module: library, Compose, Hilt, serialization routes, navigation and lifecycle dependencies, test libraries; depends only on `core` modules |
| `lifeos.quality` | ktlint and detekt configuration |

The project uses AGP 9.x, which bundles Kotlin support, so `kapt` is not used and
annotation processing goes through KSP. Hilt 2.60.1, KSP 2.3.13 and Room 2.8.5 were
verified together with this setup (AGP 9.5.0, Kotlin 2.2.x): the app builds with the full
Hilt graph and Room generates its code and schema. An open KSP bug affects Room entities
annotated with `@Parcelize` under AGP 9; entities here are never `Parcelable`.

## Tooling

- ktlint 1.8.0 through `ktlint-gradle` (formatting), rules in `.editorconfig`.
- detekt 2.0.0-alpha.6 (static analysis), rules in `config/detekt/detekt.yml`.
  It is a pre-release, see [DECISIONS.md](DECISIONS.md).
- Tests: JUnit, MockK, Turbine and `kotlinx-coroutines-test` for JVM tests; Compose UI
  tests and Room tests with an in-memory database run on a device or emulator
  (`androidTest`). Repository tests use hand-written in-memory fakes rather than mocks
  where behavior matters. Tests must not depend on the operating system: DataStore
  replaces its file by renaming a temporary one, which fails on Windows JVMs, so the
  preferences tests use an in-memory `DataStore`.
- CI: GitHub Actions, one job on `ubuntu-latest` (JDK 25, Gradle cache). It runs
  formatting and static analysis, JVM unit tests, Android lint, the debug build and the
  compilation of the instrumented tests. Instrumented tests are not run in CI yet.
