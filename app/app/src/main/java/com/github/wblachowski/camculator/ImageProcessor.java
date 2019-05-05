package com.github.wblachowski.camculator;

import android.graphics.Bitmap;

import com.github.wblachowski.camculator.processing.result.ImagePreprocessingResult;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.photo.Photo.fastNlMeansDenoising;

public class ImageProcessor {

    private static final double SCALE_FACTOR = 0.5;
    private boolean processing = false;

    public ImagePreprocessingResult process(Bitmap imageBitmap) {
        processing = true;
        Mat img = new Mat();
        Bitmap bmp32 = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, img);
        Size orgSize = img.size();

        resize(img, img, new Size(orgSize.width * SCALE_FACTOR, orgSize.height * SCALE_FACTOR));
        Mat binaryImg = fetchBinaryImg(img);
        Mat boxesImg = binaryImg.clone();
        List<Rect> boxes = fetchBoxes(binaryImg, boxesImg);
        List<Symbol> symbols = extractSymbols(binaryImg, boxes);

        resize(boxesImg, boxesImg, orgSize);

        //Uncomment for debugging purposes:
//        for(int i=0;i<6;i++){
//            if(i>=symbols.size())break;
//            Mat scaledSymbol = new Mat();
//            resize(symbols.get(i),scaledSymbol,new Size(128,128));
//            scaledSymbol.rowRange(0,127).colRange(0,127).copyTo(boxesImg.rowRange(0,127).colRange(128*i,127+128*i));
//        }
        Utils.matToBitmap(boxesImg, bmp32);
        processing = false;
        return new ImagePreprocessingResult(bmp32, symbols);
    }

    public boolean isProcessing() {
        return processing;
    }

    private Mat fetchBinaryImg(Mat mat) {
        Mat binaryImg = new Mat();
        Imgproc.cvtColor(mat, binaryImg, Imgproc.COLOR_RGB2GRAY);
        fastNlMeansDenoising(binaryImg, binaryImg, 13, 7, 21);

        int blockSize = Math.max(binaryImg.height(), binaryImg.width()) / 7;
        blockSize = blockSize % 2 == 0 ? blockSize + 1 : blockSize;
        adaptiveThreshold(binaryImg, binaryImg, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, blockSize, 5);
        return binaryImg;
    }

    private List<Rect> fetchBoxes(Mat binaryImg, Mat boxesImg) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        findContours(binaryImg, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

        List<Rect> boxes = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            boxes.add(boundingRect(contour));
        }

        //Remove too big boxes
        double maxSize = binaryImg.size().area() * 0.5;
        boxes.removeIf(rect -> rect.size().area() > maxSize);

        //Remove boxes wholly contained in other boxes
        List<Rect> boxesCopy = new ArrayList<>(boxes);
        for (Rect b1 : boxesCopy) {
            for (Rect b2 : boxesCopy) {
                if (!b1.equals(b2) && b1.contains(new Point(b2.x, b2.y)) && b1.contains(new Point(b2.x + b2.width, b2.y + b2.height))) {
                    boxes.remove(b2);
                }
            }
        }

        //Sort boxes vertically
        boxes.sort(Comparator.comparingInt(b -> b.y));
        for (Rect box : boxes) {
            rectangle(boxesImg, new Point(box.x, box.y), new Point(box.x + box.width, box.y + box.height), new Scalar(0, 255, 0));
        }
        return boxes;
    }

    private List<Symbol> extractSymbols(Mat binaryImg, List<Rect> boxes) {
        int size = 28;
        final Scalar WHITE_SCALAR = new Scalar(255);
        List<Symbol> symbols = new ArrayList<>();
        for (Rect box : boxes) {
            Mat symbol = binaryImg.submat(box);
            if (box.height >= box.width) {
                int newx = size * box.width / box.height;
                if (newx == 0) continue;
                resize(symbol, symbol, new Size(newx, size));
                int rest = size - newx;
                int restLeft = (int) Math.ceil((double) rest / 2.);
                int restRight = (int) Math.floor((double) rest / 2.);
                Core.hconcat(Arrays.asList(new Mat(size, restLeft, CV_8UC1, WHITE_SCALAR), symbol, new Mat(size, restRight, CV_8UC1, WHITE_SCALAR)), symbol);
            } else {
                int newy = size * box.height / box.width;
                if (newy == 0) continue;
                resize(symbol, symbol, new Size(size, newy));
                int rest = size - newy;
                int restUp = (int) Math.ceil((double) rest / 2.);
                int restDown = (int) Math.floor((double) rest / 2.);
                Core.vconcat(Arrays.asList(new Mat(restUp, size, CV_8UC1, WHITE_SCALAR), symbol, new Mat(restDown, size, CV_8UC1, WHITE_SCALAR)), symbol);
            }
            symbols.add(new Symbol(box, symbol));
        }
        return symbols;
    }
}
