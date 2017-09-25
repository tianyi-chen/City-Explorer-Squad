package com.example.cityexplorersquad;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

public class ViewMemberListActivity extends AppCompatActivity {

    private String server_ip;
    private int journey_id;
    private String member_name;
    private String city;
    private String date;

    JSONParser jParser = new JSONParser();

    private ArrayList<HashMap<String,String>> member_names = new ArrayList<>();

    private SimpleAdapter dataAdapter;

    // url to get all member list
    private String url_get_members;
    // url to get journey details
    private String url_get_journey_details;
    // url to add a member
    private String url_add_member;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_USERNAME = "user_name";
    private static final String TAG_JOURNEY_ID = "journey_id";
    private static final String TAG_JOURNEY = "journey";
    private static final String TAG_CITY = "city";
    private static final String TAG_DATE = "date";
    private static final String TAG_MEMBERS = "members";

    JSONArray members = null;

    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_view_member_list);

        Bundle bundle = getIntent().getExtras();
        server_ip = bundle.getString(TAG_SERVER_IP);
        journey_id = bundle.getInt(TAG_JOURNEY_ID);
        city = bundle.getString(TAG_CITY);
        date = bundle.getString(TAG_DATE);

        ((TextView) findViewById(R.id.city)).setText(city);
        ((TextView) findViewById(R.id.date)).setText(date);

        url_get_members = "http://" + server_ip + "/IDS/get_members.php";
        url_get_journey_details = "http://" + server_ip + "/IDS/get_journey_by_id.php";
        url_add_member = "http://" + server_ip + "/IDS/add_member.php";

        new GetMembers().execute();
    }

    /**
     * Background Async Task to Get complete journey details
     * */
    class GetJourneyDetails extends AsyncTask<String, String, String> {

        /**
         * Getting journey details in background thread
         * */
        protected String doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess(ViewMemberListActivity.this);
            if (connection.isOnline()) {
                // Check for success tag
                int success;
                try {
                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("journey_id", Integer.toString(journey_id)));

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
                runOnUiThread(new Runnable() {
                    public void run() {
                        ((TextView) findViewById(R.id.city)).setText(city);
                        ((TextView) findViewById(R.id.date)).setText(date);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        connection.dialog();
                    }
                });
            }
            return null;
        }
    }

    /**
     * Background Async Task to Load member names by making HTTP Request
     * */
    private class GetMembers extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess(ViewMemberListActivity.this);
            if (connection.isOnline()) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("journey_id", Integer.toString(journey_id)));

                // getting JSON string from URL
                JSONObject json = jParser.makeHttpRequest(url_get_members, "GET", params);

                Log.d("Members: ", json.toString());

                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // Getting Array of members
                        members = json.getJSONArray(TAG_MEMBERS);

                        // looping through All Products
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject c = members.getJSONObject(i);

                            // Storing each json item in variable
                            String user_name = c.getString(TAG_USERNAME);

                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put(TAG_USERNAME, user_name);
                            member_names.add(map);
                        }
                    } else {
                        // no member found


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                runOnUiThread(new Runnable() {
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
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into list view
                     * */
                    dataAdapter = new SimpleAdapter(
                            ViewMemberListActivity.this,
                            member_names,
                            R.layout.list_member_layout,
                            new String[] {TAG_USERNAME},
                            new int[] { R.id.member_name }
                    );
                    // updating list view
                    ((ListView) findViewById(R.id.listview)).setAdapter(dataAdapter);
                }
            });

        }
    }

    public void onClickAdd(View view) {
        searchView.setIconified(false);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_action_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String queryText) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String queryText) {
                member_name = queryText;
                new AddMember().execute();
                searchView.setIconified(true);

                searchView.setQuery("", false);
                searchView.clearFocus();  // close keyboard
                searchView.onActionViewCollapsed();
//                searchItem.collapseActionView();

                return true;
            }
        });

        return true;
    }

    /**
     * Background Async Task to add a member to a journey
     * */
    class AddMember extends AsyncTask<String, String, String> {

        /**
         * Adding a member through making HTTP request
         */
        @Override
        protected String doInBackground(String... args) {

            final InternetAccess connection = new InternetAccess(ViewMemberListActivity.this);
            if (connection.isOnline()) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user_name", member_name));
                params.add(new BasicNameValuePair("journey_id", Integer.toString(journey_id)));

                int success;
                final String message;
                // check for success tag
                try {
                    // getting JSON Object
                    JSONObject json = jParser.makeHttpRequest(url_add_member, "POST", params);

                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);

                    // check log cat fro response
                    Log.d("Response", json.toString());

                    if (success == 1) {
                        // successfully added member to journey
                        // update list view
                        final HashMap<String, String> newMember = new HashMap<>();
                        newMember.put(TAG_USERNAME, member_name);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                member_names.add(newMember);
                                dataAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        });


                    } else {
                        // failed to add member
                        runOnUiThread(new Runnable() {
                            public void run() {
                                dialog(message);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        connection.dialog();
                    }
                });
            }

            member_name = "";
            return null;
        }

    }

    protected void dialog(String message) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ViewMemberListActivity.this);
        builder.setMessage(message);
        builder.setTitle("Cannot invite this user");
        builder.setPositiveButton("I see", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
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
            case R.id.action_search:

        }
        return super.onOptionsItemSelected(item);
    }

}
