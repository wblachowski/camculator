package com.github.wblachowski.camculator.utils

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
