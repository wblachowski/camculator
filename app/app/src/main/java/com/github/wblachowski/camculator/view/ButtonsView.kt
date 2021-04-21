package com.github.wblachowski.camculator.view

import android.content.Context
import android.hardware.Camera.Parameters.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.github.wblachowski.camculator.R
import kotlinx.android.synthetic.main.buttons_view.view.*

class ButtonsView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val flashModes = listOf(FlashMode(FLASH_MODE_OFF, R.drawable.ic_flash_off_white_24dp, "Flash off"), FlashMode(FLASH_MODE_AUTO, R.drawable.ic_flash_auto_white_24dp, "Flash in auto mode"), FlashMode(FLASH_MODE_ON, R.drawable.ic_flash_on_white_24dp, "Flash on"), FlashMode(FLASH_MODE_TORCH, R.drawable.ic_flash_torch_white_24dp, "Flash in torch mode"))
    private var resultsModes = listOf(ResultsMode(true, R.drawable.ic_checkbox_blank_outline, "Preview enabled"), ResultsMode(false, R.drawable.ic_checkbox_blank_off_outline, "Preview disabled"))
    private var currentFlashMode = 0
    private var currentResultsMode = 0
    private var toast: Toast? = null
    var onCameraButtonClicked: () -> Any = {}
    var onFlashButtonClicked: (String) -> Any = {}
    var onPreviewButtonClicked: (Boolean) -> Any = {}

    init {
        addView((context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.buttons_view, null))
        cameraTriggerButton.setOnClickListener { onCameraButtonClicked() }
        flashButton.setOnClickListener {
            currentFlashMode = (currentFlashMode + 1) % flashModes.size
            val newFlashMode = flashModes[currentFlashMode]
            onFlashButtonClicked(newFlashMode.mode)
            flashButton.setImageDrawable(context.getDrawable(newFlashMode.drawable))
            updateToast(newFlashMode.text)
        }
        previewButton.setOnClickListener {
            currentResultsMode = (currentResultsMode + 1) % resultsModes.size
            val newResultsMode = resultsModes[currentResultsMode]
            onPreviewButtonClicked(newResultsMode.visible)
            previewButton.setImageDrawable(context.getDrawable(newResultsMode.drawable))
            updateToast(newResultsMode.text)
        }
    }

    fun hide() {
        visibility = View.GONE
    }

    fun show() {
        visibility = View.VISIBLE
    }

    private fun updateToast(text: String) {
        toast?.cancel()
        toast = Toast.makeText(context, text, Toast.LENGTH_SHORT).apply { show() }
    }

    class FlashMode(val mode: String, val drawable: Int, val text: String)

    class ResultsMode(val visible: Boolean, val drawable: Int, val text: String)
}