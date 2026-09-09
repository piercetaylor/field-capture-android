import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Convention plugin for feature modules (`:feature:*`).
 *
 * Responsibility: everything in [AndroidLibraryConventionPlugin] plus Jetpack Compose, Hilt with
 * KSP, and the standard feature dependency set (:core:model, :core:data, :core:ui, Compose BOM,
 * navigation, lifecycle). A feature module is UI + ViewModel for one user journey; domain logic
 * belongs in :core:model and persistence in :core:data (see PLAN.md, "Architecture").
 *
 * Interface: apply with `plugins { alias(libs.plugins.fieldcapture.android.feature) }`.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("fieldcapture.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("com.google.dagger.hilt.android")

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<LibraryExtension> {
                buildFeatures.compose = true
            }

            dependencies {
                add("implementation", project(":core:model"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:ui"))

                val bom = libs.findLibrary("androidx-compose-bom").get()
                add("implementation", platform(bom))
                add("androidTestImplementation", platform(bom))
                add("implementation", libs.findLibrary("androidx-compose-foundation").get())
                add("implementation", libs.findLibrary("androidx-compose-material3").get())
                add("implementation", libs.findLibrary("androidx-compose-ui").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())

                add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())

                add("implementation", libs.findLibrary("hilt-android").get())
                add("ksp", libs.findLibrary("hilt-compiler").get())
            }
        }
    }
}
