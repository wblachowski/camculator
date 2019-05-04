package com.github.wblachowski.camculator;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.threshold;

public class EquationInterpreter {

    public boolean processing=false;
    private final Interpreter interpreter;
    protected ByteBuffer imgData;
    private float[][] probArray;
    private static final String[] LABELS = {"0","1","2","3","4","5","6","7","8","9","*","-","+","w","x","y","z","/"};

    public EquationInterpreter(File model) {
        interpreter = new Interpreter(model);
        imgData = ByteBuffer.allocateDirect(1 * 28 * 28 * 1 * 4);
        imgData.order(ByteOrder.nativeOrder());
        probArray = new float[1][18];
    }

    public List<String> findEquations(Size size, List<Rect> boxes, List<Mat> symbols) {
        processing=true;
        List<String> result = new ArrayList<>();
        for (Mat symbol : symbols) {
            if(symbol==null)continue;
            convertMattoTfLiteInput(symbol);
            interpreter.run(imgData, probArray);
            result.add(findMaxProbSymbol(probArray[0]));
        }
        processing=false;
        return result;
    }

    //convert opencv mat to tensorflowlite input
    private void convertMattoTfLiteInput(Mat mat) {
        threshold(mat, mat,127, 255, THRESH_BINARY);
        imgData.rewind();
        for (int i = 0; i < mat.height(); ++i) {
            for (int j = 0; j < mat.width(); ++j) {
                imgData.putFloat((float) (mat.get(i, j)[0]/255.));
            }
        }
    }

    private String findMaxProbSymbol(float[] array){
        int maxAt = 0;
        for (int i = 0; i < array.length; i++) {
            maxAt = array[i] > array[maxAt] ? i : maxAt;
        }
        return LABELS[maxAt];
    }
}
