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

The nine `core/*` modules exist (empty, compiling) and `:app` depends on all of
them. `feature/*` modules do not exist yet: each one is created when its
feature is built. `core:common` and `core:domain` are pure Kotlin (JVM)
modules; the others are Android libraries (`designsystem`, `ui` and
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
- `core/database`, `core/network` and `core/datastore` depend on `core/domain`.
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

## Extensibility: life modules

After version 1.0.0 new areas (finance, sport, nutrition, journal, work,
hobbies) are added as separate `feature/*` modules. Each one brings its own
tables, following the same pattern as the core ones (owner column, tombstone,
RLS) and contributes widgets to the dashboard through a contract defined in the
core. The shared timeline, goals and categories are the connection points
between modules. The exact contract is *Open* and will be designed when the
dashboard is built.

## Dependency injection

Hilt, with constructor injection and Hilt modules for interface bindings and
for third-party objects (Room, Supabase client). Appropriate scopes; no service
locators or manual construction in ViewModels.

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
| `lifeos.android.compose` | Compose compiler plugin, `buildFeatures.compose`, Compose BOM (application and library modules) |
| `lifeos.quality` | ktlint and detekt configuration |

Library and pure-Kotlin convention plugins will be added together with the
modules that need them.

The project uses AGP 9.x, which bundles Kotlin support. From research (not yet
exercised in this project): KSP must be 2.3.6 or newer for AGP 9 and `kapt` is
not supported; Hilt supports AGP 9 from 2.59. Hilt, Room and KSP must be
verified with a real build when they are introduced.

## Tooling

- ktlint 1.8.0 through `ktlint-gradle` (formatting), rules in `.editorconfig`.
- detekt 2.0.0-alpha.6 (static analysis), rules in `config/detekt/detekt.yml`.
  It is a pre-release, see [DECISIONS.md](DECISIONS.md).
- Planned: GitHub Actions for CI, MockK, Turbine and Compose UI testing.
