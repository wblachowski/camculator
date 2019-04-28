package com.github.wblachowski.camculator;

import android.util.DisplayMetrics;
import android.util.TypedValue;

public class PixelConverter {

    private final DisplayMetrics displayMetrics;

    public PixelConverter(DisplayMetrics displayMetrics) {
        this.displayMetrics = displayMetrics;
    }

    public float fromDp(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                displayMetrics
        );
    }
}
