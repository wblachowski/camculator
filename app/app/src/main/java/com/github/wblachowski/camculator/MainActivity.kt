package com.github.wblachowski.camculator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import com.tbruyelle.rxpermissions2.RxPermissions

import org.matheclipse.core.expression.F
import org.opencv.android.OpenCVLoader

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private var camera: Camera? = null
    private var cameraPreview: CameraPreview? = null
    private var framePreview: ImageView? = null
    private var cropPreview: FrameLayout? = null
    private var equationsTextView: TextView? = null
    private var cropRectangle = Rect()
    private val imageProcessor = ImageProcessor()
    private var equationInterpreter: EquationInterpreter? = null

    /**
     * A safe way to get an instance of the Camera object.
     */
    private// attempt to get a Camera instance
    // Camera is not available (in use or does not exist)
    // returns null if camera is unavailable
    val cameraInstance: Camera?
        get() {
            var c: Camera? = null
            try {
                c = Camera.open()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return c
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            F.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        OpenCVLoader.initDebug()
        askForCameraPermission()
        val file = File(this.filesDir.toString() + File.separator + "model.tflite")
        try {
            val inputStream = resources.openRawResource(R.raw.model)
            val fileOutputStream = FileOutputStream(file)

            val buf = ByteArray(1024)
            var len = inputStream.read(buf)
            while (len > 0) {
                fileOutputStream.write(buf, 0, len)
                len = inputStream.read(buf)
            }


            fileOutputStream.close()
            inputStream.close()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        equationInterpreter = EquationInterpreter(file)

        //TODO handle unavailable camera
        val hasCameraAccess = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)

        camera = cameraInstance
        cameraPreview = CameraPreview(this, camera!!)
        val preview = findViewById<FrameLayout>(R.id.camera_preview)
        preview.addView(cameraPreview)
        framePreview = findViewById(R.id.frame_preview)
        cropPreview = findViewById(R.id.crop_preview)
        equationsTextView = findViewById(R.id.equations_view)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val pixelConverter = PixelConverter(resources.displayMetrics)
        val top = (resources.displayMetrics.widthPixels - cropPreview!!.width) / 2
        val left = pixelConverter.fromDp(50f).toInt()
        cropRectangle = Rect(left, top, cropPreview!!.height + left, cropPreview!!.width + top)
    }

    fun onPreviewFrame(data: ByteArray, camera: Camera) {
        if (imageProcessor.isProcessing || equationInterpreter!!.isProcessing) {
            return
        }
        val parameters = camera.parameters
        val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(data, parameters.previewFormat, parameters.previewSize.width, parameters.previewSize.height, null)

        yuvImage.compressToJpeg(cropRectangle, 90, out)
        val imageBytes = out.toByteArray()
        var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix()
        matrix.postRotate(90f)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        ImageProcessingTask().execute(imageProcessor, equationInterpreter, bitmap, framePreview, equationsTextView)
        try {
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun askForCameraPermission() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
                .request(Manifest.permission.CAMERA)
                .subscribe { granted ->
                    if (!granted) {
                        //TODO handle camera access denial
                    }
                }
    }

    override fun onPause() {
        super.onPause()
        if (camera != null) {
            camera!!.release()
            camera = null
        }
    }

    override fun onResume() {
        super.onResume()
        camera = cameraInstance
        cameraPreview = CameraPreview(this, camera!!)
        val preview = findViewById<FrameLayout>(R.id.camera_preview)
        preview.addView(cameraPreview)
    }
}
