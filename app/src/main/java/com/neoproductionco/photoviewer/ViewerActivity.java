package com.neoproductionco.photoviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ViewerActivity extends AppCompatActivity {

    final String DEBUG_TAG = "PhotoViewer_Logs";
    final String page1 = "https://api.500px.com/v1/photos?feature=popular&consumer_key=wB4ozJxTijCwNuggJvPGtBGCRqaZVcF6jsrzUadF&page=2";
    int current_photo = 0;
    TextView tvText;
    Button btnDownload;
    ImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        tvText = (TextView) findViewById(R.id.tvText);
        ivImage = (ImageView) findViewById(R.id.ivImage);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Click(); // TODO A bit strange construction
            }
        });
    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void Click() {
        // Gets the URL from the constant link
        // TODO redundant work with links
        // TODO make the link changeable and really change it from click to click
        // TODO invent the Array for the downloaded images and information. New Class: Bitmap + Info? Look for ready-to-work one.
        String stringUrl = page1;
        current_photo++;
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            tvText.setText("No network connection available.");
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, Bitmap> {
        String page;

        @Override
        protected Bitmap doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                page = downloadUrl(urls[0]);
            } catch (IOException e) {
                return null;
            }

            if(page == null)
                return null;

            try {
                return downloadBitmap(getImageData(page, current_photo).getString("image_url"));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Bitmap result) {
            if(result == null) {
                tvText.setText("Unable to retrieve web page. URL may be invalid.");
                return;
            } else {
                try {
                    tvText.setText((getImageData(page, current_photo).getString("name")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            ivImage.setImageBitmap(result);
        }

        public JSONObject getImageData(String response, int number) throws JSONException {
            JSONObject current_page = new JSONObject(response);
            JSONArray photos = current_page.getJSONArray("photos");
            if(number >= 0 && number<photos.length())
                return photos.getJSONObject(number);

            return null;
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string

            return readStreamToString(is);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readStreamToString(InputStream stream) throws IOException {
        if(stream == null)
            return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        return reader.readLine();
    }

    // Reads an InputStream and converts it to a Bitmap.
    public Bitmap readStreamToBitmap(InputStream stream) throws IOException {
        return BitmapFactory.decodeStream(stream);
    }

    Bitmap downloadBitmap(String surl) {
        URL url;
        HttpURLConnection urlConnection = null;
        Bitmap b = null;
        try {
            url = new URL(surl);
            urlConnection = (HttpURLConnection) url.openConnection();
            b = BitmapFactory.decodeStream(urlConnection.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return b;
    }
}
