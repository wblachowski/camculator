package com.github.wblachowski.camculator.processing.result;

import android.graphics.Bitmap;

import com.github.wblachowski.camculator.Symbol;

import java.util.List;

public class ImagePreprocessingResult {

    private final Bitmap boxesImg;
    private final List<Symbol> symbols;

    public ImagePreprocessingResult(Bitmap boxesImg, List<Symbol> symbols) {
        this.boxesImg = boxesImg;
        this.symbols = symbols;
    }

    public Bitmap getBoxesImg() {
        return boxesImg;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }
}
