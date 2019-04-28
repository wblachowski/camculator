package com.github.wblachowski.camculator;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImageProcessingTask extends AsyncTask<Object, Void, Bitmap> {

    ImageView preview;

    @Override
    protected Bitmap doInBackground(Object[] objects) {
        ImageProcessor imageProcessor = (ImageProcessor) objects[0];
        Bitmap bitmap = (Bitmap) objects[1];
        preview = (ImageView) objects[2];
        Bitmap result = imageProcessor.process(bitmap);
        return result;
    }

    protected void onPostExecute(Bitmap result) {
        preview.setImageBitmap(result);
    }
}
