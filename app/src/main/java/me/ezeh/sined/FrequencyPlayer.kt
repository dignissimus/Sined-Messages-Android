package me.ezeh.sined

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import me.ezeh.sined.FrequencyConstants.DEFAULT_SAMPLE_SIZE
import me.ezeh.sined.FrequencyConstants.SAMPLE_RATE
import kotlin.math.PI
import kotlin.math.sin

class FrequencyPlayer(private val frequencies: List<Int>, private val bufferSize: Int = DEFAULT_SAMPLE_SIZE) :
    Thread() {
    private var isRunning = false
    private val amplification = 10000
    private val xs = generateSequence(0) { it + 1 }.map { phase ->
        amplification * frequencies.map { sin(2 * PI * it * phase / SAMPLE_RATE) }.sum()
    }

    override fun run() {
        super.run()
        isRunning = true
        playFrequencies()
    }

    fun startPlaying() {
        println("Starting the frequency player!")
        if (!isRunning)
            start()
    }

    fun stopPlaying() {
        println("Stopping the frequency player!")
        if (isRunning) {
            isRunning = false

            try {
                join()
                interrupt()
            } catch (exception: InterruptedException) {
                exception.printStackTrace()
            }
        }
    }

    private fun playFrequencies() {
        /*val bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )*/

        val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioTrack.Builder()
                .setBufferSizeInBytes(bufferSize)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat
                        .Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } else {
            @Suppress("deprecation")
            AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                AudioTrack.MODE_STREAM
            )
        }

        println("Using a sample rate of ${audioTrack.sampleRate}")
        audioTrack.play()


        // Not sure why this doesn't work
        /*var xs = xs

        while (isRunning) {
            val next = xs.take(bufferSize)
            xs = xs.drop(bufferSize)

            audioTrack.write(
                next.map { it.toShort() }.toList().toShortArray(),
                0,
                bufferSize
            )
        }*/

        val samples = ShortArray(bufferSize)
        var phase = 0

        while (isRunning) {
            for (i in 0 until bufferSize) {
                val value = frequencies.map { sin(phase * 2 * PI * it / SAMPLE_RATE) }.sum()
                samples[i] = (amplification * value).toShort()
                phase += 1
            }
            audioTrack.write(samples, 0, bufferSize)
        }

        audioTrack.stop()
        audioTrack.release()
    }

    fun refresh() = FrequencyPlayer(frequencies)
}