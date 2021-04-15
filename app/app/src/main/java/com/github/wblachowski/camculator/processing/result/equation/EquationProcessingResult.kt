package com.github.wblachowski.camculator.processing.result.equation

data class EquationProcessingResult(val equations: List<String>, val correct: Boolean, val solutions: List<Solution>)
