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

The repository contains the build foundation (convention plugins, ktlint, detekt),
the `core/*` modules, the design system, the navigation shell, the persistence and
dependency injection foundation (Hilt, Room, DataStore) and the CI workflow
(GitHub Actions). Every navigation destination is a placeholder: no product feature
exists yet. The Supabase development project exists with its schema applied. The app
version is `0.1.0` (`versionCode` 1).

## Phase 0 - Foundation (target `0.1.0`)

| Step | Branch | Scope | Status |
|---|---|---|---|
| 0.0 | - | Read-only inspection of the project | done |
| 0.1 | `docs/project-docs` | README, CLAUDE.md, docs, Supabase SQL scripts | done |
| 0.2 | `chore/build-foundation` | Version catalog, convention plugins (application, Compose, quality), detekt, ktlint, `.editorconfig`, `.gitattributes` | done |
| 0.3 | `feature/modularization` | Nine `core/*` modules (empty), library convention plugins, dependency graph. `feature/*` modules are created later, one per feature | done |
| 0.4 | `feature/design-system` | Tokens, Material 3 theme, typography, base components | done |
| 0.5 | `feature/navigation` | Navigation Compose, bottom bar (5 tabs), deep links, placeholder destinations | done |
| 0.6 | `feature/hilt-room-datastore` | Hilt, Room with the first entity (`categories`) as reference, DataStore with the theme preference, test infrastructure | done |
| 0.7 | `chore/ci` | GitHub Actions: formatting, static analysis, unit tests, lint, build | done |

Phase 0 is complete and released as `0.1.0`: a buildable, tested foundation with no
product feature.

## Phase 1 - Core application (target `0.2.0`)

Authentication, profile, tasks, projects, dashboard (last, because it
aggregates the others). One branch per feature. Data stays local until Phase 3: the
account identifies the user but nothing is synchronized yet.

| Step | Branch | Scope | Status |
|---|---|---|---|
| 1.0 | `chore/environment-config` | Development and production configuration (Supabase URL and public key from `local.properties`, fallbacks for CI) | planned |
| 1.1 | `feature/auth-foundation` | Supabase client, `AuthRepository`, persistent session, current user available to the app; no screens | planned |
| 1.2 | `feature/authentication` | `feature/auth` module: sign up, sign in, password recovery, validation, navigation gate | planned |
| 1.3 | `feature/google-sign-in` | Google Sign-In (needs configuration on Google Cloud) | planned |
| 1.4 | `feature/profile` | Profile, avatar, theme preference screen, sign out, account deletion | planned |
| 1.5 | `feature/task-management` | Tasks: domain, Room tables, use cases, create and edit, views, filters and sorting (likely split in two branches) | planned |
| 1.6 | `feature/project-management` | Projects, progress computed from tasks, task and project association | planned |
| 1.7 | `feature/dashboard` | Overview of what exists so far | planned |

Out of Phase 1: recurring tasks and reminders (Phase 2), synchronization (Phase 3).

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
preparation. There is no early test track on Google Play: store preparation happens
here, at the end (see [DECISIONS.md](DECISIONS.md), D-11).

The scope of `1.0.0` is confirmed: specification phases 0 to 4 (including statistics)
plus production readiness (D-08).

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
