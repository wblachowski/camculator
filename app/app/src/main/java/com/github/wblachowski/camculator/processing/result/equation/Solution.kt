package com.github.wblachowski.camculator.processing.result.equation

import com.github.wblachowski.camculator.utils.latexify

data class Solution(val values: List<Pair<String, String>>) {

    val latexValues = values.map { Pair(it.first, latexify(it.second)) }.toList()
}