# Build Logic

This directory is reserved for Gradle convention plugins as the app splits into Android library modules. The current bootstrap keeps the project as a single `:app` module because the existing codebase is already green and the MVP surface is narrow.

Before adding a convention plugin, keep `./gradlew :app:assembleDebug` green and document the module split in `ARCHITECTURE.md`.
