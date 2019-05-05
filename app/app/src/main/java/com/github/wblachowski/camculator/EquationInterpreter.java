package com.github.wblachowski.camculator;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.threshold;

public class EquationInterpreter {

    private boolean processing = false;
    private final Interpreter interpreter;
    private ByteBuffer imgData;
    private float[][] probArray;
    private static final String[] LABELS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "-", "+", "w", "x", "y", "z", "/"};

    public EquationInterpreter(File model) {
        interpreter = new Interpreter(model);
        imgData = ByteBuffer.allocateDirect(28 * 28 * 4);
        imgData.order(ByteOrder.nativeOrder());
        probArray = new float[1][18];
    }

    public List<String> findEquations(Symbols symbols) {
        List<List<Mat>> equations = getEquations(symbols);
        processing = true;
        List<String> result = new ArrayList<>();
        for (List<Mat> equation : equations) {
            StringBuilder equationBuilder = new StringBuilder();
            for (Mat symbol : equation) {
                if (symbol == null) continue;
                convertMattoTfLiteInput(symbol);
                interpreter.run(imgData, probArray);
                equationBuilder.append(findMaxProbSymbol(probArray[0]));
            }
            result.add(equationBuilder.toString());
        }
        processing = false;
        return result;
    }

    public boolean isProcessing() {
        return processing;
    }

    private List<List<Mat>> getEquations(Symbols symbols) {
        Symbols symbolsCopy = new Symbols(symbols);
        List<List<Mat>> equations = new ArrayList<>();
        while (!symbolsCopy.getBoxes().isEmpty()) {
            Rect box = symbolsCopy.getBoxes().get(0);
            List<Rect> boxesToRemove = new ArrayList<>();
            List<Mat> imagesToRemove = new ArrayList<>();
            for (int j = 0; j < symbolsCopy.getBoxes().size(); j++) {
                Rect compBox = symbolsCopy.getBoxes().get(j);
                if (Math.max(box.y, compBox.y) <= Math.min(box.y + box.height, compBox.y + compBox.height)) {
                    boxesToRemove.add(symbolsCopy.getBoxes().get(j));
                    imagesToRemove.add(symbolsCopy.getImages().get(j));
                }
            }
            symbolsCopy.getBoxes().removeAll(boxesToRemove);
            symbolsCopy.getImages().removeAll(imagesToRemove);
            equations.add(new ArrayList<>(imagesToRemove));
        }
        return equations;
    }

    //convert opencv mat to tensorflowlite input
    private void convertMattoTfLiteInput(Mat mat) {
        threshold(mat, mat, 127, 255, THRESH_BINARY);
        imgData.rewind();
        for (int i = 0; i < mat.height(); ++i) {
            for (int j = 0; j < mat.width(); ++j) {
                imgData.putFloat((float) (mat.get(i, j)[0] / 255.));
            }
        }
    }

    private String findMaxProbSymbol(float[] array) {
        int maxAt = 0;
        for (int i = 0; i < array.length; i++) {
            maxAt = array[i] > array[maxAt] ? i : maxAt;
        }
        return LABELS[maxAt];
    }
}
