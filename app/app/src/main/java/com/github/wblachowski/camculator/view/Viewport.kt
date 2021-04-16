package com.github.wblachowski.camculator.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewGroup
import com.github.wblachowski.camculator.utils.PixelConverter


class Viewport @JvmOverloads constructor(context: Context?, attrs: AttributeSet?, defStyle: Int = 0) : ViewGroup(context, attrs, defStyle) {

    private val pixelConverter = PixelConverter(resources.displayMetrics)
    private var y: Float? = null

    val cornerRadius = pixelConverter.fromDp(CORNER_RADIUS_DP)

    lateinit var rectangle: RectF
        private set

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) = setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {}

    override fun shouldDelayChildPressedState() = false

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        val margin = pixelConverter.fromDp(MARGIN_DP)
        val width = width.toFloat() - margin
        val height = y?.apply { minus(margin) } ?: width * HEIGHT_WIDTH_RATIO
        rectangle = RectF(margin, margin, width, height)

        val path = Path().apply {
            val frame = RectF(margin, margin, width, height)
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

    fun repaint(y: Float) {
        this.y = y
        invalidate()
    }

    companion object {
        const val MARGIN_DP = 16
        const val CORNER_RADIUS_DP = 4
        const val STROKE_WIDTH = 6f
        const val HEIGHT_WIDTH_RATIO = 0.7f
    }
}