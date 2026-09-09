# Vosk as the default offline speech engine; platform SpeechRecognizer optional; whisper.cpp for batch re-transcription

- Status: accepted
- Date: 2026-09-04

## Context and Problem Statement

Planter and walk modes need a transcript of every spoken note plus the audio itself, with no
connectivity, over tractor noise, with a vocabulary dominated by plot and cross identifiers. Which
speech-to-text engine(s) should the app use?

## Decision Drivers

- Offline and streaming (results while speaking; a final result at utterance end with a timestamp).
- The raw audio must be captured for every note regardless of recognition outcome.
- Constrained vocabulary: the planting order gives an exact list of plausible IDs.
- License compatibility with MIT (ADR 0007) and F-Droid rules.
- Model size and CPU cost on mid-range devices.

## Considered Options

### Android `SpeechRecognizer`

The platform API: `createOnDeviceSpeechRecognizer` and `isOnDeviceRecognitionAvailable` were added in
API 31; `RecognizerIntent.EXTRA_PREFER_OFFLINE` in API 23; `EXTRA_AUDIO_SOURCE` (feed the recognizer an
already-open audio file descriptor) and `EXTRA_SEGMENTED_SESSION` in API 33 [web:
https://developer.android.com/reference/android/speech/SpeechRecognizer,
https://developer.android.com/reference/android/speech/RecognizerIntent]. The class documentation states
the implementation "is likely to stream audio to remote servers" and "is not intended to be used for
continuous recognition" [web: SpeechRecognizer]. On-device availability depends on the OEM's speech
service; there is no vocabulary constraint API; before API 33 the recogniser owns the microphone, so
capturing audio alongside it is not guaranteed.

### Vosk (alphacep/vosk-api)

Apache-2.0; `com.alphacephei:vosk-android:0.3.75` on Maven Central with JNA 5.18.1 [web:
https://central.sonatype.com/artifact/com.alphacephei/vosk-android]. Streaming `acceptWaveForm` on PCM
the app already holds, `Recognizer(model, sampleRate, grammar)` and `setGrammar()` take a JSON phrase
list [web: https://raw.githubusercontent.com/alphacep/vosk-api/master/android/lib/src/main/java/org/vosk/Recognizer.java].
Models "are small (50 Mb)" with "zero-latency response with streaming API, reconfigurable vocabulary"
[web: https://raw.githubusercontent.com/alphacep/vosk-api/master/README.md]. The demo app targets
minSdk 21 and bundles the model in assets [web:
https://raw.githubusercontent.com/alphacep/vosk-android-demo/master/app/build.gradle].

### whisper.cpp

MIT; C/C++ with an Android example that copies a tiny/base model into assets and transcribes files
[web: https://raw.githubusercontent.com/ggml-org/whisper.cpp/master/examples/whisper.android/README.md].
Higher accuracy on open speech is widely reported but was not measured here [speculation]; it needs an
NDK build and is batch-oriented in the Android example.

## Decision Outcome

Vosk is the default engine for both modes. The app owns one `AudioRecord` stream at 16 kHz and tees it
to a file and to the recogniser (`AudioTee` in `:core:media`), which is what guarantees audio evidence
exists even when recognition fails. In planter mode the grammar is set to the planting order's IDs in
spoken form plus the command words ("plant", "skip", "redo", "pause", "resume"), and reset with
`setGrammar("[]")` for free-text notes; in walk mode the grammar is the field's plot IDs plus an open
vocabulary fallback. The English small model is downloaded on first use into `models/` (not bundled,
so the APK stays small and F-Droid's no-binary rule is respected by fetching only with explicit consent).

The platform `SpeechRecognizer` is offered as a second engine on devices where
`isOnDeviceRecognitionAvailable` is true, using `EXTRA_AUDIO_SOURCE` on API 33+ so the same PCM feeds
file and recogniser; below API 33 it is not offered, because the audio tee cannot be guaranteed.

whisper.cpp is reserved for M4 as an optional "re-transcribe session" action over recorded walk audio
segments, where batch latency does not matter and accuracy on free-form notes may be better [speculation].

### Consequences

- Positive: deterministic offline behaviour, constrained-vocabulary recognition of IDs, one audio
  pipeline, Apache-2.0/MIT licences only.
- Negative: a 50 MB model download; JNA native libraries add APK size per ABI; Vosk accuracy on
  free-form agronomic notes is unknown until field trials (PLAN.md, "Risks and open questions").
- ID pronunciation mapping ("26YT-0042" → "twenty six Y T zero zero four two" and shorter aliases)
  is a `:core:model` function with tests in M1.
