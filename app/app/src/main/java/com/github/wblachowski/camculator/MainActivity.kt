package com.github.wblachowski.camculator

import android.annotation.SuppressLint
import android.graphics.*
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.math.roundToInt


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
        val optimalCameraPreviewDim = cameraSurfaceView!!.getOptimalPreviewSize(cameraSurfaceView!!.prSupportedPreviewSizes,
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
        cropRectangle = Rect(r.left.roundToInt(), r.top.roundToInt(), r.bottom.roundToInt(), r.right.roundToInt())
    }

    fun onPreviewFrame(data: ByteArray, camera: Camera) {
        if (imageProcessor.isProcessing || equationInterpreter.isProcessing) {
            return
        }
        val parameters = camera.parameters
        val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(data, parameters.previewFormat, parameters.previewSize.width, parameters.previewSize.height, null)

        val factor = layoutPreviewDim!!.y.toFloat()/parameters.previewSize.width
        val rec =  Rect( (cropRectangle.left/factor).roundToInt(),120+(cropRectangle.top/factor).roundToInt(), (cropRectangle.right/factor).roundToInt(),120+(cropRectangle.bottom/factor).roundToInt() )
        yuvImage.compressToJpeg(rec, 90, out)
        val imageBytes = out.toByteArray()
        var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        matrix.postRotate(90f)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        ImageProcessingTask().execute(imageProcessor, equationInterpreter, bitmap, framePreview, equationsView)
        try {
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onPause() {
        super.onPause()
        camera?.stopPreview()
        camera?.setPreviewCallback(null)
        camera?.release()
        camera = null
    }

    override fun onResume() {
        super.onResume()
        if (camera == null) {
            camera = getCameraInstance()
            cameraSurfaceView?.setCamera(camera!!)
        }
    }

    private fun getCameraInstance(): Camera? {
        return if (camera != null) camera else Camera.open()
    }


    @SuppressLint("NewApi")
    private fun getDisplayWH(): Point? {
        val display = this.windowManager.defaultDisplay
        val displayWH = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(displayWH)
            return displayWH
        }
        displayWH[display.width] = display.height
        return displayWH
    }

    private fun calcCamPrevDimensions(disDim: Point, camDim: Camera.Size): Point? {
        val widthRatio = disDim.x.toDouble() / camDim.height
        val heightRatio = disDim.y.toDouble() / camDim.width
        // use ">" to zoom preview full screen
        if (widthRatio > heightRatio) {
            val calcDimensions = Point()
            calcDimensions.x = disDim.x
            calcDimensions.y = disDim.x * camDim.width / camDim.height
            return calcDimensions
        }
        // use "<" to zoom preview full screen
        if (widthRatio < heightRatio) {
            val calcDimensions = Point()
            calcDimensions.x = disDim.y * camDim.height / camDim.width
            calcDimensions.y = disDim.y
            return calcDimensions
        }
        return null
    }

}
