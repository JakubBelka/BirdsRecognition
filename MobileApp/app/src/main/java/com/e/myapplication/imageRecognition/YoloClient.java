package com.e.myapplication.imageRecognition;


import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;

import java.util.List;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class YoloClient {

    static {
        System.loadLibrary("opencv_java3");
    }

    public static boolean yoloInitialize(String cfgPath, String weightsPath){
        try{
            tinyYolo = Dnn.readNetFromDarknet(cfgPath, weightsPath);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public static void yoloInitialize(){
        try{
       //     tinyYolo = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        }
        catch (Exception e){
        }
    }

    public static Net getTinyYolo() {
        return tinyYolo;
    }

    public static List<String> getOutputs() {
        List<String> outputs = new java.util.ArrayList<String>();
        outputs.add(0, "yolo_16");
        outputs.add(1, "yolo_23");

        return  outputs;
    }

    public static String getYoloWeightPath() {
        return yoloWeightPath;
    }

    public static void setYoloWeightPath(String yoloWeightPath) {
        YoloClient.yoloWeightPath = yoloWeightPath;
    }

    public static String getYoloCfgPath() {
        return yoloCfgPath;
    }

    public static void setYoloCfgPath(String yoloCfgPath) {
        YoloClient.yoloCfgPath = yoloCfgPath;
    }


    public static String yoloWeightPath;
    public static String yoloCfgPath;
    public static Net tinyYolo;


}
