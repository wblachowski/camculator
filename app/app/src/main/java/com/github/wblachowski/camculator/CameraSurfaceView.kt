package com.github.wblachowski.camculator

import android.content.ContentValues.TAG
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

class CameraSurfaceView(private val mainActivity: MainActivity, private var camera: Camera) : SurfaceView(mainActivity), SurfaceHolder.Callback {

    var prSupportedPreviewSizes: List<Camera.Size>? = null

    init {
        holder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        prSupportedPreviewSizes = camera.parameters.supportedPreviewSizes

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startCameraPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        if (holder.surface == null) {
            return
        }
        try {
            camera.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }
        startCameraPreview()
    }

    private fun startCameraPreview() {
        try {
            camera.setDisplayOrientation(90)
            //set camera to continually auto-focus
            val params = camera.parameters
            params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            camera.parameters = params
            camera.setPreviewCallback(mainActivity::onPreviewFrame)
            camera.setPreviewDisplay(holder)
            camera.startPreview()
        } catch (e: Exception) {
            Log.d(TAG, "Error starting camera preview: " + e.message)
        }
    }

    fun setCamera(camera : Camera){
        this.camera=camera
    }

    fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = h.toDouble() / w
        if (sizes == null) return null
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE
        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize
    }
}