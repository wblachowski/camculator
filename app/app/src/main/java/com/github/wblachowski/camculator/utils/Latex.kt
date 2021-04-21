package com.github.wblachowski.camculator.utils

fun latexify(mathText: String) =
        mathText.replace("I", "i")
                .replace("""([^+\-=]+)/([^+\-=*]+)""".toRegex(), "\\\\frac{$1}{$2}")
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
            newText += "$char}"
            opened--
        } else {
            newText += char
        }
    }
    for (i in 0 until opened) {
        newText += "}"
    }
    return newText
}