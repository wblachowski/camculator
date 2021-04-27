package com.github.wblachowski.camculator.utils

import junit.framework.Assert.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class LatexTest {

    @ParameterizedTest
    @CsvSource(
            "a=b,a=b",
            "x=13*I,x=13 \\cdot i",
            "x^y=324^10^20,x^{y}=324^{10^{20}}",
            "a/b^434=3+17/3,\\frac{a}{b^{434}}=3+\\frac{17}{3}",
            "a^32^w^2*3^4+23^4/43^43,a^{32^{w^{2}}} \\cdot 3^{4}+\\frac{23^{4}}{43^{43}}",
            "1^2^3^4^5,1^{2^{3^{4^{5}}}}",
            "a/b/c/d,\\frac{a}{b}/\\frac{c}{d}"
    )
    fun latexifyTest(input: String, expected: String) {
        assertEquals(expected, latexify(input))
    }
}