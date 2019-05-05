package com.github.wblachowski.camculator;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class Symbol {
    private final Rect box;
    private final Mat image;

    public Symbol(Rect box, Mat image) {
        this.box = box;
        this.image = image;
    }

    public Rect getBox() {
        return box;
    }

    public Mat getImage() {
        return image;
    }
}
