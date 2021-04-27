package com.github.wblachowski.camculator.processing.model.result.equation

import com.github.wblachowski.camculator.processing.model.Equation
import com.github.wblachowski.camculator.utils.latexifyEquations

data class EquationProcessingResult(val equations: List<Equation>, val correct: Boolean, val solutions: List<Solution>) {
    val latexEquations = latexifyEquations(equations)
}
