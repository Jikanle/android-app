# Running And Testing Jikanle

How to see the app on this machine, get it onto a phone, and keep that phone updated
as development continues. Last checked: 2026-09-01.

## 1. Build state

`./gradlew :app:assembleDebug` is green. The debug APK lands at
`app/build/outputs/apk/debug/app-debug.apk` (~30 MB).

`JAVA_HOME` is not set system-wide on the founder's machine. Every Gradle command in
this document assumes:

```bash
export JAVA_HOME="$HOME/.local/share/JetBrains/Toolbox/apps/android-studio/jbr"
```

The app opens directly on the Fuyu no Hanashi lesson reader (`NavGraph.kt`
`startDestination`). `LoginScreen`, `ProfileScreen`, and `SongbridgeScreen` are
registered routes; `Songbridge` currently has no navigation entry point.

## 2. Emulator (Android Studio)

The SDK on this machine has no `cmdline-tools`, so `avdmanager` is unavailable and an
AVD cannot be created from the terminal. Create it in the IDE:

1. Android Studio → **View → Tool Windows → Device Manager** (or the phone icon in the
   right sidebar).
2. **Add a new device → Create Virtual Device**.
3. Pick **Pixel 8** (or any phone profile), then — importantly — the system image
   **API 36.1, Google Play, x86_64**. That exact variant is the only one already on disk
   (`$ANDROID_HOME/system-images/android-36.1/google_apis_playstore/x86_64`, 2.4 GB).
   Any other API level, ABI, or "Google APIs"-without-Play variant triggers a fresh
   multi-gigabyte download.
4. Finish, then press ▶ next to the AVD to boot it.
5. With the emulator running, hit **Run ▶** in the toolbar (or `Shift+F10`).

**Disk warning:** the home partition is at ~93% (8 GB free). An AVD's user-data image
grows to several gigabytes. A previous disk-full event on this machine corrupted the
Gradle journal cache. Prefer the physical phone below; create the AVD only when a
second form factor is genuinely needed.

## 3. Physical phone (the recommended path)

### One-time setup on the phone

1. **Settings → About phone** → tap **Build number** 7 times → developer mode on.
2. **Settings → System → Developer options** → enable **USB debugging**.
3. Plug the phone in over USB and accept the *Allow USB debugging?* prompt (tick
   "Always allow from this computer").

### Verify the connection

```bash
$HOME/Android/Sdk/platform-tools/adb devices -l
```

The phone should appear as `device` (not `unauthorized` — that means the on-phone
prompt was not accepted).

### Install and iterate

```bash
export JAVA_HOME="$HOME/.local/share/JetBrains/Toolbox/apps/android-studio/jbr"
./gradlew :app:installDebug
```

That is the whole update loop: one command rebuilds and reinstalls over the previous
build. Read the phone's runtime logs with:

```bash
ADB="$HOME/Android/Sdk/platform-tools/adb"
"$ADB" logcat --pid="$("$ADB" shell pidof -s co.com.jikanle)"
```

(`adb` is not on this machine's `PATH`; either use the full path as above or add
`$HOME/Android/Sdk/platform-tools` to `PATH`.)

### Wireless (no cable)

Android 11+, phone and PC on the same Wi-Fi. On the phone: **Developer options →
Wireless debugging → Pair device with pairing code**, then:

```bash
ADB="$HOME/Android/Sdk/platform-tools/adb"
"$ADB" pair <IP>:<PAIRING-PORT>     # code from the phone screen
"$ADB" connect <IP>:<DEBUG-PORT>    # the port shown on the Wireless debugging screen
```

`installDebug` then works exactly the same with no cable attached.

## 4. Seeing the phone on the PC screen

`scrcpy` is already installed at `/usr/bin/scrcpy`. With the phone connected over adb:

```bash
scrcpy
```

It mirrors the phone to a desktop window and forwards mouse/keyboard input, at zero
disk cost. This is the cheapest way to demo the app from the laptop and to capture the
Play Store screenshots required by `play-store-launch.md`.

## 5. Signature gotcha

Debug builds and release builds are signed with different keys, and Android refuses to
install one over the other. Switching between them requires
`adb uninstall co.com.jikanle` first — which also wipes local app data. Stay on debug
builds for day-to-day iteration; use release builds only when producing a tester
artifact.

## 6. Tester distribution tracks

Three distinct things, often confused:

| Track | Who | Limit | Requirement |
|---|---|---|---|
| **Direct APK / Obtainium** | The founder and a handful of trusted people | none | GitHub Release with an attached APK |
| **Play internal testing** | Early testers | 100 | Play Console account, signed `.aab` |
| **Play closed testing** | Production-access gate | 12+ testers, 14 consecutive days | Required before applying for production |

### Direct APK track (available today, no Play account needed)

`.github/workflows/android-release.yml` (manual dispatch) builds a signed `.aab` **and**
a signed `.apk`, and — with `publish_release: true` — attaches the APK to a GitHub
Release. `versionCode` comes from the workflow run number, so each dispatch outranks the
build already on a tester's phone.

Testers install [Obtainium](https://github.com/ImranR98/Obtainium), add
`https://github.com/Jikanle/android-app`, and receive every new Release as an in-place
update.

**This track is not usable until the four `SIGNING_*` secrets listed in
`play-store-launch.md` exist in the repository.** Without them Gradle would emit an
unsigned APK, which Android refuses to install; the workflow therefore fails fast on a
missing `SIGNING_KEY_ALIAS` and re-checks that no `*-unsigned.apk` reached the output
directory.

### Play tester roster

Blocked: the Play Console account does not exist yet (see the unchecked Founder
Checklist in `play-store-launch.md` — $25 fee, identity verification, app entry). There
is no track to add emails to until those are done.

Accounts to enroll the moment the internal-testing track exists:

- `alesanchezpov@gmail.com` — founder, personal.
- *(business address — pending; must be a Google account to accept a Play invitation)*

**Do not commit attendee or tester emails to this repository — it is public.** The
roster of Casa Alternativa attendees belongs in Play Console (or a Google Group used as
the tester list), never in git.

## 7. Tracking progress

Per the Session End Checklist in `CLAUDE.md`, each session ends with a commit, a
`ROADMAP.md` update or a note in `docs/session-logs/`, and a green
`./gradlew :app:assembleDebug`. `ROADMAP.md` is the running record of what has shipped.
