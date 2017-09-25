package com.example.cityexplorersquad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewJourneyListFragmentFragment extends ViewJourneyListFragment implements View.OnClickListener {

    public String server_ip;
    private String user_name;
    private ListView cardsList;
    private JourneyCardsAdapter cardsAdapter;

    ArrayList<Integer> ids;
    ArrayList<String> cities;
    ArrayList<String> dates;

    JSONParser jParser = new JSONParser();

    // url to get all journeys list
    private String url_all_journeys;
    // url to create a new journey
    private String url_add_journey;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_JOURNEYS = "journeys";
    private static final String TAG_JOURNEY_ID = "journey_id";
    private static final String TAG_CITY = "city";
    private static final String TAG_DATE = "date";
    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_USERNAME = "user_name";
    private static final String LOG_TAG = ViewJourneyListFragmentFragment.class.getSimpleName();

    private static final int VIEW_JOURNEY_REQUEST_CODE = 1;

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
        user_name = getArguments().getString(TAG_USERNAME);
        url_all_journeys = "http://" + server_ip + "/IDS/get_all_journeys.php";
        url_add_journey = "http://" + server_ip + "/IDS/add_new_journey.php";

        // retrieve and display all journeys and set onclick listeners
        new GetAllJourneys().execute();
        cardsList.setOnItemClickListener(new ListItemClickListener());

        // set onclick listen to buttons
        Button add_journey = (Button) getView().findViewById(R.id.add_journey);
        add_journey.setOnClickListener(this);

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

    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    private void initiateRefresh() {
        Log.i(LOG_TAG, "initiateRefresh");

        new GetAllJourneys().execute();
    }

    /*
        Background Async Task to Load all journeys by making HTTP Request
     */
    private class GetAllJourneys extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess(getActivity());
            if (connection.isOnline()) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair(TAG_USERNAME, user_name));
                // getting JSON string from URL
                JSONObject json = jParser.makeHttpRequest(url_all_journeys, "GET", params);

                Log.d("All Journeys: ", json.toString());

                int success;
                final String message;
                try {
                    // Checking for SUCCESS TAG
                    success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        JSONArray journeys = json.getJSONArray(TAG_JOURNEYS);

                        ids = new ArrayList<>();
                        cities = new ArrayList<>();
                        dates = new ArrayList<>();

                        // looping through All journeys
                        for (int i = 0; i < journeys.length(); i++) {
                            JSONObject c = journeys.getJSONObject(i);

                            // Storing each json item in variable
                            int id = c.getInt(TAG_JOURNEY_ID);
                            String city = c.getString(TAG_CITY);
                            String date = c.getString(TAG_DATE);

                            ids.add(id);
                            cities.add(city);
                            dates.add(date);

                        }
                    } else {
                        // no journey found
                        message = json.getString(TAG_MESSAGE);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;

            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                       connection.dialog();
                    }
                });
                return null;
            }

        }

        @Override
        protected void onPostExecute(String arg) {
            // Stop the refreshing indicator
            setRefreshing(false);
            // updating UI from Background Thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        if (ids != null && ids.size() > 0) {
                            cardsAdapter = new JourneyCardsAdapter(getActivity(), ids, cities, dates, new ListItemButtonClickListener());
                            cardsList.setAdapter(cardsAdapter);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        // add new journey
        try {
            int journey_id = new AddNewJourney().execute().get();
            Intent newJourneyIntent = new Intent(getActivity(), AddJourneyActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(TAG_SERVER_IP, server_ip);
            bundle.putInt(TAG_JOURNEY_ID, journey_id);
            bundle.putString(TAG_USERNAME, user_name);
            newJourneyIntent.putExtras(bundle);
            startActivity(newJourneyIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
        Bcakground AsyncTask to add a new journey by making HTTP request
     */
    class AddNewJourney extends AsyncTask<String, String, Integer> {

        protected Integer doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess(getActivity());
            if (connection.isOnline()) {
                // Check for success tag
                int success;
                final String message;
                int journey_id = -1;
                try {
                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair(TAG_USERNAME, user_name));

                    JSONObject json = jParser.makeHttpRequest(url_add_journey, "POST", params);

                    Log.d("Response", json.toString());

                    // json success tag
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                    if (success == 1) {
                        journey_id = json.getInt(TAG_JOURNEY_ID);

                    }else{
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return journey_id;
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        connection.dialog();
                    }
                });
                return -1;
            }

        }

    }

    private final class ListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // start ViewJourneyActivity to view item i
            viewJourney(position);
        }
    }

    /*******************************************************************************************
     * * Reused work:
     *    Title: Creating a Cards UI on Android
     *    Author: Yasin Yildirim
     *    Date: 03/06/2014
     *    Code version: N/A
     *    Availability: https://github.com/vudin/android-cards-ui-example
     ********************************************************************************************/
    private final class ListItemButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = cardsList.getFirstVisiblePosition(); i <= cardsList.getLastVisiblePosition(); i++) {
                if (v == cardsList.getChildAt(i - cardsList.getFirstVisiblePosition()).findViewById(R.id.list_item_card_button_1)) {
//                    // start ViewJourneyActivity to view item i
//                    viewJourney(i);
                    // to view photos
//                    Intent albumIntent = new Intent(getActivity(), ViewPhotosActivity.class);
                    Intent albumIntent = new Intent(getActivity(), ViewPhotoGridActivity.class);
                    view(i, albumIntent);

                } else if (v == cardsList.getChildAt(i - cardsList.getFirstVisiblePosition()).findViewById(R.id.list_item_card_button_2)) {
                    // to view members
                    Intent memberIntent = new Intent(getActivity(), ViewMemberListActivity.class);
                    view(i, memberIntent);

                } else if (v == cardsList.getChildAt(i - cardsList.getFirstVisiblePosition()).findViewById(R.id.list_item_card_button_3)) {
                    // to view the tasks
                    Intent taskIntent = new Intent(getActivity(), ViewTaskListActivity.class);
                    view(i, taskIntent);

                }
            }
        }
    }

    private void viewJourney(int position) {
        int journey_id = cardsAdapter.getItem(position);

        Intent intent = new Intent(getActivity(), ViewJourneyActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(TAG_SERVER_IP, server_ip);
        bundle.putInt(TAG_JOURNEY_ID, journey_id);
        intent.putExtras(bundle);
        startActivityForResult(intent, VIEW_JOURNEY_REQUEST_CODE);
    }

    private void view(int position, Intent intent) {
        int journey_id = cardsAdapter.getItem(position);
        String city = cardsAdapter.getCity(position);
        String date = cardsAdapter.getDate(position);

        Bundle bundle = new Bundle();
        bundle.putString(TAG_SERVER_IP, server_ip);
        bundle.putInt(TAG_JOURNEY_ID, journey_id);
        bundle.putString(TAG_CITY, city);
        bundle.putString(TAG_DATE, date);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (VIEW_JOURNEY_REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    int journey_id = data.getExtras().getInt(TAG_JOURNEY_ID);
                    cardsAdapter.removeItem(journey_id);
                    cardsAdapter.notifyDataSetChanged();
                }
                break;
            }
        }
    }

    public void onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            dialog("Do you want to leave this app?", "You will have to log in again next time.");
        }
    }

    private void dialog(String title, String content) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder.setMessage(content);
        builder.setTitle(title);
        builder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().finish();
            }
        });
        builder.setPositiveButton("Stay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setIcon(R.drawable.ic_launcher);
        builder.create().show();
    }

}
