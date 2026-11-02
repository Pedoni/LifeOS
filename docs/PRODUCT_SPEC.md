# LifeOS - Product & Technical Master Specification

> This is the original specification (v1.0), transcribed from the master PDF.
> It describes the complete product vision, not the current state of the code.
> Decisions taken after this document (branching, versioning, module roadmap,
> backend choices) are recorded in [DECISIONS.md](DECISIONS.md) and
> [ROADMAP.md](ROADMAP.md), which prevail where they differ.

- Version: 1.0
- Project: LifeOS
- Platform: Android
- Language: Kotlin
- UI framework: Jetpack Compose
- Architecture: Clean Architecture + MVVM + Unidirectional Data Flow
- Development environment: Android Studio + Claude Code

## 1. Project overview

### 1.1 What is LifeOS?

LifeOS is a comprehensive personal productivity application designed to act as
a central operating system for an individual's everyday life.

Its purpose is to bring the different aspects of personal organization into
one coherent environment. Instead of switching between multiple applications to
manage tasks, projects, habits, goals, schedules and personal progress, users
should be able to manage everything from a single application.

LifeOS should help users:

- Organize their daily activities.
- Manage personal and professional projects.
- Track tasks, deadlines and priorities.
- Build consistent habits.
- Define long-term objectives.
- Manage their calendar.
- Monitor their productivity.
- Understand how they spend their time.
- Maintain a clear overview of their commitments and progress.

The application should not feel like a generic task manager. It should feel
like a personal command center: a place where users can understand what is
happening in their lives, decide what deserves attention and take action.

### 1.2 Product philosophy

1. **Everything is connected.** Tasks can belong to projects. Projects can
   contribute to goals. Tasks can appear in the calendar. Habits contribute to
   personal consistency. Statistics aggregate information from different parts
   of the application.
2. **Simplicity at the surface, sophistication underneath.** The interface
   should remain intuitive even when the underlying data model and
   architecture are sophisticated.
3. **Action-oriented design.** LifeOS should help users accomplish things, not
   merely record information.
4. **Personalization.** Users should be able to organize their environment
   according to their own preferences.
5. **Reliability.** User data must be persistent, secure and available even
   without an internet connection.
6. **Long-term scalability.** The application must be designed to accommodate
   additional modules without requiring architectural rewrites.

## 2. Functional requirements

The following sections describe the complete product vision. Not every feature
must be implemented immediately. The MVP and development priorities are
described in the roadmap.

### 2.1 Authentication and user accounts

LifeOS must support personal user accounts.

Required capabilities: user registration, login, logout, password recovery,
session persistence, user profile management, account deletion, secure
authentication.

Potential authentication methods: email and password; Google Sign-In.

Each user must have an isolated personal environment. All user-owned data must
be associated with the authenticated user ID. The architecture must not assume
that the application will always be used by a single account on a single
device.

### 2.2 Dashboard

The dashboard is the central screen of LifeOS. It must provide an overview of
the user's current situation without overwhelming them with information.

It should contain:

- **Greeting and date:** personalized greeting, current date, optional
  contextual message.
- **Today's overview:** number of pending tasks, completed tasks, upcoming
  deadlines, today's scheduled events.
- **Tasks:** important tasks, overdue tasks, high-priority tasks, quick access
  to task creation.
- **Habits:** habits scheduled for today, completion status, current streaks.
- **Projects:** active projects, progress indicators, upcoming project
  deadlines.
- **Goals:** current objectives, progress toward each objective.
- **Productivity overview:** tasks completed this week, habit consistency,
  weekly progress.

The dashboard must be customizable in the future, allowing users to choose
which widgets are visible and how they are organized.

### 2.3 Task management

Task management is one of the core modules.

Users must be able to: create, edit and delete tasks; complete and reopen
tasks; set priorities, due dates and reminders; add descriptions and notes;
assign categories; associate tasks with projects; add tags; define estimated
duration; define recurring tasks.

Each task should support: ID, title, description, status, priority, creation
date, updated date, due date, completion date, reminder date, estimated
duration, category, tags, associated project, associated goal (when
applicable), recurrence configuration, user ID.

