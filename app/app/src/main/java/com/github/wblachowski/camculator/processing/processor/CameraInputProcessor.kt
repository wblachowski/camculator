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
        yuvImage.compressToJpeg(Rect(0, 0, parameters.previewSize.width, parameters.previewSize.height), 90, out)
        val imageBytes = out.toByteArray()
        out.flush()
        out.close()
        val matrix = Matrix().apply { postRotate(90f) }
        var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return Bitmap.createBitmap(bitmap, rec.left, rec.top, rec.width(), rec.height())
    }

    fun processForPicture(data: ByteArray, rec: Rect): Bitmap {
        var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val matrix = Matrix().apply { postRotate(90f) }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return Bitmap.createBitmap(bitmap, rec.left, rec.top, rec.width(), rec.height())
    }

    companion object : ArglessSingletonHolder<CameraInputProcessor>(::CameraInputProcessor)
}