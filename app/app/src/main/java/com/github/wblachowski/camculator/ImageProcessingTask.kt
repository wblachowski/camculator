package com.github.wblachowski.camculator

import android.graphics.Bitmap
import android.os.AsyncTask
import android.widget.ImageView
import android.widget.TextView
import com.github.wblachowski.camculator.processing.result.CompleteResult

class ImageProcessingTask : AsyncTask<Any, Void, CompleteResult>() {

    private var preview: ImageView? = null
    private var equationsTextView: TextView? = null

    override fun doInBackground(objects: Array<Any>): CompleteResult {
        val imageProcessor = objects[0] as ImageProcessor
        val equationIntepreter = objects[1] as EquationInterpreter
        val bitmap = objects[2] as Bitmap
        preview = objects[3] as ImageView
        equationsTextView = objects[4] as TextView
        val result = imageProcessor.process(bitmap)
        val equations = equationIntepreter.findEquations(result.symbols)
        return CompleteResult(result, equations)
    }

    override fun onPostExecute(result: CompleteResult) {
        preview!!.setImageBitmap(result.preprocessingResult.boxesImg)
        equationsTextView!!.text = result.equations.stream().map { s -> s + '\n' }.reduce { obj, str -> obj + str }.orElse("")
    }
}