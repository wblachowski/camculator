package com.github.wblachowski.camculator

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import org.matheclipse.core.expression.F
import org.opencv.android.OpenCVLoader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var camera: Camera? = null
    private var cameraSurfacePreview: CameraPreview? = null
    private var equationInterpreter: EquationInterpreter? = null
    private var cropRectangle = Rect()
    private val imageProcessor = ImageProcessor()


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

        camera = getCameraInstance()
        cameraSurfacePreview = CameraPreview(this, camera!!)
        cameraPreview.addView(cameraSurfacePreview)
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
        camera?.release()
    }

    override fun onResume() {
        super.onResume()
        camera = getCameraInstance()
        cameraSurfacePreview = CameraPreview(this, camera!!)
        cameraPreview.addView(cameraSurfacePreview)
    }

    private fun getCameraInstance(): Camera? {
        return Camera.open()
    }

    private fun askForCameraPermission() {
        RxPermissions(this)
                .request(Manifest.permission.CAMERA)
                .subscribe { granted ->
                    if (!granted) {
                        //TODO handle camera access denial
                    }
                }
    }
}
