# Architecture

> **Status: proposal.** The project is still a single-module Empty Activity
> template. This document describes the target architecture. Items marked
> *Open* need the project owner's approval (see [DECISIONS.md](DECISIONS.md)).

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
`network` and `ui` are still empty. `feature/*` modules do not exist yet: each one
is created when its feature is built. `core:common` and `core:domain` are pure
Kotlin (JVM) modules; the others are Android libraries (`designsystem`, `ui` and
`navigation` with Compose). Target layout:

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
- `core/domain` depends only on `core/common`, and is pure Kotlin.
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
