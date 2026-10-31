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

### D-10 Modularization

Proposal: create all Gradle modules in Phase 0, empty but compiling, to fix the
boundaries from the start. Alternative: create them as features are built.

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
- Compatibility of Hilt, Room and KSP with AGP 9.5 and Kotlin 2.2.10 (to be
  verified in step 0.2 against official documentation).
- User-facing behavior on a sync conflict (re-apply or discard).
- Whether `allowBackup` should stay enabled given the personal data stored.
- Contract between life modules and the dashboard.
- Pricing and quotas, if AI suggestions are introduced.
- The assumptions listed in [DATABASE.md](DATABASE.md) (goal status values,
  habit frequency, event types, recurrence columns, `goals.progress`).
