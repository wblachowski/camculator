package com.github.wblachowski.camculator;

import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.imports.graphmapper.tf.TFGraphMapper;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EquationInterpreter {

    public EquationInterpreter(File model) {
        long start = System.currentTimeMillis();
        SameDiff sd = TFGraphMapper.getInstance().importGraph(model);
        System.out.println("Took: " + (System.currentTimeMillis()-start)/1000 +" seconds");
    }

    public List<String> findEquations(Size size, List<Rect> boxes, List<Mat> symbols) {
        return new ArrayList<>();
    }
}