Task statuses: `TODO`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`.

Task priorities: `LOW`, `MEDIUM`, `HIGH`, `URGENT`.

The interface should offer multiple ways of visualizing tasks: list view,
today's tasks, upcoming tasks, overdue tasks, completed tasks, tasks grouped by
priority, tasks grouped by project. Filtering and sorting must be supported.

### 2.4 Project management

Users must be able to organize multiple tasks into projects. Examples: develop
a mobile application, learn a programming language, organize a holiday,
complete a personal objective, prepare a professional certification.

A project should contain: ID, name, description, status, priority, start date,
deadline, creation date, completion date, color or visual identifier,
associated tasks, associated goals, user ID.

Project statuses: `PLANNED`, `ACTIVE`, `ON_HOLD`, `COMPLETED`, `ARCHIVED`.

Project progress should be calculated from its associated tasks. The
application should display: total tasks, completed tasks, pending tasks,
completion percentage, deadline, project status.

Future capabilities: project milestones, project-specific notes, attachments,
subtasks, project templates.

### 2.5 Habit tracking

The habit module helps users establish and maintain consistent routines.

Users must be able to: create, edit and delete habits; define frequency; set
target days; track daily completion; view historical performance; monitor
streaks; view consistency statistics.

Examples: read 20 minutes every day, exercise three times per week, drink
enough water, study programming, wake up at a specific time.

Each habit should support: ID, name, description, frequency, scheduled
weekdays, start date, optional end date, active status, user ID.

Habit completion must be represented separately from the habit definition. A
habit should have individual completion records containing: habit ID,
completion date, completion status, optional note. This separation is
essential for maintaining historical records.

The application must correctly handle streak calculations, skipped days,
scheduled days and historical changes.

### 2.6 Goals and objectives

The goals module allows users to define medium-term and long-term objectives.
Examples: read 20 books this year, save EUR 5,000, complete a professional
certification, develop and publish an application, exercise consistently for
six months.

Each goal should contain: ID, title, description, category, start date, target
date, status, progress, target value (when applicable), current value (when
applicable), associated projects, associated tasks, user ID.

Goal types should support:

- **Binary objectives:** either completed or not completed.
- **Numeric objectives:** a measurable target, such as saving EUR 5,000.
- **Progress-based objectives:** updated through associated activities or
  manually.

The application should display goal progress visually.

### 2.7 Calendar and scheduling

The calendar provides a time-oriented overview of the user's activities. It
must integrate information from: tasks with scheduled dates, task deadlines,
habits, personal events, project milestones.

Supported views: daily, weekly, monthly.

Users must be able to create personal events with: title, description, start
date and time, end date and time, location (optional), reminder, color or
category. Calendar entries must remain distinguishable by type.

Future capabilities: external calendar integration, Google Calendar
synchronization, drag-and-drop scheduling, time blocking.

### 2.8 Statistics and productivity analytics

LifeOS must provide meaningful information about personal productivity.
Statistics should be calculated from real application data.

Possible metrics:

- **Tasks:** completed, pending, overdue, completion rate, tasks completed per
  day/week/month.
- **Habits:** completion percentage, current streak, best streak, weekly
  consistency.
- **Projects:** active projects, completed projects, average completion rate.
- **Goals:** completed objectives, progress distribution, goals approaching
  deadlines.

The analytics module should include: weekly reports, monthly reports,
historical trends, visual charts, comparisons between periods.

Use Vico for native Android chart rendering. Do not introduce artificial
metrics or meaningless productivity scores. Every statistic must have a
clearly defined calculation.

### 2.9 Notifications and reminders

LifeOS must support local notifications. Examples: task deadline approaching,
task reminder, habit scheduled for today, personal event approaching, goal
deadline approaching.

Use WorkManager for deferrable background operations. Use Android notification
APIs for actual notifications. Time-sensitive reminders must account for
Android notification permissions and scheduling limitations. Notification
preferences must be configurable. Avoid sending duplicate notifications.

### 2.10 User profile and settings

The profile section should contain: user information, profile picture, account
settings, notification preferences, theme preferences, language preferences,
data and privacy settings.

Theme support: light, dark, system default. Use DataStore for local
application preferences.

## 3. UX and UI design

### 3.1 Design philosophy

LifeOS must look and feel like a polished commercial Android application.
Avoid generic layouts, excessive cards, unnecessary borders and visually noisy
screens. The interface should be modern, minimal, elegant, consistent,
responsive, accessible and easy to navigate.

Use Material 3 as the foundation, but do not blindly accept default component
styling. Customize the design system.

### 3.2 Design system

Create a centralized design system containing: color palette, typography,
spacing, shapes, elevations, component styles, iconography, animation
guidelines.

All reusable components must use centralized design tokens. Avoid hardcoding
colors, spacing and typography throughout individual screens. Support dynamic
color where appropriate.

### 3.3 Navigation

Use Navigation Compose. The main navigation should provide access to:

1. Dashboard
2. Tasks
3. Projects
4. Habits
5. Goals
6. Calendar
7. Statistics
8. Profile/settings

Use a bottom navigation bar for the most important destinations. Not every
secondary screen needs to appear in the bottom navigation. The navigation
structure must support nested destinations and deep links.

### 3.4 UI states

Every screen must explicitly handle: loading, success, empty, error. Empty
states should explain what the user can do next. Error states should provide
useful information and recovery actions. Never leave screens blank because a
request failed.

### 3.5 Accessibility

Support: content descriptions, appropriate touch target sizes, readable
typography, sufficient contrast, screen readers, font scaling, predictable
navigation.

## 4. Technical architecture

LifeOS must demonstrate a serious implementation of Clean Architecture. The
architecture should be understandable, testable, scalable and maintainable. Do
not introduce unnecessary abstractions merely to increase the number of
classes.

### 4.1 Architecture principles

The application must follow: Clean Architecture, MVVM, Unidirectional Data
Flow, Repository Pattern, Dependency Inversion, SOLID principles, separation of
concerns.

The dependency direction must always point inward. The domain layer must not
depend on Android framework classes, Room, Retrofit, Supabase or Compose.

### 4.2 Layer responsibilities

**Presentation layer.** Compose screens, UI components, ViewModels, UI state,
UI events, navigation, presentation-specific models. ViewModels must expose
observable UI state using StateFlow. Screens must collect state using
lifecycle-aware collection. Use immutable UI state. Conceptual structure:
`TasksScreen`, `TasksViewModel`, `TasksUiState`, `TasksUiEvent`. Composables
must remain as stateless as reasonably possible. Business logic must not be
implemented directly inside Composables.

**Domain layer.** Domain entities, repository interfaces, use cases, domain
validation, business calculations. Examples: `GetTasksUseCase`,
`CreateTaskUseCase`, `UpdateTaskUseCase`, `CompleteTaskUseCase`,
`GetTodayTasksUseCase`, `CalculateProjectProgressUseCase`,
`GetHabitStreakUseCase`. Use cases should represent meaningful business
operations. Avoid creating trivial use cases that add no architectural value.

**Data layer.** Repository implementations, local data sources, remote data
sources, DTOs, Room entities, mappers, serialization, data synchronization.
Repository implementations must coordinate local and remote sources. Data
models must never leak into the domain layer.

### 4.3 Recommended project structure

Start with a scalable multi-module architecture. The existing Android Studio
project must be preserved and incrementally evolved.

```
LifeOS/
  app/
  core/
    common/ domain/ data/ database/ network/ datastore/
    designsystem/ ui/ navigation/
  feature/
    auth/ dashboard/ tasks/ projects/ habits/ goals/
    calendar/ statistics/ profile/
  build-logic/
  gradle/
  build.gradle.kts
  settings.gradle.kts
  README.md
