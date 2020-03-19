package com.e.myapplication.imageRecognition;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageProcessing {
    public static Mat getImageBlob(Mat mat) {
        Mat imageBlob = Dnn.blobFromImage(mat, 0.00392, new Size(416, 416), new Scalar(0, 0, 0),/*swapRB*/false, /*crop*/false);
        return imageBlob;
    }

    public static Mat toRGB(Mat frame){
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
        return frame;
    }

    public static Mat toRGB2(Mat frame){
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);
        return frame;
    }


    public static Mat drawLabels(List<Mat> result, Mat frame) {

        float confThreshold = 0.4f;
        List<Integer> clsIds = new ArrayList<>();
        List<Float> confs = new ArrayList<>();
        List<Rect> rects = new ArrayList<>();

        for (int i = 0; i < result.size(); ++i) {
            Mat oneResultObject = result.get(i);

            for (int j = 0; j < oneResultObject.rows(); ++j) {
                Mat row = oneResultObject.row(j);
                Mat scores = row.colRange(5, oneResultObject.cols());
                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);

                float confidence = (float) mm.maxVal;
                Point classIdPoint = mm.maxLoc;

                if (confidence > confThreshold) {
                    int centerX = (int) (row.get(0, 0)[0] * frame.cols());
                    int centerY = (int) (row.get(0, 1)[0] * frame.rows());
                    int width = (int) (row.get(0, 2)[0] * frame.cols());
                    int height = (int) (row.get(0, 3)[0] * frame.rows());
                    int left = centerX - width / 2;
                    int top = centerY - height / 2;

                    clsIds.add((int) classIdPoint.x);
                    confs.add((float) confidence);
                    rects.add(new Rect(left, top, width, height));
                }
            }
        }

        if (confs.size() >= 1) {
            float nmsThresh = 0.4f;
            MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));

            Rect[] boxesArray = rects.toArray(new Rect[0]);
            MatOfRect boxes = new MatOfRect(boxesArray);
            MatOfInt indices = new MatOfInt();

            Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices);

            int[] ind = indices.toArray();
            for (int i = 0; i < ind.length; ++i) {
                int idx = ind[i];
                Rect box = boxesArray[idx];
                int idGuy = clsIds.get(idx);
                float conf = confs.get(idx);
                List<String> names = Arrays.asList("wrobel", "wrona", "kaczka", "labadz", "sroka", "golab", "sojka", "szpak", "kos", "mewa");
                int intConf = (int) (conf * 100);
                Imgproc.putText(frame, names.get(idGuy) + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(179, 59, 0), 2);
                Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(179, 59, 00), 5);
            }
        }
        return frame;
    }


}
