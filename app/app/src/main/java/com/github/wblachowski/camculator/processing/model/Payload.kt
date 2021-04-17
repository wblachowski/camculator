package com.github.wblachowski.camculator.processing.model

import android.graphics.Bitmap
import android.graphics.Rect

data class Payload(val bitmap: Bitmap, val cropRectangle: Rect)