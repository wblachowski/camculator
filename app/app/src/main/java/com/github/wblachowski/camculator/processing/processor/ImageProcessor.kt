package com.github.wblachowski.camculator.processing.processor

import android.graphics.Bitmap
import com.github.wblachowski.camculator.processing.model.Symbol
import com.github.wblachowski.camculator.processing.model.result.image.ImageProcessingResult
import com.github.wblachowski.camculator.utils.ArglessSingletonHolder
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.CvType.CV_8UC1
import org.opencv.imgproc.Imgproc.*
import org.opencv.photo.Photo.fastNlMeansDenoising
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class ImageProcessor {

    fun process(imageBitmap: Bitmap): ImageProcessingResult {
        val img = Mat()
        val bmp32 = imageBitmap.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(bmp32, img)
        val orgSize = img.size()

        resize(img, img, Size(orgSize.width * SCALE_FACTOR, orgSize.height * SCALE_FACTOR))
        val binaryImg = fetchBinaryImg(img)
        val boxes = fetchBoxes(binaryImg)
        val symbols = extractSymbols(binaryImg, boxes)

        return ImageProcessingResult(symbols, binaryImg.size())
    }

    private fun fetchBinaryImg(mat: Mat): Mat {
        val binaryImg = Mat()
        cvtColor(mat, binaryImg, COLOR_RGB2GRAY)
        fastNlMeansDenoising(binaryImg, binaryImg, 13f, 7, 21)

        var blockSize = max(binaryImg.height(), binaryImg.width()) / 7
        blockSize = if (blockSize % 2 == 0) blockSize + 1 else blockSize
        adaptiveThreshold(binaryImg, binaryImg, 255.0, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, blockSize, 5.0)
        return binaryImg
    }

    private fun fetchBoxes(binaryImg: Mat): List<Rect> {
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        findContours(binaryImg, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE)

        val maxSize = binaryImg.size().area() * 0.5

        //Create boxes filtering out too big ones
        val boxes = contours.map { boundingRect(it) }.filter { box -> box.size().area() <= maxSize }.sortedBy { it.y }.toMutableList()

        //Remove boxes wholly contained in other boxes
        val boxesCopy = ArrayList(boxes)
        for (b1 in boxesCopy) {
            for (b2 in boxesCopy) {
                if (b1 != b2 && b1.contains(Point(b2.x.toDouble(), b2.y.toDouble())) && b1.contains(Point((b2.x + b2.width).toDouble(), (b2.y + b2.height).toDouble()))) {
                    boxes.remove(b2)
                }
            }
        }

        return boxes
    }

    private fun extractSymbols(binaryImg: Mat, boxes: List<Rect>): List<Symbol> {
        val size = 28
        val symbols = ArrayList<Symbol>()
        for (box in boxes) {
            val symbol = binaryImg.submat(box)
            if (box.height >= box.width) {
                val newX = size * box.width / box.height
                if (newX == 0) continue
                resize(symbol, symbol, Size(newX.toDouble(), size.toDouble()))
                val rest = size - newX
                val restLeft = ceil(rest.toDouble() / 2.0).toInt()
                val restRight = floor(rest.toDouble() / 2.0).toInt()
                Core.hconcat(listOf(Mat(size, restLeft, CV_8UC1, WHITE_SCALAR), symbol, Mat(size, restRight, CV_8UC1, WHITE_SCALAR)), symbol)
            } else {
                val newY = size * box.height / box.width
                if (newY == 0) continue
                resize(symbol, symbol, Size(size.toDouble(), newY.toDouble()))
                val rest = size - newY
                val restUp = ceil(rest.toDouble() / 2.0).toInt()
                val restDown = floor(rest.toDouble() / 2.0).toInt()
                Core.vconcat(listOf(Mat(restUp, size, CV_8UC1, WHITE_SCALAR), symbol, Mat(restDown, size, CV_8UC1, WHITE_SCALAR)), symbol)
            }
            symbols.add(Symbol(box, symbol))
        }
        return symbols
    }

    companion object : ArglessSingletonHolder<ImageProcessor>(::ImageProcessor) {
        private const val SCALE_FACTOR = 0.5
        private val WHITE_SCALAR = Scalar(255.0)
    }
}
