package com.github.wblachowski.camculator.processing.result;

import android.graphics.Bitmap;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import java.util.List;

public class ImagePreprocessingResult {

    private final Bitmap boxesImg;
    private final Size imgSize;
    private final List<Rect> boxes;
    private final List<Mat> symbols;

    public ImagePreprocessingResult(Bitmap boxesImg, Size imgSize, List<Rect> boxes, List<Mat> symbols) {
        this.boxesImg = boxesImg;
        this.imgSize = imgSize;
        this.boxes = boxes;
        this.symbols = symbols;
    }

    public Bitmap getBoxesImg() {
        return boxesImg;
    }

    public Size getImgSize() {
        return imgSize;
    }

    public List<Rect> getBoxes() {
        return boxes;
    }

    public List<Mat> getSymbols() {
        return symbols;
    }
}
