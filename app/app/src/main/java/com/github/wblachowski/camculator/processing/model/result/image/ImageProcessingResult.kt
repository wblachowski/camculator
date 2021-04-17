package com.github.wblachowski.camculator.processing.model.result.image

import com.github.wblachowski.camculator.processing.model.Symbol
import org.opencv.core.Size

data class ImageProcessingResult(val symbols: List<Symbol>, val size: Size)
