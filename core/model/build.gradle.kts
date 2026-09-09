// :core:model — pure Kotlin/JVM. No Android dependency, so it builds and tests anywhere a JDK
// exists. Holds the domain model, geometry, file-contract parsers/writers, and plot assignment.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Target Java 17 bytecode (the Android baseline) without requiring a JDK 17 toolchain, so any
// JDK >= 17 can run the tests.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = false
    }
}
