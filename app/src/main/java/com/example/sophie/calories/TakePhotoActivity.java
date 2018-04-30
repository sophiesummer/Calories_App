package com.example.sophie.calories;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class TakePhotoActivity extends AppCompatActivity {

    private static RequestQueue requestQueue;

    ImageView imageView;

    public static final int RequestPermissionCode = 1;

    private static int IMG_RESULT = 2;

    private Bitmap currentBitMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestQueue = Volley.newRequestQueue(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        final ImageButton goback = findViewById(R.id.backButton);
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final ImageButton takePhoto = findViewById(R.id.photoAgain);

        imageView = findViewById(R.id.showPhoto);

        EnableRuntimePermission();

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 7);
            }
        });

        final ImageButton gallery = findViewById(R.id.gallerySearch);

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMG_RESULT);
            }
        });

        final Button test = findViewById(R.id.test);

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAPICall();
            }
        });


    }

    /** Take a photo by camera, and send the result to imageView. */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 7 && resultCode == RESULT_OK) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            currentBitMap = bitmap;
            imageView.setImageBitmap(bitmap);
        }

        try {
            if (requestCode == IMG_RESULT && resultCode == RESULT_OK && data != null) {
                Uri URI = data.getData();
                String[] FILE = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(URI, FILE, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(FILE[0]);
                String ImageDecode = cursor.getString(columnIndex);
                cursor.close();

                currentBitMap = BitmapFactory.decodeFile(ImageDecode);
                imageView.setImageBitmap(currentBitMap);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Please try again", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void EnableRuntimePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(TakePhotoActivity.this,
                Manifest.permission.CAMERA))
        {
            Toast.makeText(TakePhotoActivity.this,"CAMERA permission allows us to Access CAMERA app",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(TakePhotoActivity.this,new String[]{
                    Manifest.permission.CAMERA}, RequestPermissionCode);
        }
    }

    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {
        switch (RC) {
            case RequestPermissionCode:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(TakePhotoActivity.this,"Let's take a picture!  " +
                            ":)", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TakePhotoActivity.this,"Let's take a picture!  :)"
                            , Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    /**
     * processing the image.
     * firstly use recognize photo api, then use nutrition calculate api
     */
    private void startAPICall() {
        if (currentBitMap == null) {
            TextView textView = findViewById(R.id.showCal);
            textView.setText("Please take a photo / select a photo");
            return;
        }
        new ProcessPhoto.ProcessImageTask(TakePhotoActivity.this, requestQueue).execute(currentBitMap);
    }

    protected void finishProcessImage(final String jsonResult) {
        TextView textView = findViewById(R.id.showCal);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(jsonResult);
        String jsonString = gson.toJson(jsonElement);
        textView.setText(jsonString);
    }

}
