package com.github.wblachowski.camculator.processing

import android.graphics.Bitmap
import com.github.wblachowski.camculator.utils.ArglessSingletonHolder
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class BoxesProcessor {

    fun process(symbols: List<InterpretedSymbol>, inputSize: Size, finalSize: Size): Bitmap {
        val boxesImg = Mat(inputSize, CvType.CV_8U)
        Imgproc.cvtColor(boxesImg, boxesImg, Imgproc.COLOR_GRAY2RGBA, 4)
        boxesImg.setTo(Scalar(.0, .0, .0, .0))

        symbols.forEach { symbol ->
            val box = symbol.box
            val textPoint = when(symbol.value){
                "x","y","w","z"-> Point((box.x+4).toDouble(), (box.y + 15).toDouble())
                "-" -> Point((box.x+2).toDouble(), (box.y + 10).toDouble())
                "+" ->Point((box.x+1).toDouble(), (box.y + 17).toDouble())
                "*" ->Point((box.x+2).toDouble(), (box.y + 14).toDouble())
                "/" -> Point((box.x+4).toDouble(), (box.y + 22).toDouble())
                else -> Point((box.x+3).toDouble(), (box.y + 20).toDouble())
            }
            Imgproc.putText(boxesImg, symbol.value, textPoint, Core.FONT_HERSHEY_SIMPLEX, .7, Scalar(255.0, 255.0, 255.0, 255.0), 2)
            Imgproc.rectangle(boxesImg, Point(box.x.toDouble(), box.y.toDouble()), Point((box.x + box.width).toDouble(), (box.y + box.height).toDouble()), Scalar(255.0, 255.0, 255.0, 255.0), 2)
        }

        Imgproc.resize(boxesImg, boxesImg, Size(finalSize.height, finalSize.width))
        val boxesBitmap = Bitmap.createBitmap(boxesImg.cols(), boxesImg.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(boxesImg, boxesBitmap)
        return boxesBitmap
    }

    companion object : ArglessSingletonHolder<BoxesProcessor>(::BoxesProcessor)
}