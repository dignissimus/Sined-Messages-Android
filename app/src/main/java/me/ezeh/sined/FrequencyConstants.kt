package me.ezeh.sined

object FrequencyConstants {
    const val SAMPLE_RATE = 48000
    const val DEFAULT_SAMPLE_SIZE = 4096
    const val DEFAULT_MAX_FREQUENCY = SAMPLE_RATE / 2
    private const val DEFAULT_GUARD_BANDWIDTH = 50
    const val DEFAULT_BIT_RANGE = DEFAULT_GUARD_BANDWIDTH * 2
}