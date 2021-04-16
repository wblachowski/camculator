package com.github.wblachowski.camculator.processing

data class InterpretedSymbol(private val symbol: Symbol, val value: String, val probability: Double) : Symbol(symbol.box, symbol.image)