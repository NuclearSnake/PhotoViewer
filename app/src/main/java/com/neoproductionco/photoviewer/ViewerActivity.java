package com.neoproductionco.photoviewer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ViewerActivity extends AppCompatActivity {

    final String DEBUG_TAG = "PhotoViewer_Logs";
    int current_page = 1;

    TextView tvPage;
    Button btnPrev;
    Button btnNext;
    GridView gvList;
    GridViewAdapter adapter;

    JSONObject page = null;
    ArrayList<ImageItem> photos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_viewer);
        tvPage = (TextView) findViewById(R.id.tvPage);
        btnPrev = (Button) findViewById(R.id.btnPrev);
        btnNext = (Button) findViewById(R.id.btnNext);
        gvList = (GridView) findViewById(R.id.gvList);
        adapter = new GridViewAdapter(this, R.layout.image_box, photos);
        gvList.setAdapter(adapter);
        gvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent(ViewerActivity.this, FullscreenActivity.class);
                intent.putExtra("selected", position);
                intent.putExtra("photos", photos);
                startActivity(intent);
            }
        });

        reloadPage();

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(current_page <= 1)
                    return;
                current_page--;
                reloadPage();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(current_page >= page.getInt("total_pages")-1)
                        return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                current_page++;
                reloadPage();
            }
        });
    }

    private void reloadPage(){
        tvPage.setText("Page "+current_page);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadPage().execute(current_page);
        } else {
            Toast.makeText(ViewerActivity.this, "No network connection available.", Toast.LENGTH_LONG).show();
        }
    }

    // Loads the current page's JSON representation: information about the photos and page
    // Works in asynchronous way so won't reflect the UI
    private class DownloadPage extends AsyncTask<Integer, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Integer... page_num) {
            // params comes from the execute() call: params[0] is the number of the page.
            try {
                return loadPage(page_num[0]);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        // when the info was downloaded, start using it to fill the photos array
        @Override
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                page = result;
            }

            new DownloadImages().execute();
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private JSONObject loadPage(int page_number) throws IOException, JSONException {
            InputStream is = null;

            try {
                URL url = new URL("https://api.500px.com/v1/photos?feature=popular&consumer_key=" +
                        "wB4ozJxTijCwNuggJvPGtBGCRqaZVcF6jsrzUadF&page=" + page_number);
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

                // Convert the InputStream into a JSON representation of the selected page
                return new JSONObject(readStreamToString(is));

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    // Loads the photos array with the first 100 of photos from the page
    // Works in asynchronous way so won't reflect the UI
    private class DownloadImages extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            photos.clear();
            ImageItem ii;
            int total = 0;
            try {
                 total = page.getJSONArray("photos").length();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for(int i = 0; i < total; i++) {
                ii = new ImageItem();
                try {
                    ii.setImage(downloadBitmap(i));
                    ii.setTitle(getImageData(page, i).getString("name"));
                    ii.setAuthor(getImageData(page, i).getJSONObject("user").getString("fullname"));
                    ii.setCamera(getImageData(page, i).getString("camera"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                photos.add(ii);
            }

            return null;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            gvList.invalidateViews();
        }

        Bitmap downloadBitmap(int image_num) {
            URL url;
            HttpURLConnection urlConnection = null;
            Bitmap b = null;
            try {
                url = new URL(getImageData(page, image_num).getString("image_url"));
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

        public JSONObject getImageData(JSONObject current_page, int number) throws JSONException {
            JSONArray photos = current_page.getJSONArray("photos");
            if(number >= 0 && number<photos.length())
                return photos.getJSONObject(number);

            return null;
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

}
