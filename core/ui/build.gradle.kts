// :core:ui — design system: high-contrast theme, large-target controls, status banners.
plugins {
    alias(libs.plugins.fieldcapture.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.fieldcapture.ui"
    buildFeatures.compose = true
}

dependencies {
    implementation(project(":core:model"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
