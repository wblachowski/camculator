package com.github.wblachowski.camculator.processing.model.result

import android.graphics.Bitmap
import com.github.wblachowski.camculator.processing.model.result.equation.EquationProcessingResult
import com.github.wblachowski.camculator.processing.model.result.image.ImageProcessingResult

data class ProcessingResult(private val imageResult: ImageProcessingResult, val equationResult: EquationProcessingResult, val boxesImg: Bitmap)