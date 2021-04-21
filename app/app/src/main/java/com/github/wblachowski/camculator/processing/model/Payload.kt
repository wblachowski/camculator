package com.github.wblachowski.camculator.processing.model

import android.graphics.Rect
import android.graphics.RectF
import android.hardware.Camera

open class Payload(open val data: ByteArray, open val camera: Camera, open val cropRectangle: RectF, open val dataRectangle: Rect)

data class PreviewPayload(override val data: ByteArray, override val camera: Camera, override val cropRectangle: RectF, override val dataRectangle: Rect) : Payload(data, camera, cropRectangle, dataRectangle)

data class PicturePayload(override val data: ByteArray, override val camera: Camera, override val cropRectangle: RectF, override val dataRectangle: Rect) : Payload(data, camera, cropRectangle, dataRectangle)