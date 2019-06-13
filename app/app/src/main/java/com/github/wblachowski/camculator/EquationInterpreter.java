package com.github.wblachowski.camculator;

import org.matheclipse.core.eval.ExprEvaluator;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<String> findEquations(List<Symbol> symbols) {
        List<List<Symbol>> equations = getEquations(symbols);
        processing = true;
        List<String> result = new ArrayList<>();
        for (List<Symbol> equation : equations) {
            List<String> predictions = new ArrayList<>();
            for (Symbol symbol : equation) {
                Mat img = symbol.getImage();
                if (img == null) continue;
                convertMattoTfLiteInput(img);
                interpreter.run(imgData, probArray);
                predictions.add(findMaxProbSymbol(probArray[0]));
            }
            result.add(getAnalizedEquation(predictions, equation));
        }
        String expr = "Solve({" + result.stream().map(eq -> eq.replace("=", "==")).collect(Collectors.joining(",")) + "},{x,y,w,z})";
        try {
            String solution = new ExprEvaluator().eval(expr).toString();
            solution = solution.replace("Solve(", "").replace(",{x,y,w,z})", "");
            solution = solution.substring(1, solution.length() - 2)
                    .replaceAll("->", ": ").replaceAll("\\}", "\n").replaceAll("\\{", "")
                    .replaceAll("\n,", "\n").replaceAll(",", ", ");
            result.add('\n' + solution);
        } catch (Throwable ex) {
            result.add("Incorrect equation" + (result.size() > 1 ? "s" : ""));
        }
        processing = false;
        return result;
    }

    public boolean isProcessing() {
        return processing;
    }

    private List<List<Symbol>> getEquations(List<Symbol> symbols) {
        List<Symbol> symbolsCopy = new ArrayList<>(symbols);
        List<List<Symbol>> equations = new ArrayList<>();
        while (!symbolsCopy.isEmpty()) {
            Rect box = symbolsCopy.get(0).getBox();
            int rangeA = box.y;
            int rangeB = box.y + box.height;
            List<Symbol> symbolsToRemove = new ArrayList<>();
            for (int j = 0; j < symbolsCopy.size(); j++) {
                Rect compBox = symbolsCopy.get(j).getBox();
                if (Math.max(rangeA, compBox.y) <= Math.min(rangeB, compBox.y + compBox.height)) {
                    rangeA = Math.min(rangeA, compBox.y);
                    rangeB = Math.max(rangeB, compBox.y + compBox.height);
                    symbolsToRemove.add(symbolsCopy.get(j));
                }
            }
            symbolsCopy.removeAll(symbolsToRemove);
            symbolsToRemove.sort(Comparator.comparingInt(s -> s.getBox().x));
            equations.add(new ArrayList<>(symbolsToRemove));
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

    private String getAnalizedEquation(List<String> predictions, List<Symbol> symbols) {
        StringBuilder expression = new StringBuilder();
        int i = 0;
        while (i < predictions.size()) {
            if (predictions.get(i).equals("-") && i + 1 < predictions.size() && predictions.get(i + 1).equals("-") && isEquals(symbols.get(i).getBox(), symbols.get(i + 1).getBox())) {
                expression.append("=");
                i++;
            } else if (i > 0 && isPower(symbols.get(i - 1).getBox(), symbols.get(i).getBox(), predictions.get(i - 1), predictions.get(i))) {
                expression.append("^").append(predictions.get(i));
            } else {
                expression.append(predictions.get(i));
            }
            i++;
        }
        return expression.toString();
    }

    private boolean isEquals(Rect box1, Rect box2) {
        return Math.abs(box1.x - box2.x) < Math.max(box1.width, box2.width);
    }

    private boolean isPower(Rect base, Rect power, String basePrediction, String powerPrediction) {
        List<String> illegalSymbols = Arrays.asList("+", "-", "/", "*");
        if (illegalSymbols.contains(basePrediction) || illegalSymbols.contains(powerPrediction)) {
            return false;
        }
        return power.y < base.y && power.y + power.height < base.y + 0.5 * base.height && power.x > base.x + 0.5 * base.width;
    }
}
