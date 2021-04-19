package com.github.wblachowski.camculator.processing.model.result.equation

import com.github.wblachowski.camculator.processing.model.Equation

data class EquationProcessingResult(val equations: List<Equation>, val correct: Boolean, val solutions: List<Solution>) {
    val latexEquations = "\\(\\color{white}{\\begin{cases}" + equations.joinToString("\\\\") { it.latexStringRepresentation } + "\\end{cases}}\\)"
}