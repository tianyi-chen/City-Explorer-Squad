package com.example.cityexplorersquad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewJourneyFragmentFragment extends ViewJourneyFragment implements View.OnClickListener {

    private String server_ip;
    private int journey_id;
    private String city;
    private String date;
    private String members;
    private String achievements;
    private String points;

    JSONParser jParser = new JSONParser();

    // url to get journey by id
    private String url_get_journey_details;
    // url to delete journey by id
    private String url_delete_journey;

    // JSON Node names
    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_JOURNEY = "journey";
    private static final String TAG_JOURNEY_ID = "journey_id";
    private static final String TAG_CITY = "city";
    private static final String TAG_DATE = "date";
    private static final String TAG_MEMBERS = "members";
    private static final String TAG_ACHIEVEMENTS = "achievements";
    private static final String TAG_POINTS = "points";
    private static final String LOG_TAG = ViewJourneyFragmentFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialise urls
        server_ip = getArguments().getString(TAG_SERVER_IP);
        journey_id = getArguments().getInt(TAG_JOURNEY_ID);
        url_get_journey_details = "http://" + server_ip + "/IDS/get_journey_by_id.php";
        url_delete_journey = "http://" + server_ip + "/IDS/delete_journey.php";

        new GetJourneyDetails().execute();

        // set onclick listen to delete journey button
        Button delete_journey = (Button) getView().findViewById(R.id.delete_journey);
        delete_journey.setOnClickListener(this);

        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                initiateRefresh();
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_action_menu, menu);
    }

    /**
     * Respond to the user's selection of the Refresh action item.
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Log.i(LOG_TAG, "Refresh item selected");

                // Make sure that the SwipeRefreshLayout is displaying it's refreshing indicator
                if (!isRefreshing()) {
                    setRefreshing(true);
                }

                // Start our refresh background task
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

    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    private void initiateRefresh() {
        Log.i(LOG_TAG, "initiateRefresh");

        new GetJourneyDetails().execute();
    }

    /**
     * Background Async Task to Get complete journey details
     * */
    class GetJourneyDetails extends AsyncTask<String, String, String> {

        /**
         * Getting journey details in background thread
         * */
        protected String doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess((FragmentActivity) getActivity());
            if (connection.isOnline()) {
                // Check for success tag
                int success;
                try {
                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair(TAG_JOURNEY_ID, Integer.toString(journey_id)));

                    JSONObject json = jParser.makeHttpRequest(url_get_journey_details, "GET", params);

                    Log.d("Single Journey Details", json.toString());

                    // json success tag
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        JSONArray journeyObj = json.getJSONArray(TAG_JOURNEY);

                        JSONObject journey = journeyObj.getJSONObject(0);

                        city = journey.getString(TAG_CITY);
                        date = journey.getString(TAG_DATE);
                        members = journey.getString(TAG_MEMBERS);
                        achievements = journey.getString(TAG_ACHIEVEMENTS);
                        points = journey.getString(TAG_POINTS);

                    }else{
                        // no journey with id not found
                        Log.d("debug", "no data found");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // updating UI from Background Thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            ((TextView) getView().findViewById(R.id.city)).setText(city);
                            ((TextView) getView().findViewById(R.id.date)).setText(date);
                            ((TextView) getView().findViewById(R.id.group_members)).setText(members + " group members");
                            ((TextView) getView().findViewById(R.id.achievements)).setText(achievements + " achievements");
                            ((TextView) getView().findViewById(R.id.points)).setText(points + " points");
                        }
                    });
                }

            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        connection.dialog();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String arg) {
            // Stop the refreshing indicator
            setRefreshing(false);

        }
    }

    public void onClick(View view) {
        // delete the journey
        dialog("Do you want to delete this journey?", "Deleted journey cannot be restored.");
    }

    private void dialog(String title, String content) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder.setMessage(content);
        builder.setTitle(title);
        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                new DeleteJourney().execute();
            }
        });
        builder.setPositiveButton("Keep", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setIcon(R.drawable.ic_launcher);
        builder.create().show();
    }

    public void onClickGiftShop() {
        Intent giftShopIntent = new Intent(getActivity(), GiftShopActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(TAG_SERVER_IP, server_ip);
        bundle.putString(TAG_MEMBERS, members);
        bundle.putString(TAG_POINTS, points);
        giftShopIntent.putExtras(bundle);
        startActivity(giftShopIntent);
    }

    /**
     * Background Async Task to Get complete journey details
     * */
    class DeleteJourney extends AsyncTask<String, String, String> {

        /**
         * Deleting journey in background thread
         * */
        protected String doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess((FragmentActivity) getActivity());
            if (connection.isOnline()) {
                int success;
                final String message;
                try {
                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair(TAG_JOURNEY_ID, Integer.toString(journey_id)));

                    JSONObject json = jParser.makeHttpRequest(url_delete_journey, "POST", params);

                    // json success and message tags
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);

                    if (success == 1) {
                        // Successfully deleted the journey
                        if (getActivity() != null) {
                            Intent resultIntent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putInt(TAG_JOURNEY_ID, journey_id);
                            resultIntent.putExtras(bundle);
                            getActivity().setResult(Activity.RESULT_OK, resultIntent);
                            getActivity().finish();
                        }

                    }

                /*
                    Show message on UI thread
                */
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        connection.dialog();
                    }
                });
            }
            return null;
        }
    }
}
