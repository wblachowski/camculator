package com.github.wblachowski.camculator.processing

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
import java.util.*
import java.util.stream.Collectors

class EquationInterpreter(model: File) {

    var isProcessing = false
        private set
    private val interpreter: Interpreter
    private val imgData: ByteBuffer
    private val probArray: Array<FloatArray>
    private val LABELS = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "-", "+", "w", "x", "y", "z", "/")

    init {
        interpreter = Interpreter(model)
        imgData = ByteBuffer.allocateDirect(28 * 28 * 4)
        imgData.order(ByteOrder.nativeOrder())
        probArray = Array(1) { FloatArray(18) }
    }

    companion object : SingletonHolder<EquationInterpreter, File>(::EquationInterpreter)

    fun findEquations(symbols: List<Symbol>): List<String> {
        val equations = getEquations(symbols)
        isProcessing = true
        val result = ArrayList<String>()
        for (equation in equations) {
            val predictions = ArrayList<String>()
            for (symbol in equation) {
                val img = symbol.image ?: continue
                convertMattoTfLiteInput(img)
                interpreter.run(imgData, probArray)
                predictions.add(findMaxProbSymbol(probArray[0]))
            }
            result.add(getAnalizedEquation(predictions, equation))
        }
        val expr = "Solve({" + result.stream().map<String> { eq -> eq.replace("=", "==") }.collect(Collectors.joining(",")) + "},{x,y,w,z})"
        try {
            var solution = ExprEvaluator().eval(expr).toString()
            solution = solution.replace("Solve(", "").replace(",{x,y,w,z})", "")
            solution = solution.substring(1, solution.length - 2)
                    .replace("->".toRegex(), ": ").replace("\\}".toRegex(), "\n").replace("\\{".toRegex(), "")
                    .replace("\n,".toRegex(), "\n").replace(",".toRegex(), ", ")
            result.add('\n' + solution)
        } catch (ex: Throwable) {
            result.add("Incorrect equation" + if (result.size > 1) "s" else "")
        }

        isProcessing = false
        return result
    }

    private fun getEquations(symbols: List<Symbol>): List<List<Symbol>> {
        val symbolsCopy = ArrayList(symbols)
        val equations = ArrayList<List<Symbol>>()
        while (!symbolsCopy.isEmpty()) {
            val box = symbolsCopy[0].box
            var rangeA = box.y
            var rangeB = box.y + box.height
            val symbolsToRemove = ArrayList<Symbol>()
            for (j in symbolsCopy.indices) {
                val compBox = symbolsCopy[j].box
                if (Math.max(rangeA, compBox.y) <= Math.min(rangeB, compBox.y + compBox.height)) {
                    rangeA = Math.min(rangeA, compBox.y)
                    rangeB = Math.max(rangeB, compBox.y + compBox.height)
                    symbolsToRemove.add(symbolsCopy[j])
                }
            }
            symbolsCopy.removeAll(symbolsToRemove)
            symbolsToRemove.sortBy { it.box.x }
            equations.add(ArrayList(symbolsToRemove))
        }
        return equations
    }

    //convert opencv mat to tensorflowlite input
    private fun convertMattoTfLiteInput(mat: Mat) {
        threshold(mat, mat, 127.0, 255.0, THRESH_BINARY)
        imgData.rewind()
        for (i in 0 until mat.height()) {
            for (j in 0 until mat.width()) {
                imgData.putFloat((mat.get(i, j)[0] / 255.0).toFloat())
            }
        }
    }

    private fun findMaxProbSymbol(array: FloatArray): String {
        var maxAt = 0
        for (i in array.indices) {
            maxAt = if (array[i] > array[maxAt]) i else maxAt
        }
        return LABELS[maxAt]
    }

    private fun getAnalizedEquation(predictions: List<String>, symbols: List<Symbol>): String {
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

    private fun isEquals(box1: Rect, box2: Rect): Boolean {
        return Math.abs(box1.x - box2.x) < Math.max(box1.width, box2.width)
    }

    private fun isPower(base: Rect, power: Rect, basePrediction: String, powerPrediction: String): Boolean {
        val illegalSymbols = Arrays.asList("+", "-", "/", "*")
        return if (illegalSymbols.contains(basePrediction) || illegalSymbols.contains(powerPrediction)) {
            false
        } else power.y < base.y && power.y + power.height < base.y + 0.5 * base.height && power.x > base.x + 0.5 * base.width
    }

}
