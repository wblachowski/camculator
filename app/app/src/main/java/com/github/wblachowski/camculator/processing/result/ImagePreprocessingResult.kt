package com.github.wblachowski.camculator.processing.result

import android.graphics.Bitmap

import com.github.wblachowski.camculator.processing.Symbol

data class ImagePreprocessingResult(val boxesImg: Bitmap, val symbols: List<Symbol>)
