// :feature:integrations — see the placeholder screen file for responsibility and interface.
plugins {
    alias(libs.plugins.fieldcapture.android.feature)
}

android {
    namespace = "org.fieldcapture.feature.integrations"
}

dependencies {
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
}
