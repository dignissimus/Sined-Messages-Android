package me.ezeh.sined

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import ca.uol.aig.fftpack.RealDoubleFFT
import me.ezeh.sined.FrequencyConstants.DEFAULT_SAMPLE_SIZE
import me.ezeh.sined.FrequencyConstants.SAMPLE_RATE
import kotlin.math.abs
import kotlin.math.log10

class FrequencyListener(
    var threshold: Double,
    private val bitFrequencies: List<Int>,
    var bitRange: Int,
    private val callback: (List<Int>) -> Any,
    private val bufferSize: Int = DEFAULT_SAMPLE_SIZE
) : Thread() {
    private var isRunning = false

    override fun run() {
        isRunning = true
        receiveFrequencies()
    }

    fun startListening() {
        println("Starting the frequency listener")
        if (!isRunning)
            start()
    }

    fun stopListening() {
        println("Stopping the frequency listener!")
        if (isRunning) {
            callback(bitFrequencies.map { 0 })
            isRunning = false

            try {
                join()
                interrupt()
            } catch (exception: InterruptedException) {
                exception.printStackTrace()
            }
        }
    }

    private fun receiveFrequencies() {
        /* val bufferSize = sampleSize AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )*/

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord.startRecording()

        while (isRunning) {
            val buffer = ShortArray(bufferSize)
            audioRecord.read(buffer, 0, bufferSize)

            val fft = RealDoubleFFT(bufferSize) // One-dimensional FFT object
            val rawAudioData = buffer.map { it.toDouble() }.toDoubleArray()
            fft.ft(rawAudioData) // Calculates fft
            val audioData = rawAudioData.map { 20 * log10(abs(it)) }
            val frequencies =
                (1..bufferSize).map { FrequencyConstants.DEFAULT_MAX_FREQUENCY * it / bufferSize }

            val frequencyData = frequencies.zip(audioData)

            val bits = bitFrequencies.map { bitFrequency ->
                frequencyData.map { (detectedFrequency, amplitude) ->
                    abs(detectedFrequency - bitFrequency) < bitRange && amplitude > threshold
                }.any { it }
            }
            val intBits = bits.map {
                if (it)
                    1
                else
                    0
            }

            callback(intBits)
        }

        audioRecord.stop()
        audioRecord.release()

    }

    fun refresh() = FrequencyListener(
        threshold,
        bitFrequencies,
        bitRange,
        callback,
        bufferSize
    )
}