// Project settings. Module set follows the nowinandroid split into :app, :core:*, :feature:*.
//
// JVM-only mode: `gradle -PjvmOnly=true :core:model:test` configures only the pure-Kotlin
// modules so the domain tests can run on a machine without an Android SDK (CI containers,
// analysis servers). Full builds omit the property.

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "field-capture-android"

// Pure-Kotlin modules: no Android dependency, runnable anywhere with a JDK.
include(":core:model")

val jvmOnly = providers.gradleProperty("jvmOnly").map { it.toBoolean() }.getOrElse(false)

if (!jvmOnly) {
    include(":app")
    include(":core:data")
    include(":core:location")
    include(":core:media")
    include(":core:ui")
    include(":feature:planter")
    include(":feature:walk")
    include(":feature:fieldbook")
    include(":feature:integrations")
    include(":feature:crossing")
} else {
    logger.lifecycle("jvmOnly=true: Android modules are not configured; only :core:model is available.")
}
