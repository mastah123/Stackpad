package com.stackpad.app.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Thin wrapper over [MediaRecorder] that records AAC audio into an .m4a file. */
class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    val isRecording: Boolean get() = recorder != null

    /** Starts recording into [target]. Returns false if the microphone could not be opened. */
    fun start(target: File): Boolean {
        val r = createRecorder()
        return try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(64_000)
            r.setAudioSamplingRate(44_100)
            r.setOutputFile(target.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            true
        } catch (e: Exception) {
            runCatching { r.release() }
            false
        }
    }

    /** Loudest amplitude since the last call (0..32767). */
    fun maxAmplitude(): Int = try {
        recorder?.maxAmplitude ?: 0
    } catch (e: IllegalStateException) {
        0
    }

    /** Stops recording. Returns false if nothing usable was recorded (e.g. stopped too quickly). */
    fun stop(): Boolean {
        val r = recorder ?: return false
        recorder = null
        return try {
            r.stop()
            true
        } catch (e: RuntimeException) {
            false
        } finally {
            r.release()
        }
    }

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
}
