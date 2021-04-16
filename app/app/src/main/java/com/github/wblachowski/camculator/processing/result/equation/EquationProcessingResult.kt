package com.github.wblachowski.camculator.processing.result.equation

import com.github.wblachowski.camculator.processing.Equation

data class EquationProcessingResult(val equations: List<Equation>, val correct: Boolean, val solutions: List<Solution>)
