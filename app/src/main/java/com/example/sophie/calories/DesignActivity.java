package com.example.sophie.calories;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;

public class DesignActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design);

        final ImageButton buttonbbb = findViewById(R.id.designBackButton);
        buttonbbb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        final Button testButton = findViewById(R.id.designTestButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectNutrionix(getUserInput());
            }
        });


        findViewById(R.id.showResultScrollView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            }
        });
    }

    private String getUserInput() {
        final EditText input = findViewById(R.id.userInput);
        String userInput = input.getText().toString();
        final String correctFormatInput = userInput.replaceAll(",// "," and ");
        return correctFormatInput;
    }

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
                        Toast.makeText(DesignActivity.this, "Sorry, We couldn't match any of your foods",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void finishCalculate(final JSONObject jsonResult) {
        StringBuilder foodName = new StringBuilder();
        StringBuilder foodCal = new StringBuilder();
        Integer totalCal = 0;

        try {
            JSONArray foods = jsonResult.getJSONArray("foods");
            int i = 0;
            while (!foods.isNull(i)) {

                JSONObject eachFood = foods.getJSONObject(i);
                foodName.append(eachFood.getString("food_name") + "\n");
                foodCal.append(eachFood.getString("nf_calories")+ " Cal"+ '\n');
                totalCal += eachFood.getInt("nf_calories");
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView calSum = findViewById(R.id.textTotalResult);
        calSum.setText(totalCal.toString() + " Cal");

        TextView listComponent = findViewById(R.id.textView3);
        listComponent.setText(foodName.toString());

        TextView listCal = findViewById(R.id.textView4);
        listCal.setText(foodCal.toString());
    }
}
