package com.github.wblachowski.camculator

import android.graphics.Bitmap
import com.github.wblachowski.camculator.processing.result.ImagePreprocessingResult
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.CvType.CV_8UC1
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*
import org.opencv.photo.Photo.fastNlMeansDenoising
import java.util.*

class ImageProcessor {
    private val SCALE_FACTOR = 0.5
    var isProcessing: Boolean = false

    fun process(imageBitmap: Bitmap): ImagePreprocessingResult {
        isProcessing = true
        val img = Mat()
        val bmp32 = imageBitmap.copy(Bitmap.Config.ARGB_8888, true)
        Utils.bitmapToMat(bmp32, img)
        val orgSize = img.size()

        resize(img, img, Size(orgSize.width * SCALE_FACTOR, orgSize.height * SCALE_FACTOR))
        val binaryImg = fetchBinaryImg(img)
        val boxesImg = binaryImg.clone()
        val boxes = fetchBoxes(binaryImg, boxesImg)
        val symbols = extractSymbols(binaryImg, boxes)

        resize(boxesImg, boxesImg, orgSize)
        Utils.matToBitmap(boxesImg, bmp32)
        isProcessing = false
        return ImagePreprocessingResult(bmp32, symbols)
    }

    private fun fetchBinaryImg(mat: Mat): Mat {
        val binaryImg = Mat()
        Imgproc.cvtColor(mat, binaryImg, Imgproc.COLOR_RGB2GRAY)
        fastNlMeansDenoising(binaryImg, binaryImg, 13f, 7, 21)

        var blockSize = Math.max(binaryImg.height(), binaryImg.width()) / 7
        blockSize = if (blockSize % 2 == 0) blockSize + 1 else blockSize
        adaptiveThreshold(binaryImg, binaryImg, 255.0, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, blockSize, 5.0)
        return binaryImg
    }

    private fun fetchBoxes(binaryImg: Mat, boxesImg: Mat): List<Rect> {
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        findContours(binaryImg, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE)

        val boxes = ArrayList<Rect>()
        for (contour in contours) {
            boxes.add(boundingRect(contour))
        }

        //Remove too big boxes
        val maxSize = binaryImg.size().area() * 0.5
        boxes.removeIf { rect -> rect.size().area() > maxSize }

        //Remove boxes wholly contained in other boxes
        val boxesCopy = ArrayList(boxes)
        for (b1 in boxesCopy) {
            for (b2 in boxesCopy) {
                if (b1 != b2 && b1.contains(Point(b2.x.toDouble(), b2.y.toDouble())) && b1.contains(Point((b2.x + b2.width).toDouble(), (b2.y + b2.height).toDouble()))) {
                    boxes.remove(b2)
                }
            }
        }

        //Sort boxes vertically
        boxes.sortBy { it.y }
        for (box in boxes) {
            rectangle(boxesImg, Point(box.x.toDouble(), box.y.toDouble()), Point((box.x + box.width).toDouble(), (box.y + box.height).toDouble()), Scalar(0.0, 255.0, 0.0))
        }
        return boxes
    }

    private fun extractSymbols(binaryImg: Mat, boxes: List<Rect>): List<Symbol> {
        val size = 28
        val WHITE_SCALAR = Scalar(255.0)
        val symbols = ArrayList<Symbol>()
        for (box in boxes) {
            val symbol = binaryImg.submat(box)
            if (box.height >= box.width) {
                val newx = size * box.width / box.height
                if (newx == 0) continue
                resize(symbol, symbol, Size(newx.toDouble(), size.toDouble()))
                val rest = size - newx
                val restLeft = Math.ceil(rest.toDouble() / 2.0).toInt()
                val restRight = Math.floor(rest.toDouble() / 2.0).toInt()
                Core.hconcat(Arrays.asList(Mat(size, restLeft, CV_8UC1, WHITE_SCALAR), symbol, Mat(size, restRight, CV_8UC1, WHITE_SCALAR)), symbol)
            } else {
                val newy = size * box.height / box.width
                if (newy == 0) continue
                resize(symbol, symbol, Size(size.toDouble(), newy.toDouble()))
                val rest = size - newy
                val restUp = Math.ceil(rest.toDouble() / 2.0).toInt()
                val restDown = Math.floor(rest.toDouble() / 2.0).toInt()
                Core.vconcat(Arrays.asList(Mat(restUp, size, CV_8UC1, WHITE_SCALAR), symbol, Mat(restDown, size, CV_8UC1, WHITE_SCALAR)), symbol)
            }
            symbols.add(Symbol(box, symbol))
        }
        return symbols
    }
}
