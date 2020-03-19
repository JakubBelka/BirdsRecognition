package com.e.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import android.widget.TextView;
import android.widget.Toast;

import com.e.myapplication.imageRecognition.ImageProcessing;
import com.e.myapplication.imageRecognition.YoloClient;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;

public class FileActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView;
    private TextView textView;
    private Button fileButton;


    static {
        System.loadLibrary("opencv_java3");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
        fileButton = (Button) findViewById(R.id.fileBrowserButton);
        fileButton.setOnClickListener(this);

        if (image != null) {
            showImage(image);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == findViewById(R.id.fileBrowserButton)) {
            chooseFile();
        }
    }

    public void chooseFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERM_CODE);
            } else {
                tinyYolo = YoloClient.getTinyYolo();
                pickImageFromGallery();
            }
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_CODE);
    }

    private void yoloRun(Mat mat) throws IOException {
        java.util.List<Mat> result = new java.util.ArrayList<Mat>();
        tinyYolo.setInput(ImageProcessing.getImageBlob(mat));
        tinyYolo.forward(result, YoloClient.getOutputs());

        showImage(ImageProcessing.drawLabels(result, mat));
    }

    private void showImage(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mat, bitmap);
        imageView.setImageBitmap(bitmap);
    }

    private String getPath(Uri uri) {
        if (uri == null) {
            return null;
        } else {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null) {
                int col_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

                return cursor.getString(col_index);
            } else {
                return uri.getPath();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_CODE) {
            objectNum = 0;
            Uri imgUri = data.getData();
            String path = getPath(imgUri);

            Mat rgbImg = ImageProcessing.toRGB2(Imgcodecs.imread(path));
            try {
                yoloRun(rgbImg);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERM_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, getString(R.string.not_have_permission), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private static final int IMAGE_CODE = 1000;
    private static final int PERM_CODE = 1001;
    private Net tinyYolo;
    private Mat image;
    private int objectNum = 0;

}

