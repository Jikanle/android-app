# Jikanle Brand Manual

## Thesis

Jikanle is built on a single belief: most hours people spend trying to learn a language are also hours they spend alone, and that is the bug, not the user. Music is the glue between learning and belonging.

## Tagline

- Spanish: Tiempo que cuenta doble.
- English: Time that counts twice.
- Japanese: 時間が二度生きる時
- Chinese: 时间加倍的时光

## Palette

These values match the web app's CSS variables exactly. They are the brand. Mirror them as Compose `Color` definitions, Material 3 theme tokens, and document them in `BRANDING.md` verbatim.

| Token | Hex | Use |
|---|---|---|
| `ink` | `#1F1438` | Deep aubergine. Primary text on light backgrounds; primary surface in dark theme. |
| `ink-soft` | `#5B4B7A` | Secondary text. |
| `ink-faint` | `#9C8FB8` | Tertiary text, dividers. |
| `primary` | `#6D2DD3` | Vivid violet. Principal brand color — CTAs, active states, brand accents. |
| `pink` | `#E63B96` | Vivid magenta. Energy accent — used sparingly for highlights, "now playing", live indicators. |
| `blue` | `#3B5BDB` | Secondary. Use only for non-primary actions and links inside body text. |
| `paper` | `#FBF7F8` | Default background, light theme. |
| `paper-deep` | `#F2EAF5` | Card backgrounds, light theme. |
| `paper-rule` | `#E5D6E5` | Hairline dividers. |

Build both Material 3 light *and* dark themes. Dark theme uses `ink` as background with `paper` as primary text and `primary` at reduced saturation for CTAs.

## Typography

- **Display:** Fraunces (variable). Bundle as `res/font/fraunces.ttf`. Use for hero headings, Lesson titles, section anchors.
- **Body:** Instrument Sans. Bundle as `res/font/instrument_sans.ttf`. Use for all body copy.
- **CJK:** Noto Serif JP. Bundle as `res/font/noto_serif_jp.ttf`. Fallback to Noto Sans CJK for Chinese glyphs where Serif JP lacks coverage.

Implement `JikanleTypography` in `core/design/` exposing `display`, `body`, `cjk` styles. Write `hasCJK(text: String): Boolean` that returns true for any codepoint in U+3040–309F (Hiragana), U+30A0–30FF (Katakana), or U+4E00–9FFF (CJK Unified). Lyric-line Composables must auto-select the CJK style when `hasCJK` is true.

Android currently uses Google downloadable fonts to keep the repository light until `jikanle/brand` ships bundled font files. When the brand repo is available, replace the downloadable families with `res/font/` assets without changing public `JikanleTypography` names.

## Structural Device: Bilingual Section Anchors

The web uses these as navigation anchors: **時間 / 出会い / 乐 / 続き / 次** (time / encounter / music-joy / continuity / next). Preserve them on Android as section headers in long-scrolling screens. They are part of the brand — never localize them away.

## Voice

- Warm, adult, plainspoken. No hype. No exclamation marks in UI. No emoji chrome.
- Spanish and English at parity. All strings live in `res/values/strings.xml` (Spanish default) and `res/values-en/strings.xml` (English).
- Japanese and Chinese appear as *content* (lyric lines, vocabulary), not as UI language.
- Never use "AI" as a marketing word in-app. If you must reference it, say "helped by machine learning" once, in `docs/` only.

## Palette Do And Don't

Do use `paper` as the quiet default background and `ink` for readable text. Do use `primary` for selected states, main actions, and the 乐 wordmark. Do reserve `pink` for live or currently-playing moments so it keeps energy.

Do not make violet gradients dominate every screen. Do not use `pink` as a generic button color. Do not put body text on `primary` unless contrast is checked. Do not introduce beige, blue-gray, or orange theme families that compete with the locked palette.

## Logo Integrity

乐 is the logo character. Use it at 24pt or larger. Render it in `primary` or `ink`. Never rotate it, stretch it, outline it, or crowd it. Keep at least 12pt of clearspace on every side. Do not place it inside a busy image or decorative badge.

## When In Doubt

If a feature you're considering doesn't make the following sentence more true, don't build it:

> The eight people who met at Casa Alternativa last Saturday opened the app on Monday morning, found each other, and kept the conversation going.
