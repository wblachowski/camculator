package com.github.wblachowski.camculator.activity

import android.graphics.*
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import com.github.wblachowski.camculator.R
import com.github.wblachowski.camculator.processing.EquationInterpreter
import com.github.wblachowski.camculator.processing.ImageProcessingTask
import com.github.wblachowski.camculator.processing.ImageProcessor
import com.github.wblachowski.camculator.processing.Payload
import com.github.wblachowski.camculator.processing.result.ProcessingResult
import com.github.wblachowski.camculator.view.CameraSurfaceView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    private var camera: Camera? = null
    private lateinit var cameraSurfaceView: CameraSurfaceView
    private lateinit var cameraPreviewDim: Point
    private var cropRectangle = Rect()
    private var equationInterpreter = EquationInterpreter.getInstance()
    private val imageProcessor = ImageProcessor.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        camera = getCameraInstance()
        cameraSurfaceView = CameraSurfaceView(this, camera!!)
        cameraPreview.addView(cameraSurfaceView)

        cameraPreviewDim = calcCameraPreviewDimensions()
        cameraPreview.layoutParams = cameraPreview.layoutParams.apply {
            width = cameraPreviewDim.x
            height = cameraPreviewDim.y
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val r = viewport.rectangle
        cropRectangle = Rect(r.left.toInt(), r.top.toInt(), r.bottom.toInt(), r.right.toInt())
        frameWrapper.layoutParams = (frameWrapper.layoutParams as FrameLayout.LayoutParams).apply {
            val margin = r.left.toInt()
            setMargins(margin, margin, margin, margin)
        }
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
            cameraSurfaceView.camera = camera!!
        }
    }

    fun onPreviewFrame(data: ByteArray, camera: Camera) {
        if (imageProcessor.isProcessing || equationInterpreter.isProcessing) {
            return
        }
        val bitmap = getDataBitmap(data, camera)
        val payload = Payload(bitmap, cropRectangle)
        val onPostProcessing = { result: ProcessingResult ->
            framePreview.setImageBitmap(result.boxesImg)
            equationsView.text = result.equations.stream().map { s -> s + '\n' }.reduce { obj, str -> obj + str }.orElse("")
            equationsTitle.text = if (result.equationsCorrect) "Equations" else "Equations (incorrect)"
            equationsTitle.setTextColor(if (result.equationsCorrect) resources.getColor(R.color.white) else resources.getColor(R.color.red))
            solutionsView.visibility = if (result.equationsCorrect) View.VISIBLE else View.GONE
            solutionsTextView.text = result.solutions.stream().map { "->" + it.values.map { it.first + "=" + it.second + '\n' }.reduce { obj, str -> obj + str }}.reduce { obj, str -> obj + str }.orElse("")
        }
        ImageProcessingTask(onPostProcessing).execute(payload)
    }

    private fun getCameraInstance() = camera ?: Camera.open()

    private fun getDisplayWH() = Point().apply(windowManager.defaultDisplay::getSize)

    private fun calcCameraPreviewDimensions(): Point {
        val displayDim = getDisplayWH()
        val cameraDim = cameraSurfaceView.getOptimalPreviewSize(displayDim.y, displayDim.x)!!
        val widthRatio = displayDim.x.toDouble() / cameraDim.height
        val heightRatio = displayDim.y.toDouble() / cameraDim.width
        return if (widthRatio >= heightRatio) {
            Point(displayDim.x, displayDim.x * cameraDim.width / cameraDim.height)
        } else {
            Point(displayDim.y * cameraDim.height / cameraDim.width, displayDim.y)
        }
    }

    private fun getDataRectangle(camera: Camera): Rect {
        val parameters = camera.parameters
        val factor = cameraPreviewDim.y.toFloat() / parameters.previewSize.width
        val offset = cameraPreviewDim.x - parameters.previewSize.height - viewport.cornerRadius.toInt()
        return Rect((cropRectangle.left / factor).toInt(), offset + (cropRectangle.top / factor).toInt(), (cropRectangle.right / factor).toInt(), offset + (cropRectangle.bottom / factor).toInt())
    }

    private fun getDataBitmap(data: ByteArray, camera: Camera): Bitmap {
        val parameters = camera.parameters
        val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(data, parameters.previewFormat, parameters.previewSize.width, parameters.previewSize.height, null)
        val rec = getDataRectangle(camera)
        yuvImage.compressToJpeg(rec, 90, out)
        val imageBytes = out.toByteArray()
        out.flush()
        out.close()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        matrix.postRotate(90f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
