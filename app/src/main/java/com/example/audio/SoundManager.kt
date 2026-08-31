package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Built-in high-fidelity audio synthesizer using Android AudioTrack.
 * Generates crisp, realistic game sound effects with zero latency and no external assets required.
 */
class SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 44100
    var isSoundEnabled: Boolean = true

    private fun playPcm(samples: ShortArray) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val bufferSize = samples.size * 2
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()

                // Release track after playback
                val durationMs = (samples.size * 1000L) / sampleRate + 50
                kotlinx.coroutines.delay(durationMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Silently ignore audio track errors on restricted environments
            }
        }
    }

    /**
     * Crisp dice rattle / tumble sound
     */
    fun playDiceRoll() {
        if (!isSoundEnabled) return
        val durationMs = 280
        val numSamples = (sampleRate * durationMs) / 1000
        val samples = ShortArray(numSamples)

        val clicks = intArrayOf(20, 60, 110, 160, 210, 250)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var sample = 0.0

            for (clickTimeMs in clicks) {
                val clickSample = (clickTimeMs * sampleRate) / 1000
                val dt = i - clickSample
                if (dt in 0..1200) {
                    val progress = dt.toDouble() / 1200.0
                    val envelope = exp(-progress * 8.0)
                    val freq = 900.0 + (clickTimeMs * 3.0)
                    sample += sin(2.0 * PI * freq * (dt.toDouble() / sampleRate)) * envelope * 0.7
                }
            }
            // Add subtle wooden resonance
            sample += sin(2.0 * PI * 320.0 * t) * exp(-t * 12.0) * 0.15
            val clamped = (sample * Short.MAX_VALUE * 0.75).coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
            samples[i] = clamped.toInt().toShort()
        }
        playPcm(samples)
    }

    /**
     * Pleasant wooden / marimba step pop for token movement
     */
    fun playTokenStep(stepIndex: Int = 0) {
        if (!isSoundEnabled) return
        val durationMs = 90
        val numSamples = (sampleRate * durationMs) / 1000
        val samples = ShortArray(numSamples)

        // Pitch goes up slightly as token advances
        val baseFreq = 440.0 + (stepIndex % 8) * 35.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-t * 35.0)
            val tone = sin(2.0 * PI * baseFreq * t) + 0.4 * sin(2.0 * PI * (baseFreq * 2.0) * t)
            val sample = tone * envelope * 0.8
            val clamped = (sample * Short.MAX_VALUE * 0.85).coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
            samples[i] = clamped.toInt().toShort()
        }
        playPcm(samples)
    }

    /**
     * Dramatic triumphant capture sound (whoosh + descending strike)
     */
    fun playCapture() {
        if (!isSoundEnabled) return
        val durationMs = 380
        val numSamples = (sampleRate * durationMs) / 1000
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-t * 8.0)
            // Descending power chord
            val f1 = 650.0 - t * 400.0
            val f2 = 820.0 - t * 500.0
            val f3 = 1040.0 - t * 600.0
            val tone = (sin(2.0 * PI * f1 * t) + sin(2.0 * PI * f2 * t) * 0.8 + sin(2.0 * PI * f3 * t) * 0.6) / 2.4
            val sample = tone * envelope
            val clamped = (sample * Short.MAX_VALUE * 0.9).coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
            samples[i] = clamped.toInt().toShort()
        }
        playPcm(samples)
    }

    /**
     * Harmonic sparkling chime when a token enters home safe base or goal
     */
    fun playHomeReach() {
        if (!isSoundEnabled) return
        val durationMs = 450
        val numSamples = (sampleRate * durationMs) / 1000
        val samples = ShortArray(numSamples)

        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val noteLengthMs = durationMs / notes.size

        for (i in 0 until numSamples) {
            val currentMs = (i * 1000) / sampleRate
            val noteIdx = (currentMs / noteLengthMs).coerceIn(0, notes.size - 1)
            val noteStartSample = (noteIdx * noteLengthMs * sampleRate) / 1000
            val tNote = (i - noteStartSample).toDouble() / sampleRate
            val env = exp(-tNote * 12.0)
            val tone = sin(2.0 * PI * notes[noteIdx] * tNote) + 0.3 * sin(2.0 * PI * (notes[noteIdx] * 2) * tNote)
            val sample = tone * env * 0.75
            val clamped = (sample * Short.MAX_VALUE * 0.8).coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
            samples[i] = clamped.toInt().toShort()
        }
        playPcm(samples)
    }

    /**
     * Festive victory fanfare for winning player
     */
    fun playVictory() {
        if (!isSoundEnabled) return
        val durationMs = 900
        val numSamples = (sampleRate * durationMs) / 1000
        val samples = ShortArray(numSamples)

        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98) // Arpeggio to high G6
        val noteLen = durationMs / notes.size

        for (i in 0 until numSamples) {
            val currentMs = (i * 1000) / sampleRate
            val noteIdx = (currentMs / noteLen).coerceIn(0, notes.size - 1)
            val noteStart = (noteIdx * noteLen * sampleRate) / 1000
            val tNote = (i - noteStart).toDouble() / sampleRate
            val env = exp(-tNote * 6.0)
            val tone = sin(2.0 * PI * notes[noteIdx] * tNote) + 0.4 * sin(2.0 * PI * notes[noteIdx] * 2 * tNote)
            val sample = tone * env * 0.8
            val clamped = (sample * Short.MAX_VALUE * 0.85).coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
            samples[i] = clamped.toInt().toShort()
        }
        playPcm(samples)
    }

    /**
     * Tactile UI button click
     */
    fun playClick() {
        if (!isSoundEnabled) return
        val durationMs = 30
        val numSamples = (sampleRate * durationMs) / 1000
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val sample = sin(2.0 * PI * 1200.0 * t) * exp(-t * 80.0) * 0.5
            val clamped = (sample * Short.MAX_VALUE).coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
            samples[i] = clamped.toInt().toShort()
        }
        playPcm(samples)
    }
}
