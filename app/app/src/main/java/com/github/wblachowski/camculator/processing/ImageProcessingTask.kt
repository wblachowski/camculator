package com.github.wblachowski.camculator.processing

import android.os.AsyncTask
import com.github.wblachowski.camculator.processing.result.ProcessingResult


class ImageProcessingTask(val onPostProcessing: (ProcessingResult) -> Unit) : AsyncTask<Any, Void, ProcessingResult>() {

    private lateinit var payload: Payload
    private val imageProcessor = ImageProcessor.getInstance()
    private val equationInterpreter = EquationInterpreter.getInstance()

    override fun doInBackground(objects: Array<Any>): ProcessingResult {
        payload = objects.first() as Payload
        val result = imageProcessor.process(payload.bitmap, payload.cropRectangle)
        val equations = equationInterpreter.findEquations(result.symbols)
        return ProcessingResult(result, equations)
    }

    override fun onPostExecute(result: ProcessingResult) = onPostProcessing(result)
}
