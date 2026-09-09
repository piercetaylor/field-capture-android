import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Convention plugin for Android library modules (`:core:*`).
 *
 * Responsibility: apply `com.android.library` and `org.jetbrains.kotlin.android`, pin SDK levels
 * from the version catalog, set JVM 17, enable unit-test Android resources, and add the
 * coroutine + JUnit test baseline. Modules add only their own dependencies.
 *
 * Interface: apply with `plugins { alias(libs.plugins.fieldcapture.android.library) }`.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<LibraryExtension> {
                compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()
                defaultConfig {
                    minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                testOptions.unitTests.isIncludeAndroidResources = true
                lint {
                    warningsAsErrors = false
                    abortOnError = true
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                    allWarningsAsErrors.set(false)
                }
            }

            dependencies {
                add("implementation", libs.findLibrary("kotlinx-coroutines-android").get())
                add("testImplementation", libs.findLibrary("junit4").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
                add("androidTestImplementation", libs.findLibrary("androidx-test-ext-junit").get())
            }
        }
    }
}
