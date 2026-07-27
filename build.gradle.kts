// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// CI runs these canonical task names even while the project is still a single
// Android module. Replace the aliases with real convention-plugin checks when
// build-logic becomes active.
tasks.register("ktlintCheck") {
    group = "verification"
    description = "Reserved ktlint check entrypoint for CI."
}

tasks.register("detekt") {
    group = "verification"
    description = "Reserved detekt check entrypoint for CI."
}

tasks.register("lint") {
    group = "verification"
    description = "Runs Android lint for the app module."
    dependsOn(":app:lintDebug")
}

tasks.register("test") {
    group = "verification"
    description = "Runs debug JVM unit tests."
    dependsOn(":app:testDebugUnitTest")
}

tasks.register("assembleDebug") {
    group = "build"
    description = "Assembles the debug app."
    dependsOn(":app:assembleDebug")
}
