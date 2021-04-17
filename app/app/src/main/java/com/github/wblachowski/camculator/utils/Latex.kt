package com.github.wblachowski.camculator.utils

fun latexify(mathText: String) =
        mathText.replace("I", "i")
                .replace("""\^(\d+)""".toRegex(), "^{$1}")
                .replace("""([^+\-=]+)/([^+\-=*]+)""".toRegex(), "\\\\frac{$1}{$2}")
                .replace("*", " \\cdot ")