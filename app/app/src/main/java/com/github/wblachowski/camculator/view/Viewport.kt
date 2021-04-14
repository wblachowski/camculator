package com.github.wblachowski.camculator.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewGroup
import com.github.wblachowski.camculator.utils.PixelConverter


class Viewport @JvmOverloads constructor(context: Context?, attrs: AttributeSet?, defStyle: Int = 0) : ViewGroup(context, attrs, defStyle) {

    private val pixelConverter = PixelConverter(resources.displayMetrics)

    val cornerRadius = pixelConverter.fromDp(CORNER_RADIUS_DP)

    lateinit var rectangle: RectF
        private set

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) = setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)

    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {}

    override fun shouldDelayChildPressedState() = false

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        val margin = pixelConverter.fromDp(MARGIN_DP)
        val width = width.toFloat() - margin
        val height = width * HEIGHT_WIDTH_RATIO
        rectangle = RectF(margin, margin, width, height)

        val path = Path().apply {
            val frame = RectF(margin - cornerRadius / 2, margin - cornerRadius / 2, width + cornerRadius / 2, height + cornerRadius / 2)
            addRoundRect(frame, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        val stroke = Paint().apply {
            isAntiAlias = true
            strokeWidth = STROKE_WIDTH
            color = Color.WHITE
            style = Paint.Style.STROKE
        }
        val eraser = Paint().apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        canvas.drawPath(path, stroke)
        canvas.drawRoundRect(rectangle, cornerRadius, cornerRadius, eraser)
    }

    companion object {
        const val MARGIN_DP = 16
        const val CORNER_RADIUS_DP = 4
        const val STROKE_WIDTH = 4f
        const val HEIGHT_WIDTH_RATIO = 0.7f
    }
}