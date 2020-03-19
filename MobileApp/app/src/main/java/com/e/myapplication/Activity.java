package com.e.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.content.SharedPreferences;
import android.app.Dialog;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.e.myapplication.imageRecognition.YoloClient;

public class Activity extends AppCompatActivity implements View.OnClickListener {
    private Button cameraButton;
    private Button fileButton;
    private Button settingsButton;
    private final int writeExternalStorageCODE = 100;
    private final int cameraCODE = 101;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(this);
        fileButton = (Button) findViewById(R.id.fileButton);
        fileButton.setOnClickListener(this);
        settingsButton = (Button) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(this);

        fileButton.setEnabled(false);
        cameraButton.setEnabled(false);

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences), 0);

        checkWriteExternalStoragePerrmission();
    }

    private void yoloSetup() {
        YoloClient.yoloInitialize();
        String confPath = sharedPreferences.getString("confPath", "");
        String weightsPath = sharedPreferences.getString("weightsPath", "");

        if (YoloClient.getTinyYolo() == null) {
            try {
                if (YoloClient.yoloInitialize(confPath, weightsPath)) {
                    cameraButton.setEnabled(true);
                    fileButton.setEnabled(true);
                }
                else {
                    showDialog();
                }
            } catch (Exception e) {
                showDialog();
            }
        }  else {
            cameraButton.setEnabled(true);
            fileButton.setEnabled(true);
        }
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout);

        Button downloadButton = (Button) dialog.findViewById(R.id.download_button);
        Button pathButton = (Button) dialog.findViewById(R.id.path_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        pathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Activity.this, ModelSettingActivity.class);
                startActivityForResult(i, 103);
                dialog.dismiss();
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void checkWriteExternalStoragePerrmission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, writeExternalStorageCODE);
            } else {
                yoloSetup();
            }
        } else {
            yoloSetup();
        }
    }


    @Override
    public void onClick(View v) {
        if (v == findViewById(R.id.cameraButton)) {
            Intent intent = new Intent(Activity.this, CameraActivity.class);

            if (ContextCompat.checkSelfPermission(Activity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Activity.this, new String[]{Manifest.permission.CAMERA}, cameraCODE);
            } else {
                startActivity(intent);
            }
        }
        if (v == findViewById(R.id.fileButton)) {
            Intent intent = new Intent(Activity.this, FileActivity.class);
            startActivity(intent);
        }
        if (v == findViewById(R.id.settingsButton)) {
            Intent intent = new Intent(Activity.this, ModelSettingActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 103 && resultCode == RESULT_OK) {
            cameraButton.setEnabled(true);
            fileButton.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case cameraCODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Activity.this, CameraActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Brak pozwolenia, zmień ustawienia aplikacji", Toast.LENGTH_LONG).show();
                }
            }
            case writeExternalStorageCODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    yoloSetup();
                } else {
                    Toast.makeText(this, "Brak pozwolenia, zmień ustawienia aplikacji", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
