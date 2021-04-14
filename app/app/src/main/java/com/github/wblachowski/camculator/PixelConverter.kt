package com.github.wblachowski.camculator

import android.util.DisplayMetrics
import android.util.TypedValue

class PixelConverter(private val displayMetrics: DisplayMetrics) {

    fun fromDp(dp: Number) =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp.toFloat(),
                    displayMetrics
            )
}
