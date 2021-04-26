package com.github.wblachowski.camculator.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.github.wblachowski.camculator.R
import com.github.wblachowski.camculator.processing.processor.EquationInterpreter
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_splash.*
import org.matheclipse.core.expression.F
import org.opencv.android.OpenCVLoader
import java.io.File
import java.io.FileOutputStream

class SplashActivity : AppCompatActivity() {

    private val permissions = RxPermissions(this)
    private var initialized = false
    private var hasPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            askForCameraPermission()
        } else {
            setStatus(R.string.loading_failure)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (!hasFocus || initialized || !hasPermission) {
            return
        }
        initialize()
        super.onWindowFocusChanged(hasFocus)
    }

    private fun initialize() {
        initialized = true
        Thread {
            try {
                loadMath()
                loadOpenCv()
                loadModel()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } catch (ex: Exception) {
                ex.printStackTrace()
                setStatus(R.string.loading_failure)
            }
        }.start()
    }

    private fun loadMath() {
        setStatus(R.string.loading_math)
        F.await()
    }

    private fun loadOpenCv() {
        setStatus(R.string.loading_open_cv)
        OpenCVLoader.initDebug()
    }

    private fun loadModel() {
        setStatus(R.string.loading_model)
        val file = File(this.filesDir.toString() + File.separator + "model.tflite")
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
        EquationInterpreter.init(file)
    }

    private fun askForCameraPermission() =
            permissions.request(Manifest.permission.CAMERA)
                    .subscribe { granted ->
                        hasPermission = granted
                        if (!granted) {
                            setStatus(R.string.loading_failure)
                        }
                    }

    private fun setStatus(messageReference: Int) = runOnUiThread { statusTextView.text = getString(messageReference) }
}
