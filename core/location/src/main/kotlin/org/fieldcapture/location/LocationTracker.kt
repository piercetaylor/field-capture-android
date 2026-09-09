/*
 * :core:location — location acquisition and the recording foreground service.
 *
 * Responsibility: expose a Flow<GpsFix> from the Fused Location Provider at a requested rate
 * (planter: on-demand latest fix with an age/accuracy gate; walk: continuous ≥ 1 Hz), report
 * GPS loss as a state rather than an exception, and keep capture alive with a typed foreground
 * service while the screen is off. External NMEA receivers over Bluetooth (RTK rovers, as
 * Field Book's GNSS trait supports) are a later provider behind the same interface.
 *
 * Interface: LocationTracker.fixes(mode), LocationTracker.latest(maxAgeMs, maxAccuracyM),
 * RecordingForegroundService.start/stop(context, kinds). Implementations land in M1.
 */
package org.fieldcapture.location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.flow.Flow
import org.fieldcapture.model.GpsFix

enum class TrackingMode {
    /** High accuracy, 1000 ms interval, no batching. Battery cost accepted while recording. */
    WALK,

    /** High accuracy, 2000 ms interval, batching allowed up to 4000 ms. Fix is read on trigger. */
    PLANTER,

    /** Balanced power, 30 s interval. Used on the map/home screen. */
    IDLE,
}

sealed interface GpsState {
    data object Searching : GpsState
    data class Fixed(val accuracyM: Float, val satellitesUsed: Int?) : GpsState
    data class Lost(val sinceMs: Long) : GpsState
    data object Disabled : GpsState
}

interface LocationTracker {
    fun fixes(mode: TrackingMode): Flow<GpsFix>
    val state: Flow<GpsState>

    /**
     * Latest fix acceptable for a planter event. Returns null when no fix is younger than
     * [maxAgeMs] or more accurate than [maxAccuracyM]; the caller records the event without a
     * position and flags it for interpolation from neighbours (PLAN.md, "Field workflows").
     */
    suspend fun latest(maxAgeMs: Long = 3_000, maxAccuracyM: Float = 10f): GpsFix?
}

/**
 * Foreground service declared in app/src/main/AndroidManifest.xml with
 * foregroundServiceType="location|microphone|camera". Started only from the foreground
 * (while-in-use rule); the kinds actually requested are passed as an extra so planter mode
 * starts with location|microphone and walk mode adds camera.
 */
class RecordingForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // M1: build the notification channel, call startForeground(id, notification, types),
        // hand the active session id to LocationTracker, and stop self on ACTION_STOP.
        return START_NOT_STICKY
    }

    companion object {
        const val ACTION_START = "org.fieldcapture.location.START"
        const val ACTION_STOP = "org.fieldcapture.location.STOP"
        const val EXTRA_KINDS = "kinds"
        const val EXTRA_SESSION_ID = "sessionId"
    }
}
