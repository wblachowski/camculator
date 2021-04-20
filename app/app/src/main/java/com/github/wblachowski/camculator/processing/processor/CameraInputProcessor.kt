package com.github.wblachowski.camculator.processing.processor

import android.graphics.*
import android.hardware.Camera
import com.github.wblachowski.camculator.utils.ArglessSingletonHolder
import java.io.ByteArrayOutputStream

class CameraInputProcessor {

    fun processForPreview(data: ByteArray, camera: Camera, rec: Rect): Bitmap {
        val parameters = camera.parameters
        val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(data, parameters.previewFormat, parameters.previewSize.width, parameters.previewSize.height, null)
        yuvImage.compressToJpeg(rec, 90, out)
        val imageBytes = out.toByteArray()
        out.flush()
        out.close()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        val matrix = Matrix().apply { postRotate(90f) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun processForPicture(data: ByteArray, rec: Rect): Bitmap {
        var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        bitmap = Bitmap.createBitmap(bitmap, rec.left, rec.top, rec.width(), rec.height())
        val matrix = Matrix().apply { postRotate(90f) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    }

    companion object : ArglessSingletonHolder<CameraInputProcessor>(::CameraInputProcessor)
}