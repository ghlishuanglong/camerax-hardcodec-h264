package com.camerax.hardcodec.h264.util

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import java.util.concurrent.ArrayBlockingQueue

class HardDecodeUtils : Thread() {

    private val hardDecodeQueue = ArrayBlockingQueue<ByteArray>(50, true)
    private var isStopHardDecode = false

    //The name of the codec to be instantiated.
    private val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC

    //The width of the content (in pixels)
    var width: Int = 480

    //The height of the content (in pixels)
    var height: Int = 640
    private lateinit var mediaCodec: MediaCodec

    fun init(surface: Surface) {
        isStopHardDecode = true

        mediaCodec = MediaCodec.createDecoderByType(mimeType)
        val mediaFormat = MediaFormat.createVideoFormat(mimeType, height, width)
        mediaCodec.configure(mediaFormat, surface, null, 0)
        val outputFormat = mediaCodec.outputFormat
        mediaCodec.start()
    }


    fun setData(data: ByteArray) {
        hardDecodeQueue.offer(data)
    }

    override fun run() {
        super.run()
        while (isStopHardDecode) {
            val data = hardDecodeQueue.poll()
            if (data != null && data.isNotEmpty()) {
                decode(data)
            }
        }
        hardDecodeQueue.clear()
    }

    private fun decode(data: ByteArray) {
        while (true) {
            val inputBufferIndex = mediaCodec.dequeueInputBuffer(-1)
            if (inputBufferIndex >= 0) {
                val inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex)
                inputBuffer!!.put(data)
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, data.size, 1000000, 0)
            }

            val bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
            val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000)
            if (outputBufferIndex >= 0) {
                val outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
                val outputFormat = mediaCodec.getOutputFormat(outputBufferIndex)
                val remaining = outputBuffer!!.remaining()
                val h264Data = ByteArray(remaining)
                outputBuffer.get(h264Data)
                mediaCodec.releaseOutputBuffer(outputBufferIndex, true)
            }
        }
    }
}