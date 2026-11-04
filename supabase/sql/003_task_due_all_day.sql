-- =====================================================================
-- LifeOS - tasks: all-day due dates (run after 002_functions.sql)
--
-- A due date is either a calendar day ("by Friday") or an exact moment.
-- When due_all_day is true, due_date holds midnight UTC of that day, so
-- the day does not move when the device changes time zone.
-- Additive and non-destructive: existing rows keep their exact moment.
-- =====================================================================

alter table public.tasks
  add column due_all_day boolean not null default false;
