package com.github.wblachowski.camculator.processing

import android.os.AsyncTask
import com.github.wblachowski.camculator.processing.model.Payload
import com.github.wblachowski.camculator.processing.model.result.ProcessingResult
import com.github.wblachowski.camculator.processing.processor.BoxesProcessor
import com.github.wblachowski.camculator.processing.processor.EquationInterpreter
import com.github.wblachowski.camculator.processing.processor.ImageProcessor
import org.opencv.core.Size


class ImageProcessingTask(val onPostProcessing: (ProcessingResult) -> Any) : AsyncTask<Payload, Void, ProcessingResult>() {

    private lateinit var payload: Payload
    private val imageProcessor = ImageProcessor.getInstance()
    private val equationInterpreter = EquationInterpreter.getInstance()
    private val boxesProcessor = BoxesProcessor.getInstance()

    override fun doInBackground(objects: Array<Payload>): ProcessingResult {
        payload = objects.first()
        val imageResult = imageProcessor.process(payload.bitmap)
        val equationResult = equationInterpreter.findEquations(imageResult.symbols)
        val finalSize = Size(payload.cropRectangle.width().toDouble(), payload.cropRectangle.height().toDouble())
        val boxesBitmap = boxesProcessor.process(equationResult.equations.flatMap { it.symbols }, imageResult.size, finalSize)
        return ProcessingResult(imageResult, equationResult, boxesBitmap)
    }

    override fun onPostExecute(result: ProcessingResult) {
        onPostProcessing(result)
    }
}
