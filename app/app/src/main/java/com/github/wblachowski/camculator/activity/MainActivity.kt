package com.github.wblachowski.camculator.activity

import android.graphics.*
import android.hardware.Camera
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import com.github.wblachowski.camculator.R
import com.github.wblachowski.camculator.processing.ImageProcessingTask
import com.github.wblachowski.camculator.processing.Payload
import com.github.wblachowski.camculator.processing.result.ProcessingResult
import com.github.wblachowski.camculator.view.CameraSurfaceView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    private var camera: Camera? = null
    private lateinit var cameraSurfaceView: CameraSurfaceView
    private lateinit var cameraPreviewDim: Point
    private var cropRectangle = Rect()
    private var previewEnabled = true
    private var processingTask: ImageProcessingTask? = null

    private var dragging = false

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

        cameraTriggerButton.setOnClickListener { onCameraTriggerClicked() }

        val onTouchListener = View.OnTouchListener { view, event ->
            if (previewEnabled) {
                val action = event.action
                val y = event.y
                Log.d("", action.toString())
                if (action == ACTION_DOWN) {
                    dragging = abs(y - viewport.rectangle.bottom) < 60
                }
                if (action == ACTION_MOVE && dragging) {
                    processingTask?.cancel(true)
                    viewport.repaint(min(getDisplayWH().y - viewport.rectangle.left, max(2 * viewport.rectangle.left, y)))
                    val r = viewport.rectangle
                    cropRectangle = Rect(r.left.toInt(), r.top.toInt(), r.bottom.toInt(), r.right.toInt())
                    resultsView.visibility = View.INVISIBLE
                    framePreview.visibility = View.INVISIBLE
                }
            }
            true
        }
        touchPaneView.setOnTouchListener(onTouchListener)
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
        if (processingTask == null || processingTask?.status == AsyncTask.Status.FINISHED) {
            val payload = Payload(getDataBitmapFromPreview(data, camera), cropRectangle)
            executeProcessingTask(payload)
        }
    }

    private fun onCameraTriggerClicked() {
        if (previewEnabled) {
            processingTask?.cancel(true)
            resultsView.visibility = View.INVISIBLE
            framePreview.visibility = View.INVISIBLE
            val onCapture = Camera.PictureCallback { data, camera -> onCameraCapture(data, camera) }
            val onShutter = Camera.ShutterCallback { onCameraShutter() }
            camera?.enableShutterSound(true)
            camera?.parameters?.setPictureSize(camera?.parameters?.previewSize?.width
                    ?: 0, camera?.parameters?.previewSize?.height ?: 0)
            camera?.takePicture(onShutter, null, onCapture)
        } else {
            camera?.startPreview()
        }
        previewEnabled = !previewEnabled
    }

    private fun onCameraShutter() {
        shutterEffectView.visibility = View.VISIBLE
        Handler().postDelayed({ shutterEffectView.visibility = View.INVISIBLE }, 100)
    }

    private fun onCameraCapture(data: ByteArray, camera: Camera) {
        processingTask?.cancel(true)
        this.camera?.stopPreview()
        val payload = Payload(getDataBitmapFromPicture(data, camera), cropRectangle)
        executeProcessingTask(payload)
    }

    private fun executeProcessingTask(payload: Payload) {
        val onPostProcessing = { result: ProcessingResult ->
            framePreview.setImageBitmap(result.boxesImg)
            equationsView.text = result.equations.stream().map { s -> s + '\n' }.reduce { obj, str -> obj + str }.orElse("")
            equationsTitle.text = if (result.equationsCorrect) "Equations" else "Equations (incorrect)"
            equationsTitle.setTextColor(if (result.equationsCorrect) resources.getColor(R.color.white) else resources.getColor(R.color.red))
            solutionsView.visibility = if (result.equationsCorrect) View.VISIBLE else View.GONE
            solutionsTextView.text = result.solutions.stream().map { "->" + it.values.map { it.first + "=" + it.second + '\n' }.reduce { obj, str -> obj + str } }.reduce { obj, str -> obj + str }.orElse("")
            resultsView.visibility = View.VISIBLE
            framePreview.visibility = View.VISIBLE
        }
        processingTask = ImageProcessingTask(onPostProcessing).apply { execute(payload) }
    }

    private fun getCameraInstance(): Camera? {
        if (camera == null) {
            val camera = Camera.open()
            camera.parameters = camera.parameters.apply {
                setPictureSize(previewSize.width, previewSize.height)
            }
            camera.enableShutterSound(true)
            return camera
        }
        return camera
    }

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

    private fun getDataBitmapFromPreview(data: ByteArray, camera: Camera): Bitmap {
        val parameters = camera.parameters
        val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(data, parameters.previewFormat, parameters.previewSize.width, parameters.previewSize.height, null)
        val rec = getDataRectangle(parameters.previewSize)
        yuvImage.compressToJpeg(rec, 90, out)
        val imageBytes = out.toByteArray()
        out.flush()
        out.close()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix().apply { postRotate(90f) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getDataBitmapFromPicture(data: ByteArray, camera: Camera): Bitmap {
        val rec = getDataRectangle(camera.parameters.pictureSize)
        var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        bitmap = Bitmap.createBitmap(bitmap, rec.left, rec.top, rec.width(), rec.height())
        val matrix = Matrix().apply { postRotate(90f) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getDataRectangle(size: Camera.Size): Rect {
        val factor = cameraPreviewDim.y.toFloat() / size.width
        val offset = cameraPreviewDim.x - size.height - viewport.cornerRadius.toInt()
        return Rect((cropRectangle.left / factor).toInt(), offset + (cropRectangle.top / factor).toInt(), (cropRectangle.right / factor).toInt(), offset + (cropRectangle.bottom / factor).toInt())
    }
}
