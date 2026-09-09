// :app — application shell: navigation host, Hilt root, WorkManager init, permission flow.
// Feature modules own their screens; :app only wires them together.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.fieldcapture.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        // Change before publishing: applicationId must be a domain the maintainer controls.
        applicationId = "org.fieldcapture.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Signing is configured from environment variables in CI (see docs/adr/0001 and PLAN.md,
            // "Distribution"); no keystore or credentials are stored in the repository.
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:location"))
    implementation(project(":core:media"))
    implementation(project(":core:ui"))
    implementation(project(":feature:planter"))
    implementation(project(":feature:walk"))
    implementation(project(":feature:fieldbook"))
    implementation(project(":feature:integrations"))
    implementation(project(":feature:crossing"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.ext.compiler)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