```

Each feature module should contain its own presentation and data
implementation where appropriate. The shared domain layer should contain
reusable business concepts and contracts. Do not create a module for every
small class. The purpose of modularization is to establish clear ownership and
boundaries. Use Gradle convention plugins in `build-logic` to centralize common
Android and Kotlin configuration.

### 4.4 Dependency injection

Use Hilt, built on top of Dagger. Dependency injection must be centralized and
explicit. Use constructor injection, Hilt modules, appropriate scopes and
interface-to-implementation bindings. Avoid service locators and manual
dependency creation inside ViewModels.

### 4.5 Asynchronous programming

Use Kotlin Coroutines and Flow: suspend functions for asynchronous operations,
Flow for observable data streams, StateFlow for UI state, structured
concurrency, proper cancellation, no blocking operations on the main thread.
Errors must be handled deliberately. Avoid swallowing exceptions or using
generic catch blocks without a recovery strategy.

## 5. Technology stack

Use stable, mutually compatible versions. Always check current official
documentation before introducing or upgrading dependencies.

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Design | Material 3 |
| Architecture | Clean Architecture + MVVM |
| Navigation | Navigation Compose |
| DI | Hilt / Dagger |
| Local database | Room |
| Preferences | DataStore |
| Networking | Retrofit |
| HTTP client | OkHttp |
| Serialization | Kotlin Serialization |
| Async | Coroutines + Flow |
| Images | Coil |
| Background work | WorkManager |
| Pagination | Paging 3 |
| Charts | Vico |
| Backend | Supabase |
| Authentication | Supabase Auth |
| Database backend | PostgreSQL |
| Remote storage | Supabase Storage |
| Unit testing | JUnit |
| Mocking | MockK |
| Flow testing | Turbine |
| UI testing | Compose UI Testing |
| Static analysis | Detekt |
| Formatting | ktlint |
| CI/CD | GitHub Actions |

Hilt is the preferred dependency injection solution. Do not mix multiple DI
frameworks. Use a centralized version catalog in `gradle/libs.versions.toml`.

## 6. Database design

### 6.1 General principles

The database must be designed around relational integrity. Use UUID
identifiers for entities that need to exist both locally and remotely. Every
user-owned record must have an owner identifier. Use foreign keys and
appropriate indexes. Avoid storing complex relational structures as serialized
JSON when they should be represented through relational tables.

### 6.2 Core entities

The initial conceptual entities are: User, Task, Project, Habit,
HabitCompletion, Goal, CalendarEvent, Category, Tag, TaskTag, Notification.
Their final implementation must account for Room constraints, Supabase schema
requirements and domain relationships. Authentication credentials must never
be stored in the application database.

The concrete field lists are documented in [DATABASE.md](DATABASE.md).

### 6.3 Database integrity

Implement: foreign key constraints, cascading behavior where appropriate,
unique constraints, indexes, database migrations, validation at the domain
level. Do not rely exclusively on UI validation. All critical business
constraints must also be enforced by the domain and, where appropriate, the
backend.

## 7. Offline-first and data synchronization

Offline-first behavior is a core architectural requirement, not an optional
enhancement.

### 7.1 Source of truth

Room must be the primary source of truth for application screens. The UI
should observe local database streams. Remote requests should update the local
database. The UI should react to database changes rather than depending
directly on network responses.

```
Compose UI -> ViewModel -> UseCase -> Repository -> Room -> Flow -> Compose UI

