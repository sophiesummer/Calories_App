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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class TakePhotoActivity extends AppCompatActivity {

    ImageView imageView;

    public static final int RequestPermissionCode = 1;

    private static int IMG_RESULT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // saving images to gallery
        final ImageButton saving = findViewById(R.id.gallerySaving);

        saving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
            }
        });

    }

    /** Take a photo by camera, and send the result to imageView. */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 7 && resultCode == RESULT_OK) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
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

                imageView.setImageBitmap(BitmapFactory.decodeFile(ImageDecode));
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




}
