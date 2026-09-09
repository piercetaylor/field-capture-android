// :feature:walk — see the placeholder screen file for responsibility and interface.
plugins {
    alias(libs.plugins.fieldcapture.android.feature)
}

android {
    namespace = "org.fieldcapture.feature.walk"
}

dependencies {
    implementation(project(":core:location"))
    implementation(project(":core:media"))
    implementation(libs.maplibre.android.sdk)
}
