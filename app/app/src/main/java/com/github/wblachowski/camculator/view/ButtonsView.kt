package com.github.wblachowski.camculator.view

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.github.wblachowski.camculator.R
import kotlinx.android.synthetic.main.buttons_view.view.*

class ButtonsView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val flashModes = listOf(Pair(Camera.Parameters.FLASH_MODE_OFF, R.drawable.ic_flash_off_white_24dp), Pair(Camera.Parameters.FLASH_MODE_AUTO, R.drawable.ic_flash_auto_white_24dp), Pair(Camera.Parameters.FLASH_MODE_ON, R.drawable.ic_flash_on_white_24dp), Pair(Camera.Parameters.FLASH_MODE_TORCH, R.drawable.ic_flash_torch_white_24dp))
    private var currentFlashMode = 0
    private var resultsVisible = true
    var onCameraButtonClicked : ()->Any = {}
    var onFlashButtonClicked: (String)-> Any = {}
    var onPreviewButtonClicked: (Boolean) ->Any = {}


    init {
        addView((context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.buttons_view, null))
        cameraTriggerButton.setOnClickListener { onCameraButtonClicked() }
        flashButton.setOnClickListener {
            currentFlashMode = (currentFlashMode + 1) % flashModes.size
            val newFlashMode = flashModes[currentFlashMode]
            onFlashButtonClicked(newFlashMode.first)
            flashButton.setImageDrawable(context.getDrawable(newFlashMode.second))
        }
        previewButton.setOnClickListener {
            resultsVisible = !resultsVisible
            onPreviewButtonClicked(resultsVisible)
            val newDrawable = if (resultsVisible) R.drawable.ic_checkbox_blank_outline else R.drawable.ic_checkbox_blank_off_outline
            previewButton.setImageDrawable(context.getDrawable(newDrawable))
        }
    }

    fun hide() {
        visibility = View.GONE
    }

    fun show() {
        visibility = View.VISIBLE
    }
}