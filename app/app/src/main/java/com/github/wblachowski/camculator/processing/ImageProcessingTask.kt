package com.github.wblachowski.camculator.processing

import android.os.AsyncTask
import com.github.wblachowski.camculator.processing.result.CompleteResult

class ImageProcessingTask : AsyncTask<Any, Void, CompleteResult>() {

    private lateinit var payload: Payload
    private val imageProcessor = ImageProcessor.getInstance()
    private val equationInterpreter = EquationInterpreter.getInstance()

    override fun doInBackground(objects: Array<Any>): CompleteResult {
        payload = objects.first() as Payload
        val result = imageProcessor.process(payload.bitmap, payload.cropRectangle)
        val equations = equationInterpreter.findEquations(result.symbols)
        return CompleteResult(result, equations)
    }

    override fun onPostExecute(result: CompleteResult) {
        payload.framePreview.setImageBitmap(result.preprocessingResult.boxesImg)
        payload.equationsView.text = result.equations.stream().map { s -> s + '\n' }.reduce { obj, str -> obj + str }.orElse("")
    }
}
