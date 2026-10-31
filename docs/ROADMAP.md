# Roadmap

Status legend: `done`, `in progress`, `planned`. Nothing is marked done unless
it exists in the repository and was verified.

## Versioning

Semantic versioning. Versions are `0.x` until the core application is ready for
the public (`1.0.0`). After that, every new module raises the minor version
(`1.1.0`, `1.2.0`, ...), fixes raise the patch, and the major version is
reserved for breaking changes (for example an incompatible data migration).
Tags are created on `master` only, at the end of each phase.

Android also needs a monotonically increasing `versionCode`, managed in the
Gradle configuration.

## Current state

The repository contains the Empty Activity template, the build foundation
(convention plugins, ktlint, detekt), the `core/*` modules and, in progress,
the design system in `core:designsystem`. No product feature exists yet. The Supabase development project exists with its schema applied.

## Phase 0 - Foundation (target `0.1.0`)

| Step | Branch | Scope | Status |
|---|---|---|---|
| 0.0 | - | Read-only inspection of the project | done |
| 0.1 | `docs/project-docs` | README, CLAUDE.md, docs, Supabase SQL scripts | done |
| 0.2 | `chore/build-foundation` | Version catalog, convention plugins (application, Compose, quality), detekt, ktlint, `.editorconfig`, `.gitattributes` | done |
| 0.3 | `feature/modularization` | Nine `core/*` modules (empty), library convention plugins, dependency graph. `feature/*` modules are created later, one per feature | done |
| 0.4 | `feature/design-system` | Tokens, Material 3 theme, typography, base components | in progress |
| 0.5 | `feature/navigation` | Navigation Compose, bottom bar, placeholder destinations | planned |
| 0.6 | `feature/hilt-room-datastore` | Hilt, Room base, DataStore, test infrastructure | planned |
| 0.7 | `chore/ci` | GitHub Actions: build, test, lint | planned |

## Phase 1 - Core application (target `0.2.0`)

Authentication, profile, tasks, projects, dashboard (last, because it
aggregates the others). One branch per feature.

## Phase 2 - Personal productivity (target `0.3.0`)

Habits, goals, calendar, notifications, recurring tasks, advanced filtering.

## Phase 3 - Backend and synchronization (target `0.4.0`)

Remote data sources, repository synchronization, offline-first behavior,
conflict handling, background synchronization. The backend schema and
`push_rows` already exist; the client side does not.

## Phase 4 - Analytics (target `0.5.0`)

Productivity statistics, weekly and monthly reports, habit analytics, project
and goal progress, Vico charts. Every metric has a documented calculation.

## Production readiness (release `1.0.0`)

Comprehensive testing, accessibility, performance profiling, security review,
release configuration, app signing, privacy documentation and Play Store
preparation. An early closed test on Google Play after Phase 1 or 2 is
recommended, to validate signing, privacy and CI while the project is small.

The scope of `1.0.0` (specification phases 0 to 4 plus production readiness) is
awaiting confirmation; see [DECISIONS.md](DECISIONS.md).

## After 1.0.0

- **Life modules**, one per minor version, order to be decided: personal
  journal, sport, finance, work, hobbies, nutrition. Each one is an independent
  `feature/*` module connected to the core (dashboard, calendar, goals).
- **Distinctive features** to evaluate: guided daily planning, goals updated
  automatically from linked tasks and habits, weekly review.
- **AI-based suggestions** through a server-side proxy (never an API key inside
  the app), with explicit user consent, per-user limits and a privacy policy.
  Not decided yet.
- Advanced features from the specification (widgets, calendar integrations,
  attachments, export and import, cloud backups, ...).
