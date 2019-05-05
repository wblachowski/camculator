package com.github.wblachowski.camculator.processing.result;

import android.graphics.Bitmap;

import com.github.wblachowski.camculator.Symbols;

public class ImagePreprocessingResult {

    private final Bitmap boxesImg;
    private final Symbols symbols;

    public ImagePreprocessingResult(Bitmap boxesImg, Symbols symbols) {
        this.boxesImg = boxesImg;
        this.symbols = symbols;
    }

    public Bitmap getBoxesImg() {
        return boxesImg;
    }

    public Symbols getSymbols() {
        return symbols;
    }
}
