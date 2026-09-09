// :core:location — Fused Location Provider wrapper, external NMEA receiver (later), recording
// foreground service, track buffering to the sidecar file.
plugins {
    alias(libs.plugins.fieldcapture.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.fieldcapture.location"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.play.services.location)
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
