package com.example.cityexplorersquad;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DisplayTaskDetailsActivity extends AppCompatActivity {

    private String server_ip;
    private int journey_id;
    private int task_id;
    private String city;
    private String task_name;
    private String content;
    private String points;

    JSONParser jParser = new JSONParser();

    // url to get task details
    private String url_get_task_details;
    // url to add a task
    private String url_add_task;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_TASK = "task";
    private static final String TAG_CITY = "city";
    private static final String TAG_TASK_ID = "task_id";
    private static final String TAG_JOURNEY_ID = "journey_id";
    private static final String TAG_TASKNAME = "task_name";
    private static final String TAG_CONTENT = "content";
    private static final String TAG_POINTS = "points";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_task_details);

        Bundle bundle = getIntent().getExtras();
        server_ip = bundle.getString(TAG_SERVER_IP);
        journey_id = bundle.getInt(TAG_JOURNEY_ID);
        task_id = bundle.getInt(TAG_TASK_ID);

        url_get_task_details = "http://" + server_ip + "/IDS/get_task_by_id.php";
        url_add_task = "http://" + server_ip + "/IDS/add_task.php";

        new GetTaskDetails().execute();
    }

    /**
     * Background Async Task to Get complete task details
     * */
    class GetTaskDetails extends AsyncTask<String, String, String> {

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
                params.add(new BasicNameValuePair(TAG_TASK_ID, Integer.toString(task_id)));

                JSONObject json = jParser.makeHttpRequest(url_get_task_details, "GET", params);

                Log.d("Single Task Details", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    JSONArray taskObj = json.getJSONArray(TAG_TASK);

                    JSONObject task = taskObj.getJSONObject(0);

                    task_name = task.getString(TAG_TASKNAME);
                    city = task.getString(TAG_CITY);
                    content = task.getString(TAG_CONTENT);
                    points = task. getString(TAG_POINTS);

                }else{
                    // no task with id not found
                    Log.d("debug", "no data found");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    ((TextView) findViewById(R.id.task_name)).setText(task_name);
                    ((TextView) findViewById(R.id.city)).setText(city);
                    ((TextView) findViewById(R.id.content)).setText(content);
                    ((TextView) findViewById(R.id.points)).setText("points: " + points);
                }
            });

            return null;
        }
    }

    public void onClickAdd(View view) {
        new AddTask().execute();
    }

    /*
        Bcakground AsyncTask to add a task to a journey
     */
    class AddTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_TASK_ID, Integer.toString(task_id)));
            params.add(new BasicNameValuePair(TAG_JOURNEY_ID, Integer.toString(journey_id)));

            int success;
            final String message;
            // check for success tag
            try {
                // getting JSON Object

                JSONObject json = jParser.makeHttpRequest(url_add_task, "POST", params);

                success = json.getInt(TAG_SUCCESS);
                message = json.getString(TAG_MESSAGE);

                // check log cat fro response
                Log.d("Response", json.toString());


                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(DisplayTaskDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

                if (success == 1) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("task_id", task_id);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    // failed to add task

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
