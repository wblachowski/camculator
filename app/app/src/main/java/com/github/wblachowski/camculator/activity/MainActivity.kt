package com.github.wblachowski.camculator.activity

import android.graphics.*
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import com.github.wblachowski.camculator.R
import com.github.wblachowski.camculator.processing.EquationInterpreter
import com.github.wblachowski.camculator.processing.ImageProcessingTask
import com.github.wblachowski.camculator.processing.ImageProcessor
import com.github.wblachowski.camculator.view.CameraSurfaceView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private var camera: Camera? = null
    private var cameraSurfaceView: CameraSurfaceView? = null
    private var equationInterpreter = EquationInterpreter.getInstance()
    private var cropRectangle = Rect()
    private val imageProcessor = ImageProcessor()
    private var layoutPreviewDim: Point? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        camera = getCameraInstance()
        cameraSurfaceView = CameraSurfaceView(this, camera!!)

        val displayDim = getDisplayWH()
        val optimalCameraPreviewDim = cameraSurfaceView!!.getOptimalPreviewSize(
                displayDim!!.y, displayDim.x)
        layoutPreviewDim = calcCamPrevDimensions(displayDim, optimalCameraPreviewDim!!)
        if (layoutPreviewDim != null) {
            val layoutPreviewParams = cameraPreview.layoutParams
            layoutPreviewParams.width = layoutPreviewDim!!.x
            layoutPreviewParams.height = layoutPreviewDim!!.y
            cameraPreview.layoutParams = layoutPreviewParams
        }
        cameraPreview.addView(cameraSurfaceView)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val r = viewport.rectangle
        cropRectangle = Rect(r.left.toInt(), r.top.toInt(), r.bottom.toInt(), r.right.toInt())
        val frameWrapperParams = frameWrapper.layoutParams as FrameLayout.LayoutParams
        frameWrapperParams.setMargins(r.left.toInt(), r.left.toInt(), r.left.toInt(), r.left.toInt())
        frameWrapper.layoutParams = frameWrapperParams
    }

    override fun onPause() {
        super.onPause()
        camera?.apply {
            stopPreview()
            setPreviewCallback(null)
            release()
        }
        camera = null
    }

    override fun onResume() {
        super.onResume()
        if (camera == null) {
            camera = getCameraInstance()
            cameraSurfaceView?.camera = camera!!
        }
    }

    fun onPreviewFrame(data: ByteArray, camera: Camera) {
        if (imageProcessor.isProcessing || equationInterpreter.isProcessing) {
            return
        }
        val parameters = camera.parameters
        val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(data, parameters.previewFormat, parameters.previewSize.width, parameters.previewSize.height, null)

        val factor = layoutPreviewDim!!.y.toFloat() / parameters.previewSize.width
        val offset = layoutPreviewDim!!.x - parameters.previewSize.height - viewport.cornerRadius.toInt()
        val rec = Rect((cropRectangle.left / factor).toInt(), offset + (cropRectangle.top / factor).toInt(), (cropRectangle.right / factor).toInt(), offset + (cropRectangle.bottom / factor).toInt())
        yuvImage.compressToJpeg(rec, 90, out)
        val imageBytes = out.toByteArray()
        var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        matrix.postRotate(90f)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        ImageProcessingTask().execute(imageProcessor, equationInterpreter, bitmap, framePreview, equationsView, cropRectangle)
        try {
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getCameraInstance() = camera ?: Camera.open()

    private fun getDisplayWH() = Point().apply(windowManager.defaultDisplay::getSize)

    private fun calcCamPrevDimensions(disDim: Point, camDim: Camera.Size): Point? {
        val widthRatio = disDim.x.toDouble() / camDim.height
        val heightRatio = disDim.y.toDouble() / camDim.width
        return if (widthRatio >= heightRatio) {
            Point(disDim.x, disDim.x * camDim.width / camDim.height)
        } else {
            Point(disDim.y * camDim.height / camDim.width, disDim.y)
        }
    }

}
