import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';

// Reuse the Web project's PGlite version; a disposable database, never Supabase.
const { PGlite } = await import(process.env.PGLITE_MODULE || '@electric-sql/pglite');
test('event permissions, consent withdrawal and host import in real Postgres', async () => {
  const db = new PGlite();
  const user = '00000000-0000-4000-8000-000000000001';
  const other = '00000000-0000-4000-8000-000000000002';
  const host = '00000000-0000-4000-8000-000000000003';
  const event = '00000000-0000-4000-8000-000000000004';
  const sql = async name => readFile(new URL(`../../supabase/${name}`, import.meta.url), 'utf8');
  const as = async (role, id = '') => {
    await db.exec('reset role');
    await db.query("select set_config('request.jwt.claim.sub',$1,false)", [id]);
    await db.exec(`set role ${role}`);
  };
  const save = metrics => db.query("select save_event_preferences(array['ja'],array['music'],array['research'],array['conversation'],true,$1)", [metrics]);
  const roster = (rows, evidence = 'roster:synthetic-test') => db.query(
    'select import_verified_attendance($1,$2,$3) as result', [event, JSON.stringify(rows), evidence]);
  try {
    await db.exec(`create role anon; create role authenticated; create role service_role bypassrls;
      create schema auth;
      create table auth.users(id uuid primary key,email text,raw_user_meta_data jsonb default '{}');
      create function auth.uid() returns uuid language sql stable as $$
        select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid $$;
      grant usage on schema public,auth to anon,authenticated,service_role;
      grant execute on function auth.uid() to anon,authenticated,service_role;`);
    await db.exec((await sql('schema.sql')).replace('create extension if not exists pgcrypto;', ''));
    await db.exec(await sql('organizations_and_calendar.sql'));
    const migration = await sql('event_engagement.sql');
    await db.exec(migration);
    await db.exec(migration);
    for (const id of [user,other,host]) await db.query('insert into auth.users(id,email) values($1,$2)', [id,`${id}@example.test`]);
    await db.query("insert into events(id,title,host_id,starts_at,ends_at) values($1,'Synthetic test',$2,now()-interval '1 hour',now()+interval '1 hour')", [event,host]);
    await as('anon');
    await assert.rejects(db.query('select * from event_preferences'));
    await assert.rejects(db.query('select withdraw_event_preferences()'));
    await as('authenticated',user);
    await save(true);
    await save(true);
    assert.equal((await db.query('select * from event_consent_history')).rows.length,1);
    await assert.rejects(db.query("update event_preferences set metrics_opt_in=false"));
    await assert.rejects(db.query("select save_event_preferences(array['xx'],'{}','{}','{}',false,false)"));
    await assert.rejects(db.query("select save_event_preferences(array['ja'],array['politics'],'{}','{}',false,false)"));
    await assert.rejects(db.query("select save_event_preferences(array[null]::text[],'{}','{}','{}',false,false)"));
    await as('authenticated',other);
    assert.equal((await db.query('select * from event_preferences')).rows.length,0);
    assert.equal((await db.query('select * from event_consent_history')).rows.length,0);
    const timestamp = (await db.query('select clock_timestamp() as t')).rows[0].t;
    const time = new Date(timestamp).toISOString();
    const rows = [{user_id:user,checked_in_at:time},{user_id:other,checked_in_at:time}];
    await assert.rejects(roster(rows), /Only this event host/);
    await as('authenticated',host);
    assert.deepEqual((await roster(rows)).rows[0].result, {verified:2,unchanged:0});
    assert.deepEqual((await roster(rows)).rows[0].result, {verified:0,unchanged:2});
    await assert.rejects(roster([rows[0],rows[0]]), /Duplicate/);
    await assert.rejects(roster([{...rows[0],checked_in_at:'2099-01-01T00:00:00Z'}]), /window/);
    await assert.rejects(roster([{...rows[0],email:'private@example.test'}]), /Expected/);
    await assert.rejects(roster(rows,'https://private-roster'), /Invalid batch/);
    await as('authenticated',user);
    assert.equal((await db.query('select * from event_participations')).rows.length,1);
    assert.equal((await db.query('select analytics_allowed from event_participations')).rows[0].analytics_allowed,true);
    await db.query('select withdraw_event_preferences()');
    const withdrawn = (await db.query('select * from event_preferences')).rows[0];
    assert.equal(withdrawn.metrics_opt_in,false);
    assert.equal(withdrawn.matching_opt_in,false);
    assert.deepEqual(withdrawn.languages,[]);
    assert.equal((await db.query('select analytics_allowed from event_participations')).rows[0].analytics_allowed,false);
    await save(true);
    assert.equal((await db.query('select analytics_allowed from event_participations')).rows[0].analytics_allowed,false);
    await as('authenticated',other);
    await save(true);
    assert.equal((await db.query('select analytics_allowed from event_participations')).rows[0].analytics_allowed,false);
    await assert.rejects(db.query('delete from event_participations'));
    await assert.rejects(db.query('delete from event_consent_history'));
    // A current grant must not backdate permission for an earlier check-in.
    await as('postgres');
    await db.query('delete from event_participations where user_id=$1',[other]);
    await as('authenticated',host);
    await roster([rows[1]]);
    await as('authenticated',other);
    assert.equal((await db.query('select analytics_allowed from event_participations')).rows[0].analytics_allowed,false);
    await as('postgres');
    await db.exec(await readFile(new URL('../../docs/event-engagement-metrics.sql',import.meta.url),'utf8'));
  } finally { await db.close(); }
});
