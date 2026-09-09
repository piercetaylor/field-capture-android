// :core:media — CameraX video/photo capture, EXIF writing, audio tee, speech-to-text engines.
plugins {
    alias(libs.plugins.fieldcapture.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "org.fieldcapture.media"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.exifinterface)
    // Offline speech-to-text (ADR 0004). Both artifacts are AARs, as in alphacep/vosk-android-demo.
    implementation("com.alphacephei:vosk-android:${libs.versions.vosk.get()}@aar")
    implementation("net.java.dev.jna:jna:${libs.versions.jna.get()}@aar")
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
