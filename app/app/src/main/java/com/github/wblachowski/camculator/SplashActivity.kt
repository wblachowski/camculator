package com.github.wblachowski.camculator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_splash.*
import org.matheclipse.core.expression.F
import org.opencv.android.OpenCVLoader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SplashActivity : AppCompatActivity() {

    private var initialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (!hasFocus || initialized) {
            return
        }
        initialize()
        super.onWindowFocusChanged(hasFocus)
    }

    private fun initialize() {
        initialized = true
        Thread {
            runOnUiThread { statusTextView.text = "Loading math..." }
            try {
                F.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            runOnUiThread { statusTextView.text = "Loading openCV..." }
            OpenCVLoader.initDebug()

            runOnUiThread { statusTextView.text = "Loading model..." }
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
            EquationInterpreter.init(file)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }.start()

    }
}
