package com.example.cityexplorersquad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class TasksCardLayout extends Fragment {

    private String server_ip;
    private int journey_id;
    private String city;
    private ListView cardsList;
    private TaskCardsAdapter cardsAdapter;

    ArrayList<Integer> ids;
    ArrayList<String> names;
    ArrayList<String> points;

    JSONParser jParser = new JSONParser();

    // url to get all tasks list
    private String url_all_tasks;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_JOURNEY_ID = "journey_id";
    private static final String TAG_TASKS = "tasks";
    private static final String TAG_TASK_ID = "task_id";
    private static final String TAG_TASKNAME = "task_name";
    private static final String TAG_POINTS = "points";

    private final int VIEW_TASK_REQUEST_CODE = 1;

    private ArrayList<Integer> addedTaskIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);

        ids = new ArrayList<>();
        names = new ArrayList<>();
        points = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_card_layout, container, false);
        cardsList = (ListView) rootView.findViewById(R.id.cards_list);

        // initialise urls
        server_ip = getArguments().getString("server_ip");
        journey_id = getArguments().getInt("journey_id");
        city = getArguments().getString("city");
        url_all_tasks = "http://" + server_ip + "/IDS/get_all_tasks.php";

        new TasksCardLayout.GetAllTasks().execute();

        cardsList.setOnItemClickListener(new TasksCardLayout.ListItemClickListener());

        addedTaskIds = new ArrayList<Integer>();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.about_action_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    /*
    Background Async Task to Load all tasks by making HTTP Request
 */
    private class GetAllTasks extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess((FragmentActivity) getActivity());
            if (connection.isOnline()) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("city", city));
                // getting JSON string from URL
                JSONObject json = jParser.makeHttpRequest(url_all_tasks, "GET", params);

                Log.d("All Tasks: ", json.toString());

                int success;
                final String message;
                try {
                    // Checking for SUCCESS TAG
                    success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        JSONArray tasks = json.getJSONArray(TAG_TASKS);

                        ids = new ArrayList<>();
                        names = new ArrayList<>();
                        points = new ArrayList<>();

                        // looping through All journeys
                        for (int i = 0; i < tasks.length(); i++) {
                            JSONObject c = tasks.getJSONObject(i);

                            // Storing each json item in variable
                            int id = c.getInt(TAG_TASK_ID);
                            String name = c.getString(TAG_TASKNAME);
                            String point = c.getString(TAG_POINTS);

                            ids.add(id);
                            names.add(name);
                            points.add(point);

                        }
                    } else {
                        // no task found
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
            // updating UI from Background Thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        cardsAdapter = new TaskCardsAdapter(getActivity(), ids, names, points);
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
            Intent intent = new Intent(getActivity(), DisplayTaskDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(TAG_SERVER_IP, server_ip);
            bundle.putInt(TAG_JOURNEY_ID, journey_id);
            int task_id = cardsAdapter.getItem(position);
            bundle.putInt(TAG_TASK_ID, task_id);
            intent.putExtras(bundle);
            startActivityForResult(intent, VIEW_TASK_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VIEW_TASK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // update list view
                Log.d("debug", "onActivityResult in Task fragement called");
                Bundle bundle = data.getExtras();
                int task_id = bundle.getInt("task_id");
                addedTaskIds.add(task_id);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("debug", "onDestroy called");
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList("ids", addedTaskIds);
        Intent resultIntent = new Intent();
        resultIntent.putExtras(bundle);
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        super.onDestroy();
    }
}
