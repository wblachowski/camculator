package com.github.wblachowski.camculator.processing

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.AsyncTask
import android.widget.ImageView
import android.widget.TextView
import com.github.wblachowski.camculator.processing.result.CompleteResult

class ImageProcessingTask : AsyncTask<Any, Void, CompleteResult>() {

    private var preview: ImageView? = null
    private var equationsTextView: TextView? = null
    private var bitmap: Bitmap? = null

    override fun doInBackground(objects: Array<Any>): CompleteResult {
        val imageProcessor = objects[0] as ImageProcessor
        val equationInterpreter = objects[1] as EquationInterpreter
        bitmap = objects[2] as Bitmap
        preview = objects[3] as ImageView
        equationsTextView = objects[4] as TextView
        val orgSize = objects[5] as Rect
        val result = imageProcessor.process(bitmap!!, orgSize)
        val equations = equationInterpreter.findEquations(result.symbols)
        return CompleteResult(result, equations)
    }

    override fun onPostExecute(result: CompleteResult) {
        preview!!.setImageBitmap(result.preprocessingResult.boxesImg)
        equationsTextView!!.text = result.equations.stream().map { s -> s + '\n' }.reduce { obj, str -> obj + str }.orElse("")
    }
}
