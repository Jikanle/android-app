# Jikanle Content Policy

## Purpose

Jikanle teaches through music, but the MVP must not depend on copyrighted audio or full lyrics that the project does not have permission to use.

## Allowed In The MVP

- Song metadata: title, artist, language, context, and external links.
- Creator-authored lesson notes, grammar explanations, cultural notes, and discussion prompts.
- Short vocabulary picks chosen by the creator.
- Public-domain or Creative Commons material when the license is documented.
- Commercial-song clips only when Alejandro confirms permission, license, or a safe use path before the clip enters the app or repository.

## Not Allowed Without Review

- Full copyrighted lyrics committed to git or bundled in the app.
- Copyrighted audio files committed to git or bundled in the app.
- Auto-mined lyric/sentence databases from commercial songs.
- In-app claims that Jikanle owns, licenses, or distributes a commercial track unless that is true.

## Commercial Songs, Including Stray Kids

For a commercial song such as a Stray Kids release, the MVP may show:

- A seasonal feature card.
- Song title, artist, language, and context.
- Creator-authored vocabulary and discussion prompts.
- A link to the official listening source.
- A note that full playback opens externally unless a licensed clip is available.

If a short clip is used, document the permission source outside git and store only the approved asset path or URL. Do not commit private license documents to this repository.

## Agent Rule

If an AI coding agent is asked to add a commercial song, it must implement metadata and lesson scaffolding only. It must not add full lyrics or audio unless the user provides explicit written confirmation that the asset is cleared.
