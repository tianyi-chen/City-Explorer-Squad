package com.example.cityexplorersquad;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConnectServerActivity extends AppCompatActivity {

    private String user_name;

    JSONParser jParser = new JSONParser();

    // url to post client ip address to server
    private String url_post_ip;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_USERNAME = "user_name";

    private static final String server_ip = "ec2-52-56-222-152.eu-west-2.compute.amazonaws.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_connect_server);

    }

    public void onClickConnect(View view) {
        InternetAccess connection = new InternetAccess(ConnectServerActivity.this);
        if (connection.isOnline() && !fieldMissing()) {
            url_post_ip = "http://" + server_ip + "/IDS/post_ip.php";
            new PostIpAddress().execute();
        } else if (fieldMissing()){
            Toast.makeText(ConnectServerActivity.this, "Required field(s) is missing", Toast.LENGTH_SHORT).show();
        } else {
           connection.dialog();
        }

    }

    private boolean fieldMissing() {
        user_name = ((TextView) findViewById(R.id.user_name)).getText().toString();
        return user_name.equals("");
    }

    /**
     * Background Async Task to post ip address to the server
     * */
    class PostIpAddress extends AsyncTask<String, String, String> {

        ProgressDialog loading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(ConnectServerActivity.this, "Logging in", "Please wait...",true,true);
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            loading.dismiss();
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
        }

        protected String doInBackground(String... arg0) {

            int success;
            final String message;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair(TAG_USERNAME, user_name));

                Log.d("url", url_post_ip);
                JSONObject json = jParser.makeHttpRequest(url_post_ip, "POST", params);

                Log.d("Response:", json.toString());

                // json success and message tags
                success = json.getInt(TAG_SUCCESS);
                message = json.getString(TAG_MESSAGE);

                if (success == 1) {
                    // Successfully connected to server
                    Intent intent = new Intent(ConnectServerActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(TAG_SERVER_IP, server_ip);
                    bundle.putString(TAG_USERNAME, user_name);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }

                return message;


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return "Oops! An error occurred";
        }
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
}
