package com.github.wblachowski.camculator.processing.result

import android.graphics.Bitmap
import com.github.wblachowski.camculator.processing.result.equation.EquationProcessingResult
import com.github.wblachowski.camculator.processing.result.image.ImageProcessingResult

data class ProcessingResult(private val imageResult: ImageProcessingResult, private val equationResult: EquationProcessingResult, val boxesImg: Bitmap) {
    val equations = equationResult.equations.map { it.stringRepresentation }.toList()
    val equationsCorrect = equationResult.correct
    val solutions = equationResult.solutions
}