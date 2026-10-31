# Offline-first and synchronization

> **Status: design only.** Backend support (`push_rows`) exists and was
> verified manually. The client-side synchronization is not implemented; it is
> planned for Phase 3.

## Source of truth

Room is the source of truth for every screen. The UI observes local `Flow`s;
network responses update Room and the UI reacts to the database, never to the
response directly.

```
UI -> ViewModel -> UseCase -> Repository -> Room -> Flow -> UI

Supabase -> Remote data source -> DTO mapping -> Repository -> Room -> Flow -> UI
```

## Local mutations

1. Validate the operation in the domain layer.
2. Persist the change in Room.
3. The UI updates immediately through the database flow.
4. The record is marked `PENDING_UPLOAD` (or `PENDING_DELETE`).
5. A synchronization job is scheduled.
6. The change is pushed when connectivity allows.
7. The local status is updated from the server response.

No normal operation requires a network connection.

## Local sync status

Kept in Room only (not in the backend): `SYNCED`, `PENDING_UPLOAD`,
`PENDING_DELETE`, `SYNC_ERROR`.

## Push

The client calls `push_rows(p_table, p_rows)` with a batch of **complete** rows
of one table. Each row carries `base_updated_at`: the `updated_at` the client
last received from the server for that record (null for a new record).

For each row the server, holding a row lock:

| Situation | Result |
|---|---|
| row does not exist | inserted: `APPLIED` |
| row on the server is identical to the one sent | `APPLIED` (idempotent retry) |
| server `updated_at` equals `base_updated_at` | updated: `APPLIED` |
| server `updated_at` differs | not written: `CONFLICT`, server row returned |
| any database error (constraint, foreign key, ...) | `ERROR` with SQLSTATE; other rows continue |

`APPLIED` and `CONFLICT` return the server row, which the client stores in Room
(with `SYNCED`). The server forces `user_id` to the authenticated user and
always assigns `updated_at`.

Deletion is an update that sets `deleted_at`. A modification based on an older
version of a deleted record produces `CONFLICT`, so deleted records do not come
back.

### Client rules

- Send complete rows: missing columns are not filled with defaults.
- Push tables in dependency order: categories, tags, projects, goals, tasks,
  then `goal_projects` and `task_tags`, then habits and completions; events and
  notifications at any point.
- Keep `updated_at` with microsecond precision exactly as returned.
- At most 500 rows per call.
- Unique constraints can fail across devices (for example two devices creating
  the completion of the same habit and day with different UUIDs, or the same
  category name). The client must reconcile after pulling.

## Pull

Incremental: select the rows with `updated_at` greater than the last cursor,
per table, ordered by `updated_at` (covered by the `(user_id, updated_at)`
indexes). Tombstones are pulled too and applied as deletions.

`updated_at` is the start time of the transaction, so a row can become visible
after the client moved its cursor past that time. Each pull therefore restarts
a few seconds **before** the last cursor, and applying rows to Room is
idempotent.

## Conflict resolution

Initial policy: **last-write-wins on server timestamps, with protection against
overwriting newer remote changes.** The server never overwrites a version the
client did not know. On `CONFLICT` the server version is stored locally and the
local edit is not silently lost: the exact user-facing behavior (re-apply the
edit on top of the new version, or discard it and notify) is *Open* and will be
decided and documented before the sync client is written.

## Background work

WorkManager with network constraint, exponential backoff, idempotent jobs and
unique work (one sync job at a time). Synchronization status is exposed as an
observable state for the UI.

## Account deletion

`delete_my_account()` removes the user and all their data. Before calling it,
delete the avatar files through the Storage API; afterwards sign out locally
and clear the local database.

## Testing requirements

Sync behavior must be tested with failure recovery: offline writes, retries,
duplicate requests, conflicts, concurrent modifications and tombstones. Row
Level Security must also be verified against a second user.
