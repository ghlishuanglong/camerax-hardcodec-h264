package com.camerax.hardcodec.h264

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.camerax.hardcodec.h264.databinding.ActivityMainBinding
import com.camerax.hardcodec.h264.util.HardEncodeUtils
import com.camerax.hardcodec.h264.util.NV21ToBitmap
import com.camerax.hardcodec.h264.util.YUVUtils
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mViewBinding: ActivityMainBinding
    private lateinit var mCameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var isStartCodec = false
    private lateinit var mHardEncodeUtils: HardEncodeUtils
    private lateinit var mSurface: Surface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)

        //Check and request permissions
        if (allPermissionsGranted()) {
            startCamera()
            mViewBinding.mBtnStartCodec.setOnClickListener(this)
            mViewBinding.mBtnStopCodec.setOnClickListener(this)
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        mViewBinding.mTextureView.surfaceTextureListener = object :
            TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                mSurface = Surface(surfaceTexture)
                mHardEncodeUtils = HardEncodeUtils()
                mHardEncodeUtils.init(mSurface)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                mSurface.release()
                surface.release()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }

        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.mBtnStartCodec -> {//Start Codec
                isStartCodec = true
                mHardEncodeUtils.startRun()
            }
            R.id.mBtnStopCodec -> {//Stop Codec
                isStartCodec = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startCamera() {
        mCameraProviderFuture = ProcessCameraProvider.getInstance(this)

        mCameraProviderFuture.addListener({

            val cameraProvider: ProcessCameraProvider = mCameraProviderFuture.get()

            //Use the rear camera by default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                val camera =
                    cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis(), preview())
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun preview(): Preview {
        //Set preview related parameters
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(mViewBinding.mPreviewView.surfaceProvider)
        return preview
    }

    private fun imageAnalysis(): ImageAnalysis {

        val nV21ToBitmap = NV21ToBitmap(this)


        //Set image analysis
        val imageAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setTargetResolution(Size(480, 640))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(this),
            ImageAnalysis.Analyzer { imageProxy ->

                if (isStartCodec) {
                    val width = imageProxy.width
                    val height = imageProxy.height
                    val format = imageProxy.format
                    val planeProxyY = imageProxy.planes[0]
                    val planeProxyU = imageProxy.planes[1]
                    val planeProxyV = imageProxy.planes[2]

                    val pixelStrideY = planeProxyY.pixelStride
                    val rowStrideY = planeProxyY.rowStride
                    val bufferY = planeProxyY.buffer
                    val remainingY = bufferY.remaining()

                    val pixelStrideU = planeProxyU.pixelStride
                    val rowStrideU = planeProxyU.rowStride
                    val bufferU = planeProxyU.buffer
                    val remainingU = bufferU.remaining()

                    val pixelStrideV = planeProxyV.pixelStride
                    val rowStrideV = planeProxyV.rowStride
                    val bufferV = planeProxyV.buffer
                    val remainingV = bufferV.remaining()

                    Log.d(TAG, "imageAnalysis --> width --> $width")
                    Log.d(TAG, "imageAnalysis --> height --> $height")
                    Log.d(TAG, "imageAnalysis --> format --> $format")

                    Log.d(TAG, "imageAnalysis --> pixelStrideY --> $pixelStrideY")
                    Log.d(TAG, "imageAnalysis --> rowStrideY --> $rowStrideY")
                    Log.d(TAG, "imageAnalysis --> remainingY --> $remainingY")

                    Log.d(TAG, "imageAnalysis --> pixelStrideU --> $pixelStrideU")
                    Log.d(TAG, "imageAnalysis --> rowStrideU --> $rowStrideU")
                    Log.d(TAG, "imageAnalysis --> remainingU --> $remainingU")

                    Log.d(TAG, "imageAnalysis --> pixelStrideV --> $pixelStrideV")
                    Log.d(TAG, "imageAnalysis --> rowStrideV --> $rowStrideV")
                    Log.d(TAG, "imageAnalysis --> remainingV --> $remainingV")
                    Log.d(TAG, "imageAnalysis --> remainingV --> ---------------------------")

                    val yuvToNV21 = YUVUtils.yuvToNV21(
                        width,
                        height,
                        bufferY,
                        remainingY,
                        bufferU,
                        remainingU,
                        bufferV,
                        remainingV
                    )

                    mHardEncodeUtils.setData(yuvToNV21)

//                    val nv21ToBitmap = nV21ToBitmap.nv21ToBitmap(yuvToNV21, width, height)
//                    viewBinding.mImageView.setImageBitmap(nv21ToBitmap)

                }
                imageProxy.close()
            })

        return imageAnalysis
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "CameraXHardCodecH264"
        private const val REQUEST_CODE_PERMISSIONS = 100
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }


}