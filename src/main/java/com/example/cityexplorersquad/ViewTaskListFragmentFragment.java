package com.example.cityexplorersquad;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewTaskListFragmentFragment extends ViewTaskListFragment implements View.OnClickListener {

    private String server_ip;
    private int journey_id;
    private int task_id;
    private String city;
    private String date;
    private ArrayList<Integer> ids;
    private ArrayList<String> names;
    private ArrayList<String> points;
    private ArrayList<String> statuses;
    private ListView cardsList;
    private TaskStatusCardsAdapter cardsAdapter;

    JSONParser jParser = new JSONParser();

    // url to get all journeys list
    private String url_get_tasks;
    // url to get journey details
    private String url_get_journey_details;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_JOURNEY_ID = "journey_id";
    private static final String TAG_JOURNEY = "journey";
    private static final String TAG_CITY = "city";
    private static final String TAG_DATE = "date";
    private static final String TAG_TASKS = "tasks";
    private static final String TAG_TASK_ID = "task_id";
    private static final String TAG_TASKNAME = "task_name";
    private static final String TAG_STATUS = "status";
    private static final String TAG_POINTS = "points";
    private static final String LOG_TAG = ViewJourneyListFragmentFragment.class.getSimpleName();

    JSONArray tasks = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);

        ids = new ArrayList<>();
        names = new ArrayList<>();
        points = new ArrayList<>();
        statuses = new ArrayList<>();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cardsList = (ListView) getView().findViewById(R.id.cards_list);

        Bundle bundle = getArguments();
        server_ip = bundle.getString(TAG_SERVER_IP);
        journey_id = bundle.getInt(TAG_JOURNEY_ID);
        city = bundle.getString(TAG_CITY);
        date = bundle.getString(TAG_DATE);

        ((TextView) getView().findViewById(R.id.city)).setText(city);
        ((TextView) getView().findViewById(R.id.date)).setText(date);

        url_get_tasks = "http://" + server_ip + "/IDS/get_tasks.php";
        url_get_journey_details = "http://" + server_ip + "/IDS/get_journey_by_id.php";

        cardsList.setOnItemClickListener(new ListItemClickListener());

        // set onclick listen to add task button
        Button add_task = (Button) getView().findViewById(R.id.add_task);
        add_task.setOnClickListener(this);

        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                initiateRefresh();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        new GetTasks().execute();
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

        new GetTasks().execute();
    }

    /**
     * Background Async Task to Get complete journey details
     * */
    class GetJourneyDetails extends AsyncTask<String, String, String> {

        /**
         * Getting journey details in background thread
         * */
        protected String doInBackground(String... arg0) {

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

                }else{
                    // no journey with id not found
                    Log.d("debug", "no data found");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // updating UI from Background Thread
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ((TextView) getView().findViewById(R.id.city)).setText(city);
                    ((TextView) getView().findViewById(R.id.date)).setText(date);
                }
            });

            return null;
        }
    }

    /**
     * Background Async Task to Load tasks by making HTTP Request
     * */
    private class GetTasks extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess((FragmentActivity) getActivity());
            if (connection.isOnline()) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair(TAG_JOURNEY_ID, Integer.toString(journey_id)));

                // getting JSON string from URL
                JSONObject json = jParser.makeHttpRequest(url_get_tasks, "GET", params);

                Log.d("Tasks: ", json.toString());

                final String message;
                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // Getting Array of tasks
                        tasks = json.getJSONArray(TAG_TASKS);

                        ids = new ArrayList<>();
                        names = new ArrayList<>();
                        points = new ArrayList<>();
                        statuses = new ArrayList<>();

                        // looping through all tasks
                        for (int i = 0; i < tasks.length(); i++) {
                            JSONObject c = tasks.getJSONObject(i);

                            // Storing each json item in variable
                            int task_id = c.getInt(TAG_TASK_ID);
                            String task_name = c.getString(TAG_TASKNAME);
                            String status = c.getString(TAG_STATUS);
                            String point = c.getString(TAG_POINTS);

                            ids.add(task_id);
                            names.add(task_name);
                            points.add(point);
                            statuses.add(status);

                        }
                    } else {
                        // no task found
                        message = json.getString(TAG_MESSAGE);
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

        @Override
        protected void onPostExecute(String file_url) {
            super.onPostExecute(file_url);
            // Stop the refreshing indicator
            setRefreshing(false);
            // updating UI from Background Thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        cardsAdapter = new TaskStatusCardsAdapter(getActivity(), ids, names, points, statuses);
                        cardsList.setAdapter(cardsAdapter);
                    }
                });
            }

        }

    }

    private final class ListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // view task
            task_id = ids.get(position);
            Intent intent = new Intent(getActivity(), ViewTaskActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(TAG_SERVER_IP, server_ip);
            bundle.putInt(TAG_JOURNEY_ID, journey_id);
            Log.d("id", Integer.toString(task_id));
            bundle.putInt(TAG_TASK_ID, task_id);
            Log.d("id", Integer.toString(bundle.getInt("task_id")));
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void onClick(View view) {
        // add new task
        Intent intent = new Intent(getActivity(), DisplayAllTasksActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(TAG_SERVER_IP, server_ip);
        bundle.putInt(TAG_JOURNEY_ID, journey_id);
        bundle.putString(TAG_CITY, city);
        intent.putExtras(bundle);
        startActivity(intent);
    }


}
