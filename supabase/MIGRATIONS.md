# Shared Supabase schema

The Android app and Jikanle website use the same Supabase contract. The dated files in `migrations/` are immutable snapshots of the four existing SQL scripts. Each migration is byte-for-byte identical to its corresponding root script at the time of this change. New schema changes should be added as new dated migrations, then reflected in app queries and types as needed.

## Staging

The Supabase project `Jikanle-Staging` (`sgcanrytiocycvyflumv`) is a separate database in the same `us-east-1` region as production. From an isolated checkout of this repository:

```bash
supabase login
supabase link --project-ref sgcanrytiocycvyflumv
supabase projects list                 # confirm only Jikanle-Staging says linked: true
supabase db push --dry-run --linked
supabase db push --linked
supabase migration list
```

The link is local checkout metadata; it does not change the Android app's `local.properties` or the website's hosting environment variables. To test Android against staging, configure a dedicated debug/test build with the staging project URL and anon key. Keep release builds on the production project. For the website, use staging URL and anon key in a preview deployment and production values in the public deployment. Never put a service-role key in a client.

## Existing production project

`Jikanle-Android` (`ystsbpumdlhqlmjdymbl`) predates the migration directory and has been managed with standalone SQL scripts. Do not run `supabase db push` against it until its live schema and migration history have been reconciled with these snapshots. In particular, replaying the initial migrations on an already populated production database can conflict with existing objects or data.

Coordinate one migration owner for production. Capture or verify the live baseline, then mark the matching historical migrations as applied before pushing only changes that production actually needs. The `website_v02.sql` change must be verified separately for production; the presence of a file in Git does not mean it has been applied to the database.

Do not edit, commit or print `local.properties`, database passwords, access tokens, or service-role keys.
