# Play Store Launch Playbook

Last checked: 2026-07-27.

Google Play currently requires new personal developer accounts to run a closed test with at least 12 opted-in testers for 14 consecutive days before applying for production access. Google says production-access review usually takes 7 days or less, but it may take longer. The target-API floor rises on 2026-08-31: new apps and updates must target Android 16/API 36 or higher. Jikanle already targets API 36. Android developer verification has a 2026-09-30 enforcement milestone for participating stores and countries, and Play developers should check app registration in Play Console.

Sources:

- https://support.google.com/googleplay/android-developer/answer/14151465
- https://support.google.com/googleplay/android-developer/answer/11926878
- https://developer.android.com/developer-verification
- https://android-developers.googleblog.com/2026/06/android-developer-verification.html

## Realistic Timeline

| Week | Track | Work |
|---|---|---|
| Week 1 | Internal testing | Upload signed `.aab`; use internal testing for up to 100 testers via shareable link. |
| Weeks 2-3 | Closed testing | Keep 12+ Casa Alternativa attendees opted in for 14 consecutive days. Recruit from the event RSVP list. |
| Week 4 | Production access | Submit production access application after closed-test criteria are met. |
| Week 5-6 | Production | Resolve review feedback and release publicly when approved. |

## Founder Checklist

- [ ] Pay the $25 one-time Play Console fee at `play.google.com/console/signup` using `alesanchezpov@gmail.com`.
- [ ] Complete identity verification with government ID and address.
- [ ] Enable 2-step verification on the Google account.
- [ ] Create the app entry: name `Jikanle`, default language Spanish (Colombia), category `Education`, free.
- [ ] Complete app registration/developer verification tasks shown in Play Console.
- [ ] Create closed testing track and invite the 12 tester emails from the Casa Alternativa attendee list.

## Store Listing Checklist

- App name: Jikanle.
- Short description, 80 chars max: `Aprende idiomas con musica y con tu gente. Tiempo que cuenta doble.`
- Full description: adapt the README "What is Jikanle" section plus Room, Lesson, and Companion paragraphs.
- Feature graphic: 1024x500, solid `paper` background, 乐 wordmark in `primary`, tagline in Fraunces below.
- Phone screenshots: at least 2 and at most 8, showing the Lesson reader intro and vocabulary slide.
- App icon: 512x512 PNG.
- Content rating: Teen, minimal user-generated content initially.
- Privacy policy URL: `https://jikanle.com.co/legal/privacy`.
- Data safety: email, display name, hobbies, languages, and room/event participation collected; none sold.

## Signing

The upload key is held by Alejandro. Google Play App Signing should be enabled.

GitHub release workflow secrets:

- `SIGNING_KEYSTORE_BASE64`
- `SIGNING_KEY_ALIAS`
- `SIGNING_KEY_PASSWORD`
- `SIGNING_STORE_PASSWORD`
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `GOOGLE_OAUTH_CLIENT_ID`

The manual-dispatch release workflow builds an `.aab` artifact only. Alejandro uploads the first internal-testing build manually. Fastlane `supply` can be added later.
