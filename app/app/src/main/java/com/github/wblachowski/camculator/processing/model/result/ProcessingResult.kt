package com.github.wblachowski.camculator.processing.model.result

import android.graphics.Bitmap
import com.github.wblachowski.camculator.processing.model.result.equation.EquationProcessingResult
import com.github.wblachowski.camculator.processing.model.result.image.ImageProcessingResult

data class ProcessingResult(private val imageResult: ImageProcessingResult, private val equationResult: EquationProcessingResult, val boxesImg: Bitmap) {
    val equations = equationResult.latexEquations
    val equationsCorrect = equationResult.correct
    val solutions = equationResult.solutions
}