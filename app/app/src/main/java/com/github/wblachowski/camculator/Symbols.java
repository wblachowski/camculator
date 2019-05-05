package com.github.wblachowski.camculator;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

public class Symbols {
    private final List<Rect> boxes;
    private final List<Mat> images;

    public Symbols(List<Rect> boxes, List<Mat> images) {
        this.boxes = boxes;
        this.images = images;
    }

    public Symbols(Symbols symbolsToClone) {
        this.boxes = new ArrayList<>(symbolsToClone.boxes);
        this.images = new ArrayList<>(symbolsToClone.images);
    }

    public List<Rect> getBoxes() {
        return boxes;
    }

    public List<Mat> getImages() {
        return images;
    }
}
