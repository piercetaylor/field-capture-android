// :feature:fieldbook — see the placeholder screen file for responsibility and interface.
plugins {
    alias(libs.plugins.fieldcapture.android.feature)
}

android {
    namespace = "org.fieldcapture.feature.fieldbook"
}

dependencies {
    implementation(libs.maplibre.android.sdk)
}
