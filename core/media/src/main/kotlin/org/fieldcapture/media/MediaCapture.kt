/*
 * :core:media — video, photo and EXIF.
 *
 * Responsibility: CameraX Recorder for walk-mode video (FileOutputOptions into the session
 * directory, quality fallback FHD→HD→SD, audio enabled), CameraX ImageCapture for plot photos,
 * and EXIF writing (GPS lat/lon/timestamp, DateTimeOriginal, ImageDescription = plot id,
 * UserComment = sidecar JSON summary). Video start time is captured from the first
 * VideoRecordEvent.Start so annotation offsets can be converted to wall-clock time.
 *
 * Interface: VideoRecorder.start/pause/resume/stop, PhotoCapture.capture(request).
 * File names come from org.fieldcapture.model.MediaPath; nothing here invents a path.
 */
package org.fieldcapture.media

import kotlinx.coroutines.flow.Flow
import org.fieldcapture.model.GpsFix
import org.fieldcapture.model.PlotKey

data class VideoStatus(
    val recording: Boolean,
    val paused: Boolean,
    val durationMs: Long,
    val bytes: Long,
    /** Wall-clock time of the first encoded frame; null until the Start event arrives. */
    val startedAtMs: Long?,
    val error: String?,
)

interface VideoRecorder {
    val status: Flow<VideoStatus>
    suspend fun start(outputPath: String, withAudio: Boolean)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
}

data class PhotoRequest(
    val plot: PlotKey,
    val noteType: String,
    val fix: GpsFix?,
    val bearingDeg: Float?,
    val person: String?,
    val sessionId: String?,
)

data class PhotoResult(val relativePath: String, val sidecarPath: String, val sha256: String, val bytes: Long)

interface PhotoCapture {
    /** Captures, writes EXIF, writes the sidecar JSON, returns paths relative to the storage root. */
    suspend fun capture(request: PhotoRequest): PhotoResult
}
