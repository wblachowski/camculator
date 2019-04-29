package com.github.wblachowski.camculator.processing.result;

import java.util.List;

public class CompleteResult {

    private final ImagePreprocessingResult preprocessingResult;
    private final List<String> equations;

    public CompleteResult(ImagePreprocessingResult preprocessingResult, List<String> equations){
        this.preprocessingResult=preprocessingResult;
        this.equations=equations;
    }

    public ImagePreprocessingResult getPreprocessingResult() {
        return preprocessingResult;
    }

    public List<String> getEquations() {
        return equations;
    }
}
