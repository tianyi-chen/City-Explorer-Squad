package com.example.cityexplorersquad;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddJourneyActivity extends AppCompatActivity {

    private String server_ip;
    private int journey_id;
    private String user_name;
    private String member_name;
    private String city;
    private String date;

    JSONParser jParser = new JSONParser();

    // url to delete a journey
    private String url_delete_journey;
    // url to update a journey
    private String url_update_journey;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_USERNAME = "user_name";
    private static final String TAG_JOURNEY_ID = "journey_id";
    private static final String TAG_CITY = "city";
    private static final String TAG_DATE = "date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_add_journey);

        Bundle bundle = getIntent().getExtras();
        server_ip = bundle.getString(TAG_SERVER_IP);
        journey_id = bundle.getInt(TAG_JOURNEY_ID);
        user_name = bundle.getString(TAG_USERNAME);

        url_delete_journey = "http://" + server_ip + "/IDS/delete_journey.php";
        url_update_journey = "http://" + server_ip + "/IDS/update_journey.php";
    }

    public void onClickAddMember(View view) {
        city = ((TextView) findViewById(R.id.cityInput)).getText().toString();
        date = ((TextView) findViewById(R.id.dateInput)).getText().toString();
        Intent intent = new Intent(AddJourneyActivity.this, ViewMemberListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(TAG_SERVER_IP, server_ip);
        bundle.putInt(TAG_JOURNEY_ID, journey_id);
        bundle.putString(TAG_CITY, city);
        bundle.putString(TAG_DATE, date);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void onClickFinish(View view) {
        if (fieldMissing()) {
            Toast.makeText(AddJourneyActivity.this, "Required field(s) is missing", Toast.LENGTH_SHORT).show();
        } else {
            new UpdateJourney().execute();
            finish();
        }

    }

    private boolean fieldMissing() {
        city = ((TextView) findViewById(R.id.cityInput)).getText().toString();
        date = ((TextView) findViewById(R.id.dateInput)).getText().toString();
        return city.equals("") || date.equals("");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            dialog("Do you want to leave this page?", "The journey details will not be saved.");
        }
        return super.onKeyDown(keyCode, event);
    }

    private void dialog(String title, String content) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(AddJourneyActivity.this);
        builder.setMessage(content);
        builder.setTitle(title);
        builder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                new DeleteJourney().execute();
                finish();
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

    /*
        Bcakground AsyncTask to delete a journey by making HTTP request
     */
    class DeleteJourney extends AsyncTask<String, String, String> {

        protected String doInBackground(String... arg0) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_JOURNEY_ID, Integer.toString(journey_id)));

            jParser.makeHttpRequest(url_delete_journey, "POST", params);

            return null;
        }

    }

    /*
        Bcakground AsyncTask to update a journey by making HTTP request
     */
    class UpdateJourney extends AsyncTask<String, String, String> {

        protected String doInBackground(String... arg0) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_JOURNEY_ID, Integer.toString(journey_id)));
            params.add(new BasicNameValuePair(TAG_CITY, city));
            params.add(new BasicNameValuePair(TAG_DATE, date));

            jParser.makeHttpRequest(url_update_journey, "POST", params);

            return null;
        }

    }

}
