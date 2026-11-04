# LifeOS

[![CI](https://github.com/Pedoni/LifeOS/actions/workflows/ci.yml/badge.svg)](https://github.com/Pedoni/LifeOS/actions/workflows/ci.yml)

LifeOS is an Android app that brings the different areas of everyday life into
a single place: tasks, projects, habits, goals, calendar and statistics. It is
designed as a personal command center rather than a plain task manager, with an
offline-first architecture and a Supabase backend.

> **Status: version 0.1.0, foundation only.** The technical base is in place:
> a modular Clean Architecture project, a design system, bottom-bar navigation,
> dependency injection (Hilt), local persistence (Room and DataStore), the
> backend schema on Supabase and a CI pipeline. On top of that, sign-up,
> sign-in, a profile screen and the basics of task management (create, edit,
> complete, delete) exist; **no project or other product feature is
> implemented yet** and the remaining screens are placeholders. Next in the core
> application are task filters and views, projects and the dashboard. See
> [docs/ROADMAP.md](docs/ROADMAP.md) for the real state of each phase.

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
| Navigation | Navigation Compose 2.10.2 with type-safe routes (Kotlin Serialization plugin) |
| Dependency injection | Hilt 2.60.1 with KSP 2.3.13 |
| Persistence | Room 2.8.5, DataStore Preferences 1.3.0 |
| Backend client | supabase-kt 3.6.0 (Auth, Postgrest) on Ktor 3.4.3 with OkHttp; used for sign-in, profile and account deletion |
| Async | Kotlin Coroutines 1.11.0 and Flow |
| Tests | JUnit, MockK 1.14.11, Turbine 1.2.1, coroutines-test, Compose UI testing |
| Build logic | Gradle convention plugins in [`build-logic`](build-logic) |
| Code quality | ktlint 1.8.0, detekt 2.0.0-alpha.6 (pre-release, see [DECISIONS.md](docs/DECISIONS.md)) |
| CI | GitHub Actions: lint, static analysis, unit tests, Android lint and build on every pull request |
| Backend | Supabase project created, schema in [`supabase/sql`](supabase/sql) |

Retrofit, WorkManager, Coil, Vico and Paging are planned and **not yet added**. The Kotlin Serialization plugin is used only for
navigation routes so far.

## Repository layout

```
app/              Android application module (navigation shell, placeholder screens)
core/             Shared modules: common, domain, data, database, network,
                  datastore, designsystem, ui, navigation (common, network
                  and ui are still empty)
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

The backend runs on Supabase. To set up a project, create it in the Supabase
dashboard and run, in order, in the SQL Editor:

1. [`supabase/sql/001_schema.sql`](supabase/sql/001_schema.sql)
2. [`supabase/sql/002_functions.sql`](supabase/sql/002_functions.sql)
3. [`supabase/sql/003_task_due_all_day.sql`](supabase/sql/003_task_due_all_day.sql)

The app reads the project URL and the public (anon or publishable) key at build time from
`local.properties`, which is ignored by Git:

```
supabase.url=https://<project>.supabase.co
supabase.anonKey=<public anon or publishable key>

# Optional: used by debug builds only, to point at another project
supabase.dev.url=
supabase.dev.anonKey=
```

The same values can be given as the environment variables `SUPABASE_URL`,
`SUPABASE_ANON_KEY`, `SUPABASE_DEV_URL` and `SUPABASE_DEV_ANON_KEY` (for CI).
A debug build without them still compiles but cannot reach a backend; a release
build without real values fails. Never commit credentials, and never put a
`service_role` key in the app. See [docs/DECISIONS.md](docs/DECISIONS.md), D-20.

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
