# LifeOS

LifeOS is an Android app that brings the different areas of everyday life into
a single place: tasks, projects, habits, goals, calendar and statistics. It is
designed as a personal command center rather than a plain task manager, with an
offline-first architecture and a Supabase backend.

> **Status: early development.** The repository currently contains the Android
> Studio Empty Activity template (Jetpack Compose) plus the project
> documentation and the backend SQL scripts. No product feature is implemented
> yet. See [docs/ROADMAP.md](docs/ROADMAP.md) for the real state of each phase.

## Goals

- A coherent app where everything is connected: tasks belong to projects,
  projects feed goals, tasks and habits appear in the calendar.
- A serious implementation of Clean Architecture, offline-first persistence and
  reliable synchronization.
- A release on the Google Play Store.

After version 1.0.0 (the core application) further life areas will be added as
independent modules, such as finance, sport, nutrition and a personal journal.

## Technology

The target stack is defined in [docs/PRODUCT_SPEC.md](docs/PRODUCT_SPEC.md).
What the project actually uses today:

| Area | Current |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2026.02.01), Material 3 |
| Build | Android Gradle Plugin 9.5.0, Gradle 9.6.0 |
| Android | `minSdk` 29, `targetSdk` / `compileSdk` 37 |
| Build logic | Gradle convention plugins in [`build-logic`](build-logic) |
| Code quality | ktlint 1.8.0, detekt 2.0.0-alpha.6 (pre-release, see [DECISIONS.md](docs/DECISIONS.md)) |
| Backend | Supabase project created, schema in [`supabase/sql`](supabase/sql) |

Hilt, Room, DataStore, Navigation Compose, Coroutines, Retrofit/OkHttp, Kotlin
Serialization, WorkManager, Coil, Vico, MockK and Turbine are planned and
**not yet added**.

## Repository layout

```
app/              Android application module (template, to be modularized)
build-logic/      Gradle convention plugins
config/detekt/    detekt rules
docs/             Product specification, architecture, database, sync, roadmap, decisions
supabase/sql/     Backend schema and RPC functions
gradle/           Version catalog and wrapper
```

## Getting started

Requirements: Android Studio with a recent JDK (the Gradle daemon is
configured for toolchain 25) and an Android SDK with API 37.

```
./gradlew assembleDebug     # build the debug APK
./gradlew test              # JVM unit tests
./gradlew ktlintCheck detekt  # formatting and static analysis
```

On Windows use `gradlew.bat`.

### Backend

The backend runs on Supabase. To set up a development project, create it in
the Supabase dashboard and run, in order, in the SQL Editor:

1. [`supabase/sql/001_schema.sql`](supabase/sql/001_schema.sql)
2. [`supabase/sql/002_functions.sql`](supabase/sql/002_functions.sql)

The app will read the project URL and the public (anon) key from
`local.properties`, which is ignored by Git. This configuration is not
implemented yet. Never commit credentials, and never put a `service_role` key
in the app.

## Workflow

- Branches: `master` (stable, tagged), `develop` (integration), and one
  short-lived branch per step: `feature/*`, `fix/*`, `refactor/*`, `chore/*`,
  `docs/*`.
- Commits follow [Conventional Commits](https://www.conventionalcommits.org/).
- Branches are merged with a merge commit (`--no-ff`), never squashed, so the
  history of every feature stays visible.
- Versioning follows semantic versioning: `0.x` until the core application
  reaches `1.0.0`, then one minor version per new module.

Details in [docs/DECISIONS.md](docs/DECISIONS.md).

## Documentation

| File | Content |
|---|---|
| [docs/PRODUCT_SPEC.md](docs/PRODUCT_SPEC.md) | Original product and technical specification |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Layers, modules and dependency rules |
| [docs/DATABASE.md](docs/DATABASE.md) | Entities, relationships, security |
| [docs/SYNC.md](docs/SYNC.md) | Offline-first flow and conflict resolution |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Phases, versions and feature status |
| [docs/DECISIONS.md](docs/DECISIONS.md) | Decisions and open questions |
| [CLAUDE.md](CLAUDE.md) | Permanent instructions for Claude Code |
