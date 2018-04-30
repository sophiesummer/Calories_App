package com.example.sophie.calories;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class ProcessPhoto {
    private static final int DEFAULT_COMPRESSION_QUALITY_LEVEL = 100;

    static class ProcessImageTask extends AsyncTask<Bitmap, Integer, Integer> {

        /** Url for the MS cognitive services API. */
        private static final String MS_CV_API_URL =
                "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/analyze";

        /** Default visual features to request. You may need to change this value. */
        private static final String MS_CV_API_DEFAULT_VISUAL_FEATURES = "Description";

        /** Default visual features to request. */
        private static final String MS_CV_API_DEFAULT_LANGUAGE = "en";

        /** Default visual features to request. You may need to change this value. */
        private static final String MS_CV_API_DEFAULT_DETAILS = "Landmarks";

        /** Subscription key. */
        private static final String SUBSCRIPTION_KEY = "2cac5177722246bf9c827288b2e1e6e6";

        /** Reference to the calling activity so that we can return results. */
        private WeakReference<TakePhotoActivity> activityReference;

        /** Request queue to use for our API call. */
        private RequestQueue requestQueue;

        /**
         * Create a new talk to upload data and return the API results.
         *
         * We pass in a reference to the app so that this task can be static.
         * Otherwise we get warnings about leaking the context.
         *
         * @param context calling activity context
         * @param setRequestQueue Volley request queue to use for the API request
         */
        ProcessImageTask(final TakePhotoActivity context, final RequestQueue setRequestQueue) {
            activityReference = new WeakReference<>(context);
            requestQueue = setRequestQueue;
        }
        /**
         * Convert an image to a byte array, upload to the Microsoft Cognitive Services API,
         * and return a result.
         *
         * @param currentBitmap the bitmap to process
         * @return unused unused result
         */
        protected Integer doInBackground(final Bitmap... currentBitmap) {
            /*
             * Convert the image from a Bitmap to a byte array for upload.
             */
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            currentBitmap[0].compress(Bitmap.CompressFormat.PNG,
                    DEFAULT_COMPRESSION_QUALITY_LEVEL, stream);

            // Prepare our API request
            String requestURL = Uri.parse(MS_CV_API_URL)
                    .buildUpon()
                    .appendQueryParameter("visualFeatures", MS_CV_API_DEFAULT_VISUAL_FEATURES)
                    .appendQueryParameter("details", MS_CV_API_DEFAULT_DETAILS)
                    .appendQueryParameter("language", MS_CV_API_DEFAULT_LANGUAGE)
                    .build()
                    .toString();


            /*
             * Make the API request.
             */
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST, requestURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            // On success, clear the progress bar and call finishProcessImage

                            TakePhotoActivity activity = activityReference.get();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }

                            activity.finishProcessImage(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(final VolleyError error) {

                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null &&
                            networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {

                    }
                    TakePhotoActivity activity = activityReference.get();
                    if (activity == null || activity.isFinishing()) {
                        return;
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    // Set up headers properly
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/octet-stream");
                    headers.put("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY);
                    return headers;
                }
                @Override
                public String getBodyContentType() {
                    // Set the body content type properly for a binary upload
                    return "application/octet-stream";
                }
                @Override
                public byte[] getBody() {
                    return stream.toByteArray();
                }
            };
            requestQueue.add(stringRequest);

            /* doInBackground can't return void, otherwise we would. */
            return 0;
        }
    }

}
