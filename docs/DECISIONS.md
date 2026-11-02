# Decisions

Each decision has a status: **Accepted** (agreed with the project owner),
**Proposed** (recommended, awaiting approval) or **Open** (not discussed in
enough detail). Accepted decisions prevail over the original specification.

## Accepted

### D-01 Branch model

`master` is the stable branch (kept as `master`, not renamed to `main`) and
`develop` is the integration branch. Each step lives in its own short-lived
branch created from `develop`: `feature/*`, `fix/*`, `refactor/*`, `chore/*`,
`docs/*`.

### D-02 Merging

Branches are merged with a merge commit (`--no-ff`). Squash and rebase merges
are not used, so the branch of each feature stays visible in the history.
Feature branches are merged into `develop` through pull requests; `develop` is
merged into `master` at the end of a phase and tagged. History is never
rewritten.

**Consequence:** commits inside a branch must be clean and well described,
because they remain in the history.

### D-03 Commits

Conventional Commits with a scope; atomic commits. Commit, push and merge only
when the project owner asks.

### D-04 Versioning

Semantic versioning, `0.x` until the core application is complete. Version
`1.0.0` is the public release of the core; every later module raises the minor
version. See [ROADMAP.md](ROADMAP.md).

### D-05 Product direction

LifeOS is a portfolio project that must also be useful and published on the
Play Store. The core application defined in the original specification comes
first; further life areas (finance, work, sport, nutrition, hobbies, personal
journal) are added afterwards as independent modules, one at a time.

### D-06 Backend

Supabase is the backend. A development project (`lifeos-dev`) exists. The
schema (`001_schema.sql`) was applied; its design choices are documented in
[DATABASE.md](DATABASE.md): relational model with composite same-owner foreign
keys, RLS on every table, tombstones, server-side `updated_at`, `check`
constraints instead of enums.

### D-07 Synchronization push

Push uses a generic RPC, `push_rows`, which receives complete rows with
`base_updated_at` and never overwrites a newer server version (approach "A"
chosen over table triggers). The initial conflict policy is last-write-wins on
server timestamps with that safeguard. See [SYNC.md](SYNC.md).

### D-13 Static analysis and formatting

ktlint (through `ktlint-gradle` 14.2.0, ktlint 1.8.0) and detekt are configured
through the `lifeos.quality` convention plugin. detekt is on
`2.0.0-alpha.6`, a **pre-release**: the stable 1.23.8 is built for Kotlin 2.0.21
and AGP 8.8 and has no AGP 9 support, while the 2.x line is the one targeting
AGP 9. The version is pinned and runs correctly on this project (AGP 9.5.0,
Kotlin 2.2.10). It will be moved to the first stable 2.x release. If it becomes
a problem, detekt can be removed without affecting the rest of the build.

### D-14 Convention plugin scope

Convention plugins live in `build-logic/convention`. Only what the current
single module needs exists (application, Compose, quality); library and
pure-Kotlin plugins are added with the modularization step, to avoid
speculative code.

### D-10 Modularization

The nine `core/*` modules are created in Phase 0, empty but compiling, to fix
the boundaries from the start. `feature/*` modules are created one at a time,
when the feature is built, because empty feature modules bring no benefit and
cost build time. `core:common` and `core:domain` are pure Kotlin (JVM) modules
with no Android dependency; all other core modules are Android libraries.

### D-15 Visual identity

Direction "Calma": deep indigo primary with a teal accent and cool neutral
surfaces, so that color is reserved for state (priority, deadlines, progress)
rather than decoration. The system font is used for now; a bundled font can be
introduced later. The LifeOS palette is always used by default; dynamic color
is only an opt-in option. Details in [ARCHITECTURE.md](ARCHITECTURE.md).

### D-16 Navigation

Navigation Compose 2.10.2 with type-safe routes (Kotlin Serialization), chosen
over Navigation 3 because the specification asks for nested graphs and deep
links and Navigation Compose is the more mature option for them. The bottom
bar has five tabs (Home, Tasks, Calendar, Habits, More); *More* is the hub for
Projects, Goals, Statistics, Profile and the future modules. Icons come from
`material-icons-extended`, whose unused icons are removed by R8 in release builds.
Destinations without a feature yet show an explicit placeholder. See
[ARCHITECTURE.md](ARCHITECTURE.md).

### D-17 Dependency injection and persistence foundation

Hilt 2.60.1 with KSP 2.3.13, Room 2.8.5 and DataStore Preferences 1.3.0, plus
coroutines 1.11.0, MockK 1.14.11 and Turbine 1.2.1 for tests. Room 3 is still alpha
and is not used. The foundation is demonstrated end to end with two real but minimal
pieces: the `categories` table (reference for the conventions of every synchronized
entity: owner, tombstone, local sync state, exact server timestamp) and the theme
preference. `core:database` and `core:network` do not depend on the domain; mapping
happens in `core:data`. Details in [ARCHITECTURE.md](ARCHITECTURE.md) and
[DATABASE.md](DATABASE.md). `androidx.lifecycle:lifecycle-runtime-compose`
(same version as the other lifecycle artifacts) was added for lifecycle-aware state
collection, as the specification requires, and `androidx.test:runner` 1.7.0 for the
instrumented tests of library modules (the app gets it through Espresso). The Room
tests were run on an emulator (Pixel 3a, API 31): 5 of 5 passed.

### D-18 Continuous integration

GitHub Actions, a single job on `ubuntu-latest` so Gradle's cache is used as well as
possible, running on pull requests and on pushes to `develop` and `master`: formatting
and static analysis, JVM unit tests, Android lint, the debug build and the compilation of
the instrumented tests. Actions are pinned by major version (for example `@v7`), not by
commit hash; hashes can be adopted later if the supply chain becomes a concern.
Permissions are read-only and no secrets are used yet. Instrumented tests (emulator) are
not run in CI for now. The workflow could not be run before pushing; the `gradlew` file
mode was fixed to executable, because it had been created on Windows without the bit.
Requiring a green CI before merging is a repository setting on GitHub, left to the
project owner.

## Proposed

### D-08 Scope of version 1.0.0

Proposal: `1.0.0` contains foundation, core (authentication, profile,
dashboard, tasks, projects), personal productivity (habits, goals, calendar,
notifications, recurring tasks), backend and synchronization, statistics and
production readiness. Specification phase 5 and the life modules come after.
Alternative: ship statistics as `1.1.0` to reach the release earlier.

### D-09 Authentication

Proposal: use Supabase Auth from Phase 1, keeping data local in Room until the
synchronization phase. Alternative: a local implementation behind a domain
interface in Phase 1 and Supabase in Phase 3.

### D-11 Early closed test on Google Play

Proposal: publish an internal or closed test after Phase 1 or 2 to validate
signing, privacy requirements and CI early.

### D-12 AI suggestions

Proposal: introduce them only after the data exists and the backend is stable;
calls go through a server-side proxy (for example a Supabase Edge Function), the
OpenAI key never ships in the app, the user gives explicit consent and can turn
the feature off, only aggregated data is sent, and per-user quotas apply.

## Open

- Package layout inside the modules (base package is `it.edon.lifeos`).
- Whether Retrofit is needed, given that the Supabase Kotlin client has its own
  networking.
- User-facing behavior on a sync conflict (re-apply or discard).
- Whether `allowBackup` should stay enabled given the personal data stored.
- Contract between life modules and the dashboard.
- Pricing and quotas, if AI suggestions are introduced.
- The assumptions listed in [DATABASE.md](DATABASE.md) (goal status values,
  habit frequency, event types, recurrence columns, `goals.progress`).
