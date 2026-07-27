# Security Policy

Report security issues privately to `alesanchezpov@gmail.com`.

## Secrets

- `local.properties` is ignored and must never be committed.
- Supabase anon keys are public-safe by design when RLS is correct.
- Supabase `service_role` keys must never appear in this repository.
- Signing keys live only in GitHub encrypted secrets or Alejandro's local secure storage.
- Session tokens must never be logged.

## Release Signing

GitHub Actions expects these encrypted secrets for release builds:

- `SIGNING_KEYSTORE_BASE64`
- `SIGNING_KEY_ALIAS`
- `SIGNING_KEY_PASSWORD`
- `SIGNING_STORE_PASSWORD`
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `GOOGLE_OAUTH_CLIENT_ID`

## Data Handling

The MVP may collect email, display name, languages, city, hobbies, and room participation. Do not collect analytics, precise location, contacts, or microphone recordings without a new security and privacy review.
