package com.camerax.hardcodec.h264.util

import java.util.concurrent.ArrayBlockingQueue

class HardDecodeUtils : Thread {

    private val hardDecodeQueue = ArrayBlockingQueue<ByteArray>(50, true)
    private var isStopHardDecode = false

    constructor() : super() {
        isStopHardDecode = true
    }


    fun setData(data: ByteArray) {
        hardDecodeQueue.offer(data)
    }

    override fun run() {
        super.run()
        while (isStopHardDecode) {
            val data = hardDecodeQueue.poll()
            if (data != null && data.isNotEmpty()) {

            }
        }
        hardDecodeQueue.clear()
    }

    private fun decode(data: ByteArray) {

    }
}