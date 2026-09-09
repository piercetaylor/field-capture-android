/*
 * :app — Application class.
 *
 * Responsibility: Hilt root component and manual WorkManager initialisation so that export and
 * weather/soil fetch workers can be Hilt-injected. Nothing else lives here; app-level state is
 * in ViewModels owned by feature modules.
 *
 * Interface: declared in AndroidManifest.xml as android:name=".FieldCaptureApp".
 */
package org.fieldcapture.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FieldCaptureApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
