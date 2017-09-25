package com.example.cityexplorersquad;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ViewPhotoGridActivity extends AppCompatActivity {

    private String server_ip;
    private int journey_id;
    private String city;
    private String date;
    private GridView cardsGrid;
    private PhotoGridAdapter cardsAdapter;

    ArrayList<Bitmap> photos;

    JSONParser jParser = new JSONParser();

    private Bitmap bitmap;
    private String downloadImageUri;
    private ArrayList<String> uris;

    // url to get all photos of the journey
    private String url_get_photos;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_JOURNEY_ID = "journey_id";
    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_CITY = "city";
    private static final String TAG_DATE = "date";
    private static final String TAG_PATHS = "paths";
    private static final String TAG_IMAGE_PATH = "image_path";

    private static final int CANCELLED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo_grid);

        cardsGrid = (GridView) findViewById(R.id.cards_grid);

        // initialise urls
        Bundle bundle = getIntent().getExtras();
        server_ip = bundle.getString(TAG_SERVER_IP);
        journey_id = bundle.getInt(TAG_JOURNEY_ID);
        city = bundle.getString(TAG_CITY);
        date = bundle.getString(TAG_DATE);
        url_get_photos = "http://" + server_ip + "/IDS/get_photos.php";
        downloadImageUri = "http://" + server_ip + "/IDS/";

        uris = new ArrayList<>();

        new DownloadPhotos().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.dialog_message).setTitle(R.string.app_name);
                builder.setPositiveButton(R.string.dialog_ok, null);
                builder.setIcon(R.drawable.ic_launcher);

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
       Background Async Task to Load all photos of a journey by making HTTP Request
    */
    private class DownloadPhotos extends AsyncTask<String, Integer, Integer> {

        ProgressDialog loading;
        private volatile boolean running = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(ViewPhotoGridActivity.this, "Downloading photo", "Please wait...", true, true);
        }

        @Override
        protected void onPostExecute(final Integer success) {
            super.onPostExecute(success);
            loading.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        cardsAdapter = new PhotoGridAdapter(ViewPhotoGridActivity.this, photos, uris, new GridItemButtonClickListener());
                        cardsGrid.setAdapter(cardsAdapter);
                        Toast.makeText(getApplicationContext(), "Tap photo to share to Facebook!", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }

        @Override
        protected void onProgressUpdate(Integer... errorCode) {
            switch (errorCode[0]) {
                case CANCELLED:
                    loading.dismiss();
                    Toast.makeText(ViewPhotoGridActivity.this, "Task cancelled", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.onProgressUpdate(errorCode);
        }

        protected Integer doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess(ViewPhotoGridActivity.this);
            if (connection.isOnline()) {
                // Check for success tag
                int success;
                final String message;
                try {
                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair(TAG_JOURNEY_ID, Integer.toString(journey_id)));

                    JSONObject json = jParser.makeHttpRequest(url_get_photos, "GET", params);

                    Log.d("All images:", json.toString());

                    // json success tag
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        photos = new ArrayList<>();
                        uris = new ArrayList<>();

                        JSONArray paths = json.getJSONArray(TAG_PATHS);

                        for (int i = 0; i < paths.length(); i++) {
                            JSONObject image_path = paths.getJSONObject(i);
                            uris.add(downloadImageUri + image_path.getString(TAG_IMAGE_PATH));
                        }
                        for (int i = 0; i < uris.size(); i++) {
                            URL url = new URL(uris.get(i));
                            Log.d("url", url.toString());
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setDoInput(true);
                            conn.connect();
                            InputStream is = conn.getInputStream();
                            bitmap = BitmapFactory.decodeStream(is);
                            photos.add(bitmap);
                        }

                    } else {
                        // no photo found
                        message = json.getString(TAG_MESSAGE);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(ViewPhotoGridActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    return success;

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        connection.dialog();
                    }
                });
            }
            return -1;
        }

    }

    private final class GridItemButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = cardsGrid.getFirstVisiblePosition(); i <= cardsGrid.getLastVisiblePosition(); i++) {
                if (v == cardsGrid.getChildAt(i - cardsGrid.getFirstVisiblePosition()).findViewById(R.id.imageButton)) {

                    Intent intent = new Intent(ViewPhotoGridActivity.this, ShareToFacebookActivity.class);
                    Bundle bundle = new Bundle();
                    String uri = cardsAdapter.getPhotoUri(i);
                    bundle.putString(TAG_IMAGE_PATH, uri);
                    bundle.putString(TAG_CITY, city);
                    bundle.putString(TAG_DATE, date);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
            }
        }
    }

}
