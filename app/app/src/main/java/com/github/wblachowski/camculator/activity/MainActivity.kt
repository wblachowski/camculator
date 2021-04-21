package com.github.wblachowski.camculator.activity

import android.graphics.Point
import android.graphics.Rect
import android.hardware.Camera
import android.hardware.Camera.Parameters.*
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import com.github.wblachowski.camculator.R
import com.github.wblachowski.camculator.processing.ImageProcessingTask
import com.github.wblachowski.camculator.processing.model.Payload
import com.github.wblachowski.camculator.processing.model.PicturePayload
import com.github.wblachowski.camculator.processing.model.PreviewPayload
import com.github.wblachowski.camculator.processing.model.result.ProcessingResult
import com.github.wblachowski.camculator.utils.PixelConverter
import com.github.wblachowski.camculator.view.CameraSurfaceView
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    private var camera: Camera? = null
    private lateinit var cameraSurfaceView: CameraSurfaceView
    private lateinit var cameraPreviewDim: Point
    private lateinit var pixelConverter: PixelConverter
    private var draggingViewport = false
    private var previewEnabled = true
    private var processingTask: ImageProcessingTask? = null
    private val flashModes = listOf(Pair(FLASH_MODE_OFF, R.drawable.ic_flash_off_white_24dp), Pair(FLASH_MODE_AUTO, R.drawable.ic_flash_auto_white_24dp), Pair(FLASH_MODE_ON, R.drawable.ic_flash_on_white_24dp), Pair(FLASH_MODE_TORCH, R.drawable.ic_flash_torch_white_24dp))
    private var currentFlashMode = 0
    private var resultsVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        pixelConverter = PixelConverter(resources.displayMetrics)
        camera = getCameraInstance()
        cameraSurfaceView = CameraSurfaceView(this, camera!!)

        cameraPreview.addView(cameraSurfaceView)
        cameraPreviewDim = calcCameraPreviewDimensions()
        cameraPreview.layoutParams = cameraPreview.layoutParams.apply {
            width = cameraPreviewDim.x
            height = cameraPreviewDim.y
        }

        cameraTriggerButton.setOnClickListener { onCameraTriggerClicked() }
        touchPaneView.setOnTouchListener { view, event -> onTouchPaneClicked(view, event) }
        flashButton.setOnClickListener {
            currentFlashMode = (currentFlashMode + 1) % flashModes.size
            val newFlashMode = flashModes[currentFlashMode]
            camera?.parameters = camera?.parameters?.apply {
                flashMode = newFlashMode.first
            }
            flashButton.setImageDrawable(getDrawable(newFlashMode.second))
        }
        previewButton.setOnClickListener {
            resultsVisible = !resultsVisible
            onResultsVisibleChanged(resultsVisible)
            val newDrawable = if (resultsVisible) R.drawable.ic_checkbox_blank_outline else R.drawable.ic_checkbox_blank_off_outline
            previewButton.setImageDrawable(getDrawable(newDrawable))
        }
    }

    private fun onResultsVisibleChanged(previewButtonEnabled: Boolean) {
        if (previewButtonEnabled) {
            showResults()
        } else {
            processingTask?.cancel(true)
            hideResults()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        frameWrapper.layoutParams = (frameWrapper.layoutParams as FrameLayout.LayoutParams).apply {
            val margin = viewport.rectangle.left.toInt()
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

    override fun onBackPressed() =
            if (previewEnabled) {
                super.onBackPressed()
            } else {
                camera?.startPreview()
                previewEnabled = true
                cameraTriggerButtonWrapper.visibility = View.VISIBLE
                onResultsVisibleChanged(resultsVisible)
            }

    fun onPreviewFrame(data: ByteArray, camera: Camera) {
        if (resultsVisible && (processingTask == null || processingTask?.status == AsyncTask.Status.FINISHED)) {
            val payload = PreviewPayload(data, camera, viewport.rectangle, getDataRectangle(camera.parameters.previewSize))
            executeProcessingTask(payload)
        }
    }

    private fun onTouchPaneClicked(view: View, event: MotionEvent): Boolean {
        val action = event.action
        val y = event.y
        val draggingMargin = pixelConverter.fromDp(24)
        if (!draggingViewport && y >= viewport.rectangle.bottom + draggingMargin) {
            return false
        }
        if (previewEnabled) {
            if (action == ACTION_DOWN) {
                draggingViewport = abs(y - viewport.rectangle.bottom) < draggingMargin
            } else if (action == ACTION_MOVE && draggingViewport) {
                processingTask?.cancel(true)
                viewport.repaint(min(getDisplayWH().y - pixelConverter.fromDp(140), max(2 * viewport.rectangle.left, y)))
                hideResults()
            } else if (action == ACTION_UP) {
                draggingViewport = false
            }
        }
        return true
    }

    private fun onCameraTriggerClicked() {
        processingTask?.cancel(true)
        hideResults()
        val onCapture = Camera.PictureCallback { data, camera -> onCameraCapture(data, camera) }
        val onShutter = Camera.ShutterCallback { onCameraShutter() }
        camera?.takePicture(onShutter, null, onCapture)
        cameraTriggerButtonWrapper.visibility = View.GONE
        previewEnabled = false
    }

    private fun onCameraShutter() {
        shutterEffectView.visibility = View.VISIBLE
        Handler().postDelayed({ shutterEffectView.visibility = View.INVISIBLE }, 100)
    }

    private fun onCameraCapture(data: ByteArray, camera: Camera) {
        processingTask?.cancel(true)
        this.camera?.stopPreview()
        val payload = PicturePayload(data, camera, viewport.rectangle, getDataRectangle(camera.parameters.pictureSize))
        executeProcessingTask(payload)
    }

    private fun executeProcessingTask(payload: Payload) {
        val onPostProcessing = { result: ProcessingResult ->
            framePreview.setImageBitmap(result.boxesImg)
            equationsView.updateEquations(result.equationResult)
            solutionsView.updateSolutions(result.equationResult)
            showResults()
        }
        processingTask = ImageProcessingTask(onPostProcessing).apply { execute(payload) }
    }

    private fun hideResults() {
        resultsView.visibility = View.INVISIBLE
        framePreview.visibility = View.INVISIBLE
    }

    private fun showResults() {
        resultsView.visibility = View.VISIBLE
        framePreview.visibility = View.VISIBLE
    }

    private fun getCameraInstance(flashMode: String = FLASH_MODE_OFF, focusMode: String = FOCUS_MODE_CONTINUOUS_PICTURE) =
            camera ?: Camera.open().apply {
                enableShutterSound(true)
                parameters = parameters.apply {
                    setPictureSize(previewSize.width, previewSize.height)
                    this.flashMode = flashMode
                    this.focusMode = focusMode
                }
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

    private fun getDataRectangle(size: Camera.Size): Rect {
        val factor = cameraPreviewDim.y.toFloat() / size.width
        val r = viewport.rectangle
        return Rect((r.left / factor).toInt(), (r.top / factor).toInt(), (r.right / factor).toInt(), (r.bottom / factor).toInt())
    }
}
