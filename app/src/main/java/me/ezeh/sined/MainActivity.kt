package me.ezeh.sined

import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.ezeh.sined.FrequencyConstants.DEFAULT_SAMPLE_SIZE
import kotlin.math.floor

class MainActivity : AppCompatActivity() {

    private val frequencies = listOf(18000)

    private val minBitRange = 0
    private val defaultBitRange = 50
    private val maxBitRange = 100

    private val minThreshold = 70.0
    private val defaultThreshold = 80.0
    private val maxThreshold = 150.0

    private val bitFrequencies = (20500..21500 step 100).toList()
    // private val bitFrequencies = (400..1400 step 100).toList()
    // listOf(18000, 18100, 18200, 18300, 18400, 18500, 18600, 18700, 18800, 18900)
    private val callback = { bits: List<Int> -> receivedBits.text = bits.joinToString() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var frequencyPlayer = FrequencyPlayer(frequencies)
        var frequencyListener =
            FrequencyListener(
                defaultThreshold,
                bitFrequencies,
                defaultBitRange,
                callback,
                DEFAULT_SAMPLE_SIZE
            )

        println(bitFrequencies)

        playTones.setOnClickListener {
            //            frequencyPlayer.startPlaying()
            frequencyListener.startListening()
        }

        stopTones.setOnClickListener {
            frequencyPlayer.stopPlaying()
            frequencyPlayer = frequencyPlayer.refresh()

            frequencyListener.stopListening()
            frequencyListener = frequencyListener.refresh()
        }

        thresholdSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, rawProgress: Int, fromUser: Boolean) {
                val progress = rawProgress.toDouble()
                val threshold =
                    minThreshold + (progress / 100) * (maxThreshold - minThreshold)
                frequencyListener.threshold = threshold
                val formattedThreshold = "%.2f".format(threshold)
                thresholdDisplayer.text = "Threshold: ${formattedThreshold}db"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Don't care
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Don't care
            }

        })

        bitRangeSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, rawProgress: Int, fromUser: Boolean) {
                val progress = rawProgress.toDouble()
                val bitRange = floor(
                    minBitRange + (progress / 100) * (maxBitRange - minBitRange)
                ).toInt()

                frequencyListener.bitRange = bitRange
                bitRangeDisplayer.text = "Bit range: $bitRange"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
}
