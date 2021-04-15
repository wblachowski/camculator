package com.github.wblachowski.camculator.processing.result

import com.github.wblachowski.camculator.processing.result.equation.EquationProcessingResult
import com.github.wblachowski.camculator.processing.result.image.ImageProcessingResult

data class ProcessingResult(private val imageResult: ImageProcessingResult, private val equationResult: EquationProcessingResult) {
    val boxesImg = imageResult.boxesImg
    val equations = equationResult.equations
    val equationsCorrect = equationResult.correct
    val solutions = equationResult.solutions
}