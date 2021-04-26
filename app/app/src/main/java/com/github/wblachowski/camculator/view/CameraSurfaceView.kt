package com.github.wblachowski.camculator.view

import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.github.wblachowski.camculator.activity.MainActivity
import kotlin.math.abs

class CameraSurfaceView(private val mainActivity: MainActivity, var camera: Camera) : SurfaceView(mainActivity), SurfaceHolder.Callback {

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) = startCameraPreview()

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Empty. Releasing the camera in the main activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        if (holder.surface == null) {
            return
        }
        try {
            camera.stopPreview()
        } catch (e: Exception) {
            // Ignoring, tried to stop a non-existent preview
        }
        startCameraPreview()
    }

    private fun startCameraPreview() {
        camera.apply {
            setDisplayOrientation(90)
            setPreviewCallback(mainActivity::onPreviewFrame)
            setPreviewDisplay(holder)
            startPreview()
        }
    }

    fun getOptimalPreviewSize(w: Int, h: Int, aspectTolerance: Float = 0.1f): Camera.Size? {
        val supportedPreviewSizes = camera.parameters.supportedPreviewSizes
        val targetRatio = h.toDouble() / w

        return supportedPreviewSizes
                .filter { abs(it.width.toDouble() / it.height - targetRatio) <= aspectTolerance }
                .minBy { abs(it.height - h) } ?: supportedPreviewSizes.minBy { abs(it.height - h) }
    }
}