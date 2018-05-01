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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;
import com.loopj.android.http.*;

public class TakePhotoActivity extends AppCompatActivity {

    private final String TAG = "Calorie";

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
        JsonParser jsonParser = new JsonParser();
        JsonObject result = jsonParser.parse(jsonResult).getAsJsonObject();
        JsonObject description = result.get("description").getAsJsonObject();
        JsonArray array = description.get("tags").getAsJsonArray();
        String components = array.toString();

        String newComp = components.replaceAll(","," and ");
        Log.d(TAG,"PROCCESSED IMAGE");
        connectNutrionix(newComp);
    }



    // call Nutritionix API
    private void connectNutrionix(String foodName) {
        StringEntity stringEntity = null;
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("x-app-id", "6c47ef6f");
        client.addHeader("x-app-key", "60177784d08e12841eaed10cb6bd0d06");
        client.addHeader("x-remote-user-id", "0");
        JSONObject jsonObject = new JSONObject();


        try {
            jsonObject.put("query", foodName);

        } catch (JSONException e) {
            Log.d("Healthier", e.toString());
        }
        try {
            stringEntity = new StringEntity(jsonObject.toString());
        } catch (UnsupportedEncodingException e) {
            Log.d("Healthier", e.toString());
        }

        client.post(this, "https://trackapi.nutritionix.com/v2/natural/nutrients", stringEntity, "application/json",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                        Log.d("Healthier", "Success" + response.toString());
                        finishCalculate(response);
                        //data = NutritionData.fromJson(response);
                    }

                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d("Healthier", "failed" + errorResponse.toString());
                        Toast.makeText(TakePhotoActivity.this, "Sorry, We couldn't match any of your foods",
                                Toast.LENGTH_SHORT).show();


                    }
                });
    }

    protected void finishCalculate(final JSONObject jsonResult) {
        StringBuilder foodName = new StringBuilder();
        StringBuilder foodCal = new StringBuilder();
        Integer totalCal = 0;

        try {
            JSONArray foods = jsonResult.getJSONArray("foods");
            int i = 0;
            while (!foods.isNull(i)) {
                Log.d(TAG,foods.getJSONObject(i).toString());

                JSONObject eachFood = foods.getJSONObject(i);
                foodName.append(eachFood.getString("food_name") + "\n");
                foodCal.append(eachFood.getString("nf_calories")+ " Cal"+ '\n');
                totalCal += eachFood.getInt("nf_calories");
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView calSum = findViewById(R.id.totalCal);
        calSum.setText(totalCal.toString());

        TextView listComponent = findViewById(R.id.showComponent);
        listComponent.setText(foodName.toString());

        TextView listCal = findViewById(R.id.showCal);
        listCal.setText(foodCal.toString());
    }




}
