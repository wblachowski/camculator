package com.github.wblachowski.camculator.processing

import com.github.wblachowski.camculator.utils.latexify

data class Equation(val symbols: List<InterpretedSymbol>, val stringRepresentation: String) {

    val latexStringRepresentation = latexify(stringRepresentation)
}