Supabase -> Remote Data Source -> DTO Mapping -> Repository -> Room -> Flow -> Compose UI
```

### 7.2 Local mutations

When a user creates or modifies an entity:

1. Validate the operation.
2. Persist the change locally.
3. Update the UI immediately.
4. Mark the record as pending synchronization.
5. Schedule synchronization.
6. Send the change to the backend when connectivity permits.
7. Update the synchronization status.

The user must not need an internet connection to perform normal operations.

### 7.3 Synchronization strategy

Each synchronized entity should have a stable UUID, an updated timestamp, a
deletion marker and a synchronization status. Suggested states: `SYNCED`,
`PENDING_UPLOAD`, `PENDING_DELETE`, `SYNC_ERROR`.

Use soft deletion and tombstones where necessary so that deleted records do not
reappear during synchronization.

The synchronization system must handle: new local records, updated local
records, remote changes, deleted records, duplicate requests, network failures,
retry mechanisms, concurrent modifications.

For the initial implementation, use a documented last-write-wins policy based
on server timestamps, with explicit safeguards against overwriting newer remote
changes. Do not implement synchronization as a collection of unrelated API
calls inside individual ViewModels. It must be an independent architectural
concern.

### 7.4 Background synchronization

Use WorkManager for synchronization that can be deferred: network constraints,
retry with backoff, idempotent operations, appropriate unique work policies,
observable synchronization status.

## 8. Backend and security

### 8.1 Supabase

Services: Supabase Auth, PostgreSQL, Row Level Security, Supabase Storage when
required. The backend must mirror the important relationships of the domain
model.

### 8.2 Security requirements

Security is not optional. Implement: Row Level Security on user-owned tables;
policies ensuring users can access only their own records; secure session
management; HTTPS communication; input validation; safe handling of
authentication tokens; no hardcoded secrets; no service-role keys in the
Android application.

The Supabase anonymous/public key may be included in the client configuration
when appropriate, but access must always be protected through RLS. Use
encrypted local storage only when justified by the sensitivity of the
information and the actual threat model. Do not claim that local database
encryption exists unless it is implemented.

### 8.3 Environment configuration

Separate configuration by environment: development and production. Never
commit credentials or secrets to Git. Use local configuration files and CI
secrets.

## 9. Testing strategy

Testing must be part of development from the beginning. Do not postpone all
testing until the application is complete.

- **Unit tests:** use cases, business rules, repository behavior, validation,
  progress calculations, habit streak calculations, date and recurrence logic,
  synchronization decisions.
- **Repository tests:** local data retrieval, remote data retrieval, cache
  updates, offline behavior, error handling, synchronization status, conflict
  resolution. Use an in-memory Room database where appropriate.
- **ViewModel tests:** initial state, loading state, successful operations,
  error states, UI event handling, state updates, coroutine cancellation. Use
  Turbine for Flow testing.
- **Compose UI tests:** registration and login, creating a task, completing a
  task, creating a project, creating and completing a habit, creating a goal,
  navigating between screens. Tests must verify user-visible behavior, not
  merely implementation details.

## 10. Code quality and development standards

### 10.1 Kotlin conventions

Follow official Kotlin coding conventions. Prefer immutable data structures,
data classes for state and models, sealed interfaces for UI states and events
when appropriate, extension functions when they improve readability,
constructor injection, explicit types where they improve clarity.

Avoid: god classes, excessive inheritance, global mutable state, business logic
inside Composables, direct database access from ViewModels, hardcoded strings,
unnecessary abstractions, duplicate logic, massive files containing unrelated
responsibilities.

### 10.2 Error handling

Use a consistent error handling strategy. Represent expected failures
explicitly. Differentiate between network failures, authentication failures,
validation errors, database errors and unexpected failures. Errors should be
translated into useful UI messages at the appropriate layer. Never expose
technical stack traces to users.

### 10.3 Logging

Use structured logging in development. Never log passwords, authentication
tokens, sensitive personal data or private user content unnecessarily. Disable
or appropriately restrict verbose logging in production.

### 10.4 Documentation

Document architectural decisions. Use concise comments to explain non-obvious
behavior. Do not write comments that simply repeat what the code already
expresses. Maintain a clear README with setup instructions and project
information.

## 11. Performance and reliability

LifeOS must remain responsive as the database grows. Requirements: avoid
unnecessary recompositions, use stable UI models, use appropriate Compose state
management, optimize database queries, use pagination for large datasets where
needed, avoid blocking the main thread, load images efficiently, handle
lifecycle changes correctly, avoid memory leaks, use lazy layouts for large
collections. Performance must be measured rather than optimized through
guesswork.

## 12. Product roadmap (original)

The complete vision is larger than the initial release. Development must
therefore happen incrementally. The updated roadmap is in
[ROADMAP.md](ROADMAP.md).

- **Phase 0 - Foundation:** inspect the existing Empty Activity project, review
  Gradle configuration, design system, modularization, Hilt, navigation, Room,
  DataStore, domain and data conventions, testing infrastructure, prepare
  Supabase integration. Deliverable: a clean, buildable and testable
  application foundation.
- **Phase 1 - Core application:** authentication, user profile, dashboard, task
  management, project management, local persistence, basic navigation.
- **Phase 2 - Personal productivity:** habit tracking, goals, calendar,
  notifications, recurring tasks, advanced filtering.
- **Phase 3 - Backend and synchronization:** Supabase authentication,
  PostgreSQL schema, RLS policies, remote data sources, repository
  synchronization, offline-first behavior, conflict handling, background
  synchronization.
- **Phase 4 - Analytics:** productivity statistics, weekly and monthly reports,
  habit analytics, project progress, goal progress, charts.
- **Phase 5 - Advanced features:** advanced recurring rules, time blocking,
  calendar integrations, attachments, widgets, home screen shortcuts, advanced
  customization, export and import, additional analytics, cloud backups. These
  features must not compromise the stability of the core application.
- **Phase 6 - Production readiness:** comprehensive testing, accessibility
  improvements, performance profiling, security review, release configuration,
  CI/CD, app signing, Play Store preparation, privacy documentation.

## 13. Git and development workflow

The project owner personally guides the development process. Development
happens through individually authorized steps and branches.

Commits follow Conventional Commits, for example
`feat(tasks): implement task creation`, `fix(database): correct task migration`,
`refactor(domain): extract task validation`, `test(habits): add streak
calculation tests`, `chore(gradle): update dependency configuration`. Commits
should be focused and logically grouped.

For every authorized development step:

1. Understand the requested feature.
2. Inspect the existing code.
3. Identify architectural dependencies.
4. Propose the implementation approach.
5. Identify files and modules that will change.
6. Implement only the agreed scope.
7. Add or update tests.
8. Run relevant tests.
9. Run compilation checks.
10. Report the changes and any remaining issues.

Do not modify unrelated modules. Do not silently introduce new dependencies. Do
not perform broad refactoring unless explicitly requested. Do not merge
branches or rewrite Git history without authorization.

## 14. Claude Code operating instructions

The project owner is responsible for choosing development priorities, defining
individual steps, selecting branches, approving architectural changes,
approving new dependencies, deciding when a feature is complete and deciding
when to merge branches. Claude Code is the implementation assistant, not the
autonomous product owner.

Claude Code must not: implement all features automatically; start a phase
without explicit authorization; change the project's name; recreate the Android
project; replace the existing build configuration without justification;
introduce unnecessary dependencies; implement speculative functionality; change
the Git branch without permission; commit or push without permission; merge
branches without permission; ignore architectural boundaries; leave broken or
placeholder implementations while claiming a feature is complete.

When requirements are ambiguous, ask before making architectural assumptions.
Claude Code acts as a senior Android engineer: it challenges poor technical
decisions, explains alternatives, compares trade-offs, recommends one and waits
for approval when the decision materially changes architecture or scope. It
prioritizes official documentation and stable dependencies.

## 15. Definition of done

A feature is complete only when: its functionality meets the requirements; its
architecture respects the established boundaries; its data persists correctly;
its UI handles all relevant states; errors are handled correctly; loading and
empty states are implemented; relevant tests are present and passing; there are
no known regressions; code formatting and static analysis pass; documentation
is updated where necessary. For features involving synchronization, offline
behavior and failure recovery must also be tested.

## 16. Final product vision

LifeOS should ultimately become a complete, reliable and polished personal
productivity platform, demonstrating a sophisticated but maintainable
architecture, excellent native UI and UX, well-designed relational data models,
offline-first persistence, reliable backend synchronization, authentication and
security, reactive programming, background processing, automated testing and
production-quality engineering practices.

The objective is not to maximize the number of technologies used, but to use
the right technologies correctly, with clear architectural reasoning. Every
feature should contribute to a coherent product, every architectural
abstraction should have a purpose, every important business rule should be
testable and every user action should produce predictable behavior. LifeOS must
be developed as a real product, not as a tutorial project.
