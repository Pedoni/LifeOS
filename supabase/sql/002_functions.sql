-- =====================================================================
-- LifeOS - RPC functions (run after 001_schema.sql)
-- See docs/SYNC.md for the sync contract.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. push_rows: batch write with protection against overwriting newer
--    remote changes
--
-- Contract:
--   input  p_table : syncable table (whitelist below)
--          p_rows  : JSON array of COMPLETE rows, each with an extra
--                    "base_updated_at" = the updated_at the client knew
--                    (null/absent for a new record)
--   output JSON array, one entry per row, same order:
--          { key, status, row?, error_code?, error_message? }
--     APPLIED  : written (or already identical on the server: idempotent
--                retry); "row" is the server state, with the new updated_at
--     CONFLICT : the server has a newer version; NOT overwritten;
--                "row" is the server version
--     ERROR    : row rejected (constraint, FK, ...); the others proceed
--
-- Runs as SECURITY INVOKER: RLS stays the only access barrier, the
-- function cannot touch other users' rows.
-- ---------------------------------------------------------------------
create or replace function public.push_rows(p_table text, p_rows jsonb)
returns jsonb
language plpgsql
set search_path = ''
as $$
declare
  v_uid      uuid := auth.uid();
  v_pk_cols  text[];
  v_match    text;
  v_set      text;
  v_item     jsonb;
  v_base     timestamptz;
  v_row      jsonb;
  v_key      jsonb;
  v_existing jsonb;
  v_incoming jsonb;
  v_result   jsonb;
  v_status   text;
  v_out      jsonb := '[]'::jsonb;
begin
  if v_uid is null then
    raise exception 'not authenticated' using errcode = '28000';
  end if;

  if p_table in ('categories', 'tags', 'projects', 'goals', 'tasks', 'habits',
                 'habit_completions', 'calendar_events', 'notifications') then
    v_pk_cols := array['id'];
  elsif p_table = 'goal_projects' then
    v_pk_cols := array['goal_id', 'project_id'];
  elsif p_table = 'task_tags' then
    v_pk_cols := array['task_id', 'tag_id'];
  else
    raise exception 'table "%" is not syncable', p_table using errcode = '22023';
  end if;

  if jsonb_typeof(p_rows) is distinct from 'array' then
    raise exception 'p_rows must be a JSON array' using errcode = '22023';
  end if;
  if jsonb_array_length(p_rows) > 500 then
    raise exception 'too many rows (max 500)' using errcode = '22023';
  end if;

  select string_agg(format('t.%1$I = r.%1$I', c), ' and ')
    into v_match
    from unnest(v_pk_cols) as c;

  -- updatable columns: all but key, owner and timestamps
  select string_agg(format('%1$I = r.%1$I', a.attname), ', ')
    into v_set
    from pg_catalog.pg_attribute a
   where a.attrelid = format('public.%I', p_table)::regclass
     and a.attnum > 0
     and not a.attisdropped
     and a.attname::text <> all (v_pk_cols || array['user_id', 'created_at', 'updated_at']);

  for v_item in select value from jsonb_array_elements(p_rows) loop
    v_key := null;
    v_existing := null;
    begin
      v_base := nullif(v_item ->> 'base_updated_at', '')::timestamptz;
      -- user_id is always forced to the authenticated user; updated_at is decided by the server
      v_row := (v_item - 'base_updated_at' - 'updated_at')
               || jsonb_build_object('user_id', v_uid);
      select jsonb_object_agg(c, v_row -> c) into v_key from unnest(v_pk_cols) as c;

      execute format(
        'select to_jsonb(t.*)
           from public.%1$I t, jsonb_populate_record(null::public.%1$I, $1) r
          where %2$s
            for update of t', p_table, v_match)
        into v_existing using v_row;

      if v_existing is null then
        execute format(
          'insert into public.%1$I
             select * from jsonb_populate_record(null::public.%1$I, $1)
           returning to_jsonb(%1$I.*)', p_table)
          into v_result using v_row;
        v_status := 'APPLIED';
      else
        execute format(
          'select to_jsonb(x) from
             (select * from jsonb_populate_record(null::public.%1$I, $1)) x', p_table)
          into v_incoming using v_row;

        if (v_incoming - 'updated_at') = (v_existing - 'updated_at') then
          -- same version already on the server: retry of a push that succeeded
          v_result := v_existing;
          v_status := 'APPLIED';
        elsif v_base is not null
              and (v_existing ->> 'updated_at')::timestamptz = v_base then
          execute format(
            'update public.%1$I as t set %2$s
               from jsonb_populate_record(null::public.%1$I, $1) r
              where %3$s
          returning to_jsonb(t.*)', p_table, v_set, v_match)
            into v_result using v_row;
          v_status := 'APPLIED';
        else
          v_result := v_existing;
          v_status := 'CONFLICT';
        end if;
      end if;

      v_out := v_out || jsonb_build_object(
        'key', v_key, 'status', v_status, 'row', v_result);
    exception
      -- Recovery: the error stays confined to the row (sub-transaction) and
      -- is returned to the client, which marks it SYNC_ERROR without
      -- blocking the rest of the batch.
      when others then
        v_out := v_out || jsonb_build_object(
          'key', v_key, 'status', 'ERROR',
          'error_code', sqlstate, 'error_message', sqlerrm);
    end;
  end loop;

  return v_out;
end;
$$;

revoke execute on function public.push_rows(text, jsonb) from public, anon;
grant  execute on function public.push_rows(text, jsonb) to authenticated;

-- ---------------------------------------------------------------------
-- 2. delete_my_account: deletes the user and, by cascade, all their data
--
-- Before calling it the client must delete its own files from the
-- "avatars" bucket through the Storage API (storage tables cannot be
-- deleted directly via SQL), then sign out locally.
-- ---------------------------------------------------------------------
create or replace function public.delete_my_account()
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_uid uuid := auth.uid();
begin
  if v_uid is null then
    raise exception 'not authenticated' using errcode = '28000';
  end if;
  delete from auth.users where id = v_uid;
end;
$$;

revoke execute on function public.delete_my_account() from public, anon;
grant  execute on function public.delete_my_account() to authenticated;

-- ---------------------------------------------------------------------
-- 3. Keep the profile email aligned when it changes in auth.users
-- ---------------------------------------------------------------------
create or replace function public.handle_user_email_change()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  update public.profiles set email = new.email where id = new.id;
  return new;
end;
$$;

create trigger on_auth_user_email_changed
  after update of email on auth.users
  for each row
  when (old.email is distinct from new.email)
  execute function public.handle_user_email_change();

-- Trigger functions must not be callable from the app
revoke execute on function public.handle_new_user()          from public, anon, authenticated;
revoke execute on function public.handle_user_email_change() from public, anon, authenticated;
