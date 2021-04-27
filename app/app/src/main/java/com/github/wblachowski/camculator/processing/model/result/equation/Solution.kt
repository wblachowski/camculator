package com.github.wblachowski.camculator.processing.model.result.equation

import com.github.wblachowski.camculator.utils.latexifySolutions

data class Solution(val values: List<Pair<String, String>>) {
    val latexStringRepresentation = latexifySolutions(values)
}