package com.example.cityexplorersquad;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

public class ViewPhotoListFragmentFragment extends ViewPhotoListFragment {

    private String server_ip;
    private int journey_id;
    private String city;
    private String date;
    private ListView cardsList;
    private PhotoCardsAdapter cardsAdapter;

    ArrayList<Bitmap> photos;

    JSONParser jParser = new JSONParser();

    private ImageView imageView;
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
    private static final String LOG_TAG = ViewJourneyListFragmentFragment.class.getSimpleName();

    private static final int CANCELLED = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cardsList = (ListView) getView().findViewById(R.id.cards_list);

        // initialise urls
        server_ip = getArguments().getString(TAG_SERVER_IP);
        journey_id = getArguments().getInt(TAG_JOURNEY_ID);
        city = getArguments().getString(TAG_CITY);
        date = getArguments().getString(TAG_DATE);
        url_get_photos = "http://" + server_ip + "/IDS/get_photos.php";
        downloadImageUri = "http://" + server_ip + "/IDS/";

        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                initiateRefresh();
            }
        });

        uris = new ArrayList<>();

        new DownloadPhotos().execute();

    }

//    @Override
//    public void onResume() {
//        super.onResume();
//
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_action_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Log.i(LOG_TAG, "Refresh item selected");

                // Make sure that the SwipeRefreshLayout is displaying it's refreshing indicator
                if (!isRefreshing()) {
                    setRefreshing(true);
                }

                initiateRefresh();
                return true;
            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_message).setTitle(R.string.app_name);
                builder.setPositiveButton(R.string.dialog_ok, null);
                builder.setIcon(R.drawable.ic_launcher);

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initiateRefresh() {
        Log.i(LOG_TAG, "initiateRefresh");

        new DownloadPhotos().execute();
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
            loading = ProgressDialog.show(getActivity(), "Downloading photo", "Please wait...", true, true);
        }

        @Override
        protected void onPostExecute(final Integer success) {
            super.onPostExecute(success);
            loading.dismiss();
            setRefreshing(false);
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (success == 1) {
                            cardsAdapter = new PhotoCardsAdapter(getActivity(), photos, uris, new ListItemButtonClickListener());
                            cardsList.setAdapter(cardsAdapter);
                        }
                    }
                });
            }

        }

        @Override
        protected void onProgressUpdate(Integer... errorCode) {
            switch (errorCode[0]) {
                case CANCELLED:
                    loading.dismiss();
                    Toast.makeText(getActivity(), "Task cancelled", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.onProgressUpdate(errorCode);
        }

        protected Integer doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess((FragmentActivity) getActivity());
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
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
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
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        connection.dialog();
                    }
                });
            }
            return -1;
        }

    }

    private final class ListItemButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = cardsList.getFirstVisiblePosition(); i <= cardsList.getLastVisiblePosition(); i++) {
                if (v == cardsList.getChildAt(i - cardsList.getFirstVisiblePosition()).findViewById(R.id.imageView)) {
                    Intent intent = new Intent(getActivity(), ShareToFacebookActivity.class);
                    Bundle bundle = new Bundle();
                    String uri = cardsAdapter.getItem(i);
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
