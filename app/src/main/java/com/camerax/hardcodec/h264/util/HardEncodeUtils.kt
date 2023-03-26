package com.camerax.hardcodec.h264.util

import android.media.MediaCodec
import java.util.concurrent.ArrayBlockingQueue

class HardEncodeUtils : Thread {

    private val hardcodeQueue = ArrayBlockingQueue<ByteArray>(50, true)
    private var isStopHardcode = false
    private lateinit var hardDecodeUtils: HardDecodeUtils

    constructor() : super() {
        isStopHardcode = true
        hardDecodeUtils = HardDecodeUtils()
    }


    fun setData(data: ByteArray) {
        hardcodeQueue.offer(data)
    }

    override fun run() {
        super.run()
        while (isStopHardcode) {
            val data = hardcodeQueue.poll()
            if (data != null && data.isNotEmpty()) {

            }
        }
        hardcodeQueue.clear()
    }

    private fun encode(data: ByteArray) {

        MediaCodec.createByCodecName("")

    }
}