/*
 * :core:media — speech-to-text engines and the audio tee.
 *
 * Responsibility: one AudioRecord stream (16 kHz mono PCM) is split two ways: to a file writer
 * (audio evidence for every note) and to a streaming recogniser. The default engine is Vosk
 * (offline, streaming, grammar-constrained to the planting order's IDs); the platform
 * SpeechRecognizer is an optional engine on devices with on-device recognition (API 31+),
 * fed through EXTRA_AUDIO_SOURCE on API 33+ so the same PCM reaches both consumers. whisper.cpp
 * is reserved for batch re-transcription of walk audio (ADR 0004).
 *
 * Interface: SpeechEngine.start(grammar) / stop(); results as a Flow<SttResult>. Engine choice
 * is a user setting; unavailability degrades to audio-only notes with a banner.
 */
package org.fieldcapture.media

import kotlinx.coroutines.flow.Flow

data class SttResult(
    val text: String,
    val isFinal: Boolean,
    val confidence: Float?,
    /** Wall-clock time the utterance ended, used to index annotations against the GPS track. */
    val endedAtMs: Long,
    val engine: String,
)

sealed interface SttState {
    data object Idle : SttState
    data object Listening : SttState
    data class Unavailable(val reason: String) : SttState
}

interface SpeechEngine {
    val id: String
    val state: Flow<SttState>

    /**
     * Start streaming recognition. [grammar] is an optional phrase list (plot IDs, commands such as
     * "skip", "redo", "pause") that constrains recognition; null means open vocabulary.
     */
    suspend fun start(grammar: List<String>?)
    suspend fun stop()
    val results: Flow<SttResult>
}

/** Vosk engine (com.alphacephei:vosk-android). Model directory: StorageLayout.MODELS/<model-name>. */
class VoskSpeechEngine : SpeechEngine {
    override val id = "vosk"
    override val state: Flow<SttState> get() = TODO("M1")
    override val results: Flow<SttResult> get() = TODO("M1")
    override suspend fun start(grammar: List<String>?) = TODO("M1")
    override suspend fun stop() = TODO("M1")
}

/** android.speech.SpeechRecognizer with createOnDeviceSpeechRecognizer when available. */
class PlatformSpeechEngine : SpeechEngine {
    override val id = "android-speech"
    override val state: Flow<SttState> get() = TODO("M1")
    override val results: Flow<SttResult> get() = TODO("M1")
    override suspend fun start(grammar: List<String>?) = TODO("M1")
    override suspend fun stop() = TODO("M1")
}

/**
 * Splits one microphone stream to a WAV/AAC file and to the active engine. Owning the
 * AudioRecord here (not inside the engine) is what guarantees audio evidence exists even when
 * recognition fails.
 */
interface AudioTee {
    suspend fun start(outputPath: String, sink: (pcm: ShortArray, len: Int) -> Unit)
    suspend fun stop(): AudioSegment
}

data class AudioSegment(val path: String, val startedAtMs: Long, val endedAtMs: Long, val sampleRateHz: Int)
