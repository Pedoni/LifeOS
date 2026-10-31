-- =====================================================================
-- LifeOS - initial schema (Supabase / PostgreSQL 15+)
-- Run on an empty project, from the SQL Editor.
-- See docs/DATABASE.md for the rationale.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Helper functions
-- ---------------------------------------------------------------------

-- updated_at is always set by the server: this is the basis of the
-- last-write-wins policy based on server timestamps (see docs/SYNC.md).
create or replace function public.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at := now();
  return new;
end;
$$;

-- ---------------------------------------------------------------------
-- 2. Profiles (1:1 with auth.users). No credentials are stored here.
-- ---------------------------------------------------------------------
create table public.profiles (
  id           uuid primary key references auth.users (id) on delete cascade,
  email        text,
  display_name text,
  avatar_url   text,                       -- path inside the "avatars" bucket
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now()
);

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles (id, email, display_name, avatar_url)
  values (
    new.id,
    new.email,
    coalesce(new.raw_user_meta_data ->> 'full_name', new.raw_user_meta_data ->> 'name'),
    coalesce(new.raw_user_meta_data ->> 'avatar_url', new.raw_user_meta_data ->> 'picture')
  );
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- ---------------------------------------------------------------------
-- 3. Categories and tags
-- ---------------------------------------------------------------------
create table public.categories (
  id         uuid primary key default gen_random_uuid(),
  user_id    uuid not null default auth.uid() references auth.users (id) on delete cascade,
  name       text not null check (char_length(btrim(name)) between 1 and 100),
  color      text check (color ~ '^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$'),
  type       text not null check (type in ('TASK', 'GOAL', 'EVENT')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  unique (id, user_id)
);
create unique index categories_unique_name_idx
  on public.categories (user_id, type, lower(name)) where deleted_at is null;

create table public.tags (
  id         uuid primary key default gen_random_uuid(),
  user_id    uuid not null default auth.uid() references auth.users (id) on delete cascade,
  name       text not null check (char_length(btrim(name)) between 1 and 50),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  unique (id, user_id)
);
create unique index tags_unique_name_idx
  on public.tags (user_id, lower(name)) where deleted_at is null;

-- ---------------------------------------------------------------------
-- 4. Projects and goals
-- ---------------------------------------------------------------------
create table public.projects (
  id           uuid primary key default gen_random_uuid(),
  user_id      uuid not null default auth.uid() references auth.users (id) on delete cascade,
  name         text not null check (char_length(btrim(name)) between 1 and 200),
  description  text,
  status       text not null default 'PLANNED'
               check (status in ('PLANNED', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED')),
  priority     text not null default 'MEDIUM'
               check (priority in ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
  color        text check (color ~ '^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$'),
  start_date   date,
  deadline     date,
  completed_at timestamptz,
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now(),
  deleted_at   timestamptz,
  unique (id, user_id),
  check (deadline is null or start_date is null or deadline >= start_date)
);

create table public.goals (
  id            uuid primary key default gen_random_uuid(),
  user_id       uuid not null default auth.uid() references auth.users (id) on delete cascade,
  category_id   uuid,
  title         text not null check (char_length(btrim(title)) between 1 and 200),
  description   text,
  goal_type     text not null check (goal_type in ('BINARY', 'NUMERIC', 'PROGRESS')),
  target_value  numeric,
  current_value numeric not null default 0,
  progress      numeric(5, 2) not null default 0 check (progress between 0 and 100),
  status        text not null default 'ACTIVE'
                check (status in ('ACTIVE', 'COMPLETED', 'ARCHIVED')),
  start_date    date,
  target_date   date,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now(),
  deleted_at    timestamptz,
  unique (id, user_id),
  check (goal_type <> 'NUMERIC' or target_value is not null),
  check (target_date is null or start_date is null or target_date >= start_date),
  foreign key (category_id, user_id) references public.categories (id, user_id)
    on delete set null (category_id)
);

-- N:M relation goals <-> projects
create table public.goal_projects (
  user_id    uuid not null default auth.uid() references auth.users (id) on delete cascade,
  goal_id    uuid not null,
  project_id uuid not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  primary key (goal_id, project_id),
  foreign key (goal_id, user_id) references public.goals (id, user_id) on delete cascade,
  foreign key (project_id, user_id) references public.projects (id, user_id) on delete cascade
);

-- ---------------------------------------------------------------------
-- 5. Tasks
-- ---------------------------------------------------------------------
create table public.tasks (
  id                         uuid primary key default gen_random_uuid(),
  user_id                    uuid not null default auth.uid() references auth.users (id) on delete cascade,
  project_id                 uuid,
  goal_id                    uuid,
  category_id                uuid,
  title                      text not null check (char_length(btrim(title)) between 1 and 300),
  description                text,
  notes                      text,
  status                     text not null default 'TODO'
                             check (status in ('TODO', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
  priority                   text not null default 'MEDIUM'
                             check (priority in ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
  due_date                   timestamptz,
  reminder_date              timestamptz,
  estimated_duration_minutes integer check (estimated_duration_minutes > 0),
  completed_at               timestamptz,
  recurrence_frequency       text check (recurrence_frequency in ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
  recurrence_interval        integer not null default 1 check (recurrence_interval >= 1),
  recurrence_days            smallint[] not null default '{}'
                             check (recurrence_days <@ array[1, 2, 3, 4, 5, 6, 7]::smallint[]),
  recurrence_end_date        date,
  created_at                 timestamptz not null default now(),
  updated_at                 timestamptz not null default now(),
  deleted_at                 timestamptz,
  unique (id, user_id),
  foreign key (project_id, user_id) references public.projects (id, user_id)
    on delete set null (project_id),
  foreign key (goal_id, user_id) references public.goals (id, user_id)
    on delete set null (goal_id),
  foreign key (category_id, user_id) references public.categories (id, user_id)
    on delete set null (category_id)
);

create table public.task_tags (
  user_id    uuid not null default auth.uid() references auth.users (id) on delete cascade,
  task_id    uuid not null,
  tag_id     uuid not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  primary key (task_id, tag_id),
  foreign key (task_id, user_id) references public.tasks (id, user_id) on delete cascade,
  foreign key (tag_id, user_id) references public.tags (id, user_id) on delete cascade
);

-- ---------------------------------------------------------------------
-- 6. Habits (definition kept separate from completions)
-- ---------------------------------------------------------------------
create table public.habits (
  id             uuid primary key default gen_random_uuid(),
  user_id        uuid not null default auth.uid() references auth.users (id) on delete cascade,
  name           text not null check (char_length(btrim(name)) between 1 and 200),
  description    text,
  frequency      text not null check (frequency in ('DAILY', 'WEEKLY', 'SPECIFIC_DAYS')),
  scheduled_days smallint[] not null default '{}'
                 check (scheduled_days <@ array[1, 2, 3, 4, 5, 6, 7]::smallint[]),  -- ISO: 1 = Monday
  start_date     date not null,
  end_date       date,
  is_active      boolean not null default true,
  created_at     timestamptz not null default now(),
  updated_at     timestamptz not null default now(),
  deleted_at     timestamptz,
  unique (id, user_id),
  check (end_date is null or end_date >= start_date)
);

create table public.habit_completions (
  id              uuid primary key default gen_random_uuid(),
  habit_id        uuid not null,
  user_id         uuid not null default auth.uid() references auth.users (id) on delete cascade,
  completion_date date not null,           -- the user's local day
  status          text not null check (status in ('COMPLETED', 'SKIPPED')),
  note            text,
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now(),
  deleted_at      timestamptz,
  unique (habit_id, completion_date),
  foreign key (habit_id, user_id) references public.habits (id, user_id) on delete cascade
);

-- ---------------------------------------------------------------------
-- 7. Calendar and notifications
-- ---------------------------------------------------------------------
create table public.calendar_events (
  id              uuid primary key default gen_random_uuid(),
  user_id         uuid not null default auth.uid() references auth.users (id) on delete cascade,
  category_id     uuid,
  title           text not null check (char_length(btrim(title)) between 1 and 200),
  description     text,
  start_date_time timestamptz not null,
  end_date_time   timestamptz not null,
  location        text,
  reminder_date   timestamptz,
  event_type      text not null default 'PERSONAL'
                  check (event_type in ('PERSONAL', 'APPOINTMENT', 'OTHER')),
  color           text check (color ~ '^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$'),
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now(),
  deleted_at      timestamptz,
  unique (id, user_id),
  check (end_date_time >= start_date_time),
  foreign key (category_id, user_id) references public.categories (id, user_id)
    on delete set null (category_id)
);

-- related_entity_id is polymorphic (task, event, habit, goal):
-- no FK, consistency is handled by the app.
create table public.notifications (
  id                uuid primary key default gen_random_uuid(),
  user_id           uuid not null default auth.uid() references auth.users (id) on delete cascade,
  related_entity_id uuid not null,
  notification_type text not null
                    check (notification_type in
                      ('TASK_DEADLINE', 'TASK_REMINDER', 'HABIT_DAILY', 'EVENT_REMINDER', 'GOAL_DEADLINE')),
  scheduled_at      timestamptz not null,
  status            text not null default 'SCHEDULED'
                    check (status in ('SCHEDULED', 'SENT', 'DISMISSED', 'CANCELLED')),
  created_at        timestamptz not null default now(),
  updated_at        timestamptz not null default now(),
  deleted_at        timestamptz,
  -- avoids duplicate notifications
  unique (user_id, related_entity_id, notification_type, scheduled_at)
);

-- ---------------------------------------------------------------------
-- 8. Indexes
-- ---------------------------------------------------------------------
create index tasks_project_idx   on public.tasks (project_id);
create index tasks_goal_idx      on public.tasks (goal_id);
create index tasks_category_idx  on public.tasks (category_id);
create index tasks_due_idx       on public.tasks (user_id, due_date)
  where deleted_at is null and status in ('TODO', 'IN_PROGRESS');
create index tasks_status_idx    on public.tasks (user_id, status) where deleted_at is null;
create index task_tags_tag_idx   on public.task_tags (tag_id);
create index goal_projects_project_idx on public.goal_projects (project_id);
create index goals_category_idx  on public.goals (category_id);
create index habit_completions_date_idx on public.habit_completions (user_id, completion_date);
create index calendar_events_start_idx  on public.calendar_events (user_id, start_date_time)
  where deleted_at is null;
create index calendar_events_category_idx on public.calendar_events (category_id);
create index notifications_due_idx on public.notifications (user_id, scheduled_at)
  where status = 'SCHEDULED';

-- ---------------------------------------------------------------------
-- 9. updated_at triggers, sync indexes (incremental pull) and RLS
-- ---------------------------------------------------------------------
do $$
declare
  t text;
begin
  foreach t in array array[
    'profiles', 'categories', 'tags', 'projects', 'goals', 'goal_projects',
    'tasks', 'task_tags', 'habits', 'habit_completions',
    'calendar_events', 'notifications'
  ] loop
    execute format(
      'create trigger %I before insert or update on public.%I
         for each row execute function public.set_updated_at()',
      t || '_set_updated_at', t);
    execute format('alter table public.%I enable row level security', t);
  end loop;

  foreach t in array array[
    'categories', 'tags', 'projects', 'goals', 'goal_projects',
    'tasks', 'task_tags', 'habits', 'habit_completions',
    'calendar_events', 'notifications'
  ] loop
    execute format(
      'create index %I on public.%I (user_id, updated_at)',
      t || '_sync_idx', t);
    execute format(
      'create policy %I on public.%I for all to authenticated
         using ((select auth.uid()) = user_id)
         with check ((select auth.uid()) = user_id)',
      t || '_own_rows', t);
  end loop;
end;
$$;

-- Profiles: users read and update only their own.
-- Insert happens through the trigger; delete through cascade from auth.users.
create policy profiles_select_own on public.profiles
  for select to authenticated using ((select auth.uid()) = id);
create policy profiles_update_own on public.profiles
  for update to authenticated
  using ((select auth.uid()) = id) with check ((select auth.uid()) = id);

-- No access for the anonymous role
revoke all on all tables in schema public from anon;

-- ---------------------------------------------------------------------
-- 10. Storage: profile pictures (private bucket, one folder per user)
-- ---------------------------------------------------------------------
insert into storage.buckets (id, name, public)
values ('avatars', 'avatars', false)
on conflict (id) do nothing;

create policy avatars_select_own on storage.objects
  for select to authenticated
  using (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid())::text);
create policy avatars_insert_own on storage.objects
  for insert to authenticated
  with check (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid())::text);
create policy avatars_update_own on storage.objects
  for update to authenticated
  using (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid())::text);
create policy avatars_delete_own on storage.objects
  for delete to authenticated
  using (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid())::text);
