package com.github.wblachowski.camculator.processing

import android.graphics.Bitmap
import android.graphics.Rect
import android.widget.ImageView
import android.widget.TextView

data class Payload(val bitmap: Bitmap, val framePreview: ImageView, val equationsView: TextView, val cropRectangle: Rect)