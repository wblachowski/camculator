package com.github.wblachowski.camculator;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tbruyelle.rxpermissions2.RxPermissions;

import org.matheclipse.core.expression.F;
import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Camera camera;
    private CameraPreview cameraPreview;
    private ImageView framePreview;
    private FrameLayout cropPreview;
    private TextView equationsTextView;
    private Rect cropRectangle = new Rect();
    private ImageProcessor imageProcessor = new ImageProcessor();
    private EquationInterpreter equationInterpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            F.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initDebug();
        askForCameraPermission();
        File file = new File(this.getFilesDir() + File.separator + "model.tflite");
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.model);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                fileOutputStream.write(buf, 0, len);
            }

            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        equationInterpreter = new EquationInterpreter(file);

        //TODO handle unavailable camera
        boolean hasCameraAccess = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

        camera = getCameraInstance();
        cameraPreview = new CameraPreview(this, camera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
        framePreview = findViewById(R.id.frame_preview);
        cropPreview = findViewById(R.id.crop_preview);
        equationsTextView = findViewById(R.id.equations_view);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        PixelConverter pixelConverter = new PixelConverter(getResources().getDisplayMetrics());
        int top = (getResources().getDisplayMetrics().widthPixels - cropPreview.getWidth()) / 2;
        int left = (int) pixelConverter.fromDp(50);
        cropRectangle = new Rect(left, top, cropPreview.getHeight() + left, cropPreview.getWidth() + top);
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        if (imageProcessor.isProcessing() || equationInterpreter.isProcessing()) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuvImage = new YuvImage(data, parameters.getPreviewFormat(), parameters.getPreviewSize().width, parameters.getPreviewSize().height, null);

        yuvImage.compressToJpeg(cropRectangle, 90, out);
        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        new ImageProcessingTask().execute(imageProcessor, equationInterpreter, bitmap, framePreview, equationsTextView);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void askForCameraPermission() {
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.CAMERA)
                .subscribe(granted -> {
                    if (!granted) {
                        //TODO handle camera access denial
                    }
                });
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = getCameraInstance();
        cameraPreview = new CameraPreview(this, camera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
    }
}
