package com.github.wblachowski.camculator.utils

import com.github.wblachowski.camculator.processing.model.Equation
import org.apache.commons.lang3.StringUtils

private const val casesBegin = "\\(\\color{white}{\\begin{cases}"
private const val casesEnd = "\\end{cases}}\\)"
private const val casesSeparator = "\\\\"


fun latexifyEquations(equations: List<Equation>): String {
    val nonEmptyEquations = equations.filter { !it.stringRepresentation.isNullOrBlank() }.toList()
    if (nonEmptyEquations.isEmpty()) return StringUtils.EMPTY
    return casesBegin + nonEmptyEquations.joinToString(casesSeparator) { latexify(it.stringRepresentation) } + casesEnd
}

fun latexifySolutions(values: List<Pair<String, String>>) = casesBegin + values.joinToString(casesSeparator) { it.first + "=" + latexify(it.second) } + casesEnd

fun latexify(mathText: String) =
        mathText.replace("I", "i")
                .replace("""([^+\-=/]+)/([^+\-=*/]+)""".toRegex(), "\\\\frac{$1}{$2}")
                .let { addExpBrackets(it) }
                .replace("*", " \\cdot ")

private fun addExpBrackets(text: String): String {
    var opened = 0
    var newText = ""
    val terminating = listOf('+', '-', '=', '/', '*', '\\', ']', '{', '}')
    for (char in text) {
        if (char == '^') {
            newText += "^{"
            opened++
        } else if (opened > 0 && char in terminating) {
            while (opened > 0) {
                newText += "}"
                opened--
            }
            newText += char
        } else {
            newText += char
        }
    }
    while (opened > 0) {
        newText += "}"
        opened--
    }
    return newText
}