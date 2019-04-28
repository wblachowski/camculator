package com.github.wblachowski.camculator;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.util.List;

public class ImageProcessingTask extends AsyncTask<Object, Void, ImageProcessingResult> {

    ImageView preview;

    @Override
    protected ImageProcessingResult doInBackground(Object[] objects) {
        ImageProcessor imageProcessor = (ImageProcessor) objects[0];
        EquationInterpreter equationIntepreter = (EquationInterpreter) objects[1];
        Bitmap bitmap = (Bitmap) objects[2];
        preview = (ImageView) objects[3];
        ImageProcessingResult result = imageProcessor.process(bitmap);
//        List<String> equations = equationIntepreter.findEquations(result.imgSize,result.boxes,result.symbols);
//        System.out.println(equations);
        return result;
    }

    protected void onPostExecute(ImageProcessingResult result) {
        preview.setImageBitmap(result.getBoxesImg());
    }
}
