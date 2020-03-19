package com.e.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.e.myapplication.imageRecognition.YoloClient;

public class ModelSettingActivity extends AppCompatActivity implements View.OnClickListener {
    private Button modePathButton;
    private Button cfgPathButton;
    private Button returnButton;
    private Button okButton;
    private EditText modelPath;
    private EditText cfgPath;
    private String weightsPath;
    private String confPath;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefEdit;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_setting);

        modePathButton = (Button) findViewById(R.id.model_button);
        cfgPathButton = (Button) findViewById(R.id.cfg_button);
        returnButton = (Button) findViewById(R.id.return_button);
        okButton = (Button) findViewById(R.id.ok_button);
        modelPath = (EditText) findViewById(R.id.model_path);
        cfgPath = (EditText) findViewById(R.id.cfg_path);

        modePathButton.setOnClickListener(this);
        cfgPathButton.setOnClickListener(this);
        returnButton.setOnClickListener(this);
        okButton.setOnClickListener(this);

        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preferences), 0);
        prefEdit = sharedPreferences.edit();
    }

    private void findModelPath(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 301) {
            modelPath.setText(getPath(data.getData()));
            weightsPath = getPath(data.getData());

        } else if (resultCode == RESULT_OK && requestCode == 302) {
            cfgPath.setText(getPath(data.getData()));
            confPath = getPath(data.getData());
        }
    }

    @Override
    public void onClick(View v) {
        if (v == findViewById(R.id.ok_button)) {
            if (weightsPath!=null && confPath!=null) {
                try {
                    if (YoloClient.yoloInitialize(confPath, weightsPath)){
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK,returnIntent);
                        prefEdit.putString("confPath", confPath);
                        prefEdit.putString("weightsPath", weightsPath);
                        prefEdit.commit();
                        finish();
                    }
                }
                catch (Exception e){
                    System.out.println(R.string.error);
                }

            } else {
                Toast.makeText(this, getString(R.string.path_error), Toast.LENGTH_SHORT).show();
            }
        }
        if (v == findViewById(R.id.return_button)) {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED,returnIntent);
            finish();
        }
        if (v == findViewById(R.id.model_button)) {
            findModelPath(301);
        }
        if (v == findViewById(R.id.cfg_button)) {
            findModelPath(302);
        }
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
}
