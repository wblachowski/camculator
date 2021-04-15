package com.github.wblachowski.camculator.processing.result

data class ProcessingResult(private val imageResult: ImageProcessingResult, private val equationResult: EquationProcessingResult) {
    val boxesImg = imageResult.boxesImg
    val equations = equationResult.equations
}