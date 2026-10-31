# CLAUDE.md

Permanent instructions for Claude Code on the LifeOS project. The full
specification is in `docs/PRODUCT_SPEC.md`; decisions taken afterwards are in
`docs/DECISIONS.md` and prevail over the specification where they differ.

## Project

LifeOS is a personal productivity Android app (Kotlin, Jetpack Compose,
Clean Architecture + MVVM + UDF, Room, Hilt, Supabase). It is both a portfolio
project and a product to publish on Google Play.

- Application ID and namespace: `it.edon.lifeos` (do not change).
- Gradle modules: `:app` and nine `:core:*` modules (all empty for now).
  `feature/*` modules are created one at a time, when their feature is built
  (`docs/ARCHITECTURE.md`).
- The repository is an existing Android Studio project: preserve and evolve it,
  never recreate it.

## How to work

The project owner decides priorities, branches, dependencies and merges. You
are the implementation assistant.

For every step:

1. Inspect the existing code before proposing anything.
2. Propose the approach and the files that will change; wait for approval when
   the decision affects architecture, scope or dependencies.
3. Implement only the agreed scope.
4. Add or update tests, then run the relevant tests and a compilation check.
5. Report what changed, what was verified and what remains.

Never, without explicit permission:

- start a roadmap phase or step that was not authorized;
- create, switch or delete branches;
- commit, push, merge, rebase or rewrite history;
- add or upgrade dependencies;
- perform broad refactoring or touch unrelated modules;
- implement speculative functionality.

When requirements are ambiguous, ask. When several approaches exist, compare
them, recommend one and explain why it fits LifeOS.

Do not claim a feature exists because it is described in the specification.
Keep the documentation consistent with the actual implementation, and report
honestly what was and was not tested.

## Git conventions

- Branches: `master` (stable, tagged), `develop` (integration). Work happens in
  `feature/*`, `fix/*`, `refactor/*`, `chore/*`, `docs/*`, created from
  `develop`.
- Conventional Commits with a scope: `feat(tasks): ...`, `chore(gradle): ...`.
  Commits are atomic and focused; no "wip" or "fix again" commits.
- Merge with `--no-ff` (merge commit). Never squash. Pull requests target
  `develop`; `develop` is merged into `master` only at the end of a phase and
  tagged.
- Versions: `0.x` until the core application is complete (`1.0.0`), then one
  minor version per new module. Tags live on `master` only.

## Architecture rules

- Dependencies always point inward: presentation -> domain <- data.
- The domain layer is pure Kotlin: no Android, Room, Retrofit, Supabase or
  Compose.
- Data models (Room entities, DTOs) never leak into the domain layer; use
  mappers.
- ViewModels expose immutable UI state through `StateFlow`; screens collect it
  with lifecycle-aware collection. No business logic in Composables; no direct
  database access from ViewModels.
- Room is the source of truth for screens. The UI observes local `Flow`s;
  remote data updates the local database. Synchronization is an independent
  concern, never ad-hoc calls inside ViewModels (`docs/SYNC.md`).
- Hilt only, with constructor injection. No service locators.
- Every screen handles loading, success, empty and error states.
- Use cases must represent meaningful business operations; avoid trivial ones.
- Do not introduce abstractions that have no purpose.

## Code standards

- Official Kotlin conventions; immutable data; data classes for state and
  models; sealed interfaces for UI state and events.
- No hardcoded strings, colors or spacing in screens: use string resources and
  the design system tokens (`LifeOSTheme.colors`, `.spacing`, `.typography`,
  ...) and the components in `core:designsystem`.
- Represent expected failures explicitly (network, authentication, validation,
  database, unexpected). Do not swallow exceptions or add generic `catch`
  blocks without a recovery strategy. Never show stack traces to users.
- Never log passwords, tokens or private user content.
- Comments explain non-obvious behavior only.
- Use stable dependency versions, checked against official documentation. The
  build uses AGP 9.x, so plugin and library compatibility must be verified
  before adding KSP, Hilt or Room.

## Security

- Never commit secrets. Configuration lives in `local.properties` (ignored) and
  CI secrets.
- Only the Supabase public (anon) key may be in the client; never the
  `service_role` key. Access is protected by Row Level Security.
- Do not claim local database encryption unless it is implemented.

## Backend

SQL lives in `supabase/sql/` and is applied manually in the Supabase SQL
Editor, in numeric order. Any schema change must update `docs/DATABASE.md` and
be added as a new numbered script. The sync contract (`push_rows`) is described
in `docs/SYNC.md`.

## Commands

```
./gradlew assembleDebug    # build
./gradlew test             # JVM unit tests
./gradlew ktlintCheck      # formatting check
./gradlew ktlintFormat     # auto-format
./gradlew detekt           # static analysis
```

CI is not configured yet. Shared build configuration lives in
`build-logic/convention` (plugins `lifeos.android.application`,
`lifeos.android.library`, `lifeos.kotlin.library`, `lifeos.android.compose`,
`lifeos.quality`); change common Android, Compose or lint settings there, not
in module scripts. Modules reference each other with type-safe accessors
(`projects.core.domain`). detekt rules are in
`config/detekt/detekt.yml`.

## Language

The project owner writes in Italian. Code, comments, commit messages and
documentation are in English.
