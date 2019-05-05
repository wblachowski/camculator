package com.github.wblachowski.camculator;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.wblachowski.camculator.processing.result.CompleteResult;
import com.github.wblachowski.camculator.processing.result.ImagePreprocessingResult;

import java.util.List;

public class ImageProcessingTask extends AsyncTask<Object, Void, CompleteResult> {

    private ImageView preview;
    private TextView equationsTextView;

    @Override
    protected CompleteResult doInBackground(Object[] objects) {
        ImageProcessor imageProcessor = (ImageProcessor) objects[0];
        EquationInterpreter equationIntepreter = (EquationInterpreter) objects[1];
        Bitmap bitmap = (Bitmap) objects[2];
        preview = (ImageView) objects[3];
        equationsTextView = (TextView) objects[4];
        ImagePreprocessingResult result = imageProcessor.process(bitmap);
        List<String> equations = equationIntepreter.findEquations(result.getSymbols());
        return new CompleteResult(result, equations);
    }

    protected void onPostExecute(CompleteResult result) {
        preview.setImageBitmap(result.getPreprocessingResult().getBoxesImg());
        equationsTextView.setText(result.getEquations().stream().map(s -> s + '\n').reduce(String::concat).orElse(""));
    }
}
