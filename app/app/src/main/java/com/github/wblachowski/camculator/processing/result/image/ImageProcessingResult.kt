package com.github.wblachowski.camculator.processing.result.image

import android.graphics.Bitmap

import com.github.wblachowski.camculator.processing.Symbol
import org.opencv.core.Size

data class ImageProcessingResult(val symbols: List<Symbol>, val size: Size)
