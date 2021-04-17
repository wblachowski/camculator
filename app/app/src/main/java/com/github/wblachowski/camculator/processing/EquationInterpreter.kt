package com.github.wblachowski.camculator.processing

import com.github.wblachowski.camculator.processing.result.equation.EquationProcessingResult
import com.github.wblachowski.camculator.processing.result.equation.Solution
import com.github.wblachowski.camculator.utils.SingletonHolder
import org.matheclipse.core.eval.ExprEvaluator
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc.THRESH_BINARY
import org.opencv.imgproc.Imgproc.threshold
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.stream.Collectors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class EquationInterpreter(model: File) {

    private val interpreter: Interpreter = Interpreter(model)

    fun findEquations(symbols: List<Symbol>): EquationProcessingResult {
        val groupedSymbols = groupSymbols(symbols)
        val probs = mutableListOf<Pair<String, Int>>()
        val equations = groupedSymbols.map { equation ->
            val interpretedSymbols = mutableListOf<InterpretedSymbol>()
            val labels = equation.mapNotNull { symbol ->
                val imgData = convertMatToTfLiteInput(symbol.image)
                val probArray = Array(1) { FloatArray(LABELS.size) }
                interpreter.run(imgData, probArray)
                val prob = probArray.first().max()?.toDouble() ?: .0
                val label = findMaxProbSymbol(probArray.first())
                if (prob >= .75) {
                    interpretedSymbols.add(InterpretedSymbol(symbol, label, prob))
                    label
                } else null
            }.toList()
            Equation(interpretedSymbols, getStringRepresentation(labels, equation))
        }.toMutableList()

        val expression = convertEquationsToExpression(equations.map { it.stringRepresentation }.toList())
        var correctEquations = true
        var solutions = listOf<Solution>()
        try {
            val solutionText = ExprEvaluator().eval(expression).toString().replace("Solve(", "").replace(",{x,y,w,z})", "")
            solutions = solutionText.substring(2, solutionText.length - 2)
                    .replace(",{", "")
                    .replace("I", "i")
                    .split("}")
                    .map { it.split(",") }
                    .map { textSolution -> textSolution.map { it.split("->") }.map { Pair(it[0], it[1]) } }
                    .map(::Solution)
        } catch (ex: Throwable) {
            correctEquations = false
        }

        probs.sortBy { it.second }
        return EquationProcessingResult(equations, correctEquations, solutions)
    }

    private fun groupSymbols(symbols: List<Symbol>): List<List<Symbol>> {
        val symbolsCopy = symbols.toMutableList()
        val equations = mutableListOf<List<Symbol>>()
        while (symbolsCopy.isNotEmpty()) {
            val box = symbolsCopy.first().box
            var rangeStart = box.y
            var rangeEnd = box.y + box.height
            val symbolsToRemove = mutableListOf<Symbol>()
            for (symbol in symbolsCopy) {
                val compBox = symbol.box
                if (max(rangeStart, compBox.y) <= min(rangeEnd, compBox.y + compBox.height)) {
                    rangeStart = min(rangeStart, compBox.y)
                    rangeEnd = max(rangeEnd, compBox.y + compBox.height)
                    symbolsToRemove.add(symbol)
                }
            }
            symbolsToRemove.apply {
                sortBy { it.box.x }
                symbolsCopy.removeAll(this)
                equations.add(this)
            }
        }
        return equations
    }


    //Convert OpenCv Mat to TensorflowLite input
    private fun convertMatToTfLiteInput(mat: Mat): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(IMG_SIZE * IMG_SIZE * 4).apply { order(ByteOrder.nativeOrder()) }
        threshold(mat, mat, 127.0, 255.0, THRESH_BINARY)
        imgData.rewind()
        for (i in 0 until mat.height()) {
            for (j in 0 until mat.width()) {
                imgData.putFloat((mat.get(i, j)[0] / 255.0).toFloat())
            }
        }
        return imgData
    }

    private fun findMaxProbSymbol(array: FloatArray) =
            LABELS[array.indexOf(array.max() ?: array.first())]

    private fun getStringRepresentation(predictions: List<String>, symbols: List<Symbol>): String {
        val expression = StringBuilder()
        var i = 0
        while (i < predictions.size) {
            if (predictions[i] == "-" && i + 1 < predictions.size && predictions[i + 1] == "-" && isEquals(symbols[i].box, symbols[i + 1].box)) {
                expression.append("=")
                i++
            } else if (i > 0 && isPower(symbols[i - 1].box, symbols[i].box, predictions[i - 1], predictions[i])) {
                expression.append("^").append(predictions[i])
            } else {
                expression.append(predictions[i])
            }
            i++
        }
        return expression.toString()
    }

    private fun isEquals(box1: Rect, box2: Rect) =
            abs(box1.x - box2.x) < max(box1.width, box2.width)

    private fun isPower(base: Rect, power: Rect, basePrediction: String, powerPrediction: String): Boolean {
        val illegalSymbols = listOf("+", "-", "/", "*")
        return if (illegalSymbols.contains(basePrediction) || illegalSymbols.contains(powerPrediction)) {
            false
        } else power.y < base.y && power.y + power.height < base.y + 0.5 * base.height && power.x > base.x + 0.5 * base.width
    }

    private fun convertEquationsToExpression(equations: List<String>) =
            "Solve({" + equations.stream().map<String> { eq -> eq.replace("=", "==") }.collect(Collectors.joining(",")) + "},{x,y,w,z})"

    companion object : SingletonHolder<EquationInterpreter, File>(::EquationInterpreter) {
        const val IMG_SIZE = 28
        val LABELS = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "-", "+", "w", "x", "y", "z", "/")
    }
}
