package com.camerax.hardcodec.h264.util

import java.nio.ByteBuffer

open class YUVUtils {

    companion object {
        init {
            System.loadLibrary("yuv-utils")
        }

        external fun yuvToNV21(
            width: Int,
            height: Int,
            byteBufferY: ByteBuffer,
            byteBufferYLength: Int,
            byteBufferU: ByteBuffer,
            byteBufferULength: Int,
            byteBufferV: ByteBuffer,
            byteBufferVLength: Int,
        ): ByteArray
    }
}