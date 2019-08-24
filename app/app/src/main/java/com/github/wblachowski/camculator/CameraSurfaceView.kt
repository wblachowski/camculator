package com.github.wblachowski.camculator

import android.content.ContentValues.TAG
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

class CameraSurfaceView(private val mainActivity: MainActivity, private val camera: Camera) : SurfaceView(mainActivity), SurfaceHolder.Callback {

    init {
        holder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        camera.setDisplayOrientation(90)
        //set camera to continually auto-focus
        val params = camera.parameters
        params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        camera.parameters = params
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
            camera.setPreviewCallback(mainActivity::onPreviewFrame)
            camera.setPreviewDisplay(holder)
            camera.startPreview()
        } catch (e: Exception) {
            Log.d(TAG, "Error starting camera preview: " + e.message)
        }
    }
}