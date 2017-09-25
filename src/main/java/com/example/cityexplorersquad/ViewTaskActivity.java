package com.example.cityexplorersquad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static android.R.attr.bitmap;
import static android.R.attr.logo;
import static com.example.cityexplorersquad.R.id.date;
import static com.example.cityexplorersquad.R.id.submenuarrow;

public class ViewTaskActivity extends AppCompatActivity {

    private String server_ip;
    private int journey_id;
    private String city;
    private int task_id;
    private String task_name;
    private String content;
    private String status;
    private String points;

    JSONParser jParser = new JSONParser();

    // url to get task details
    private String url_get_task_details;
    // url to upload photo
    private String url_upload_photo;
    // url to get locations
    private String url_get_locations;
    // url to update task status
    private String url_update_task;
    // url to get the uri to download image
    private String url_get_image_path;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_JOURNEY_ID = "journey_id";
    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_USERNAME = "user_name";
    private static final String TAG_TASK = "task";
    private static final String TAG_CITY = "city";
    private static final String TAG_TASK_ID = "task_id";
    private static final String TAG_TASK_NAME = "task_name";
    private static final String TAG_CONTENT = "content";
    private static final String TAG_POINTS = "points";
    private static final String TAG_STATUS = "status";
    private static final String TAG_IMAGE = "image";
    private static final String TAG_IMAGE_PATH = "image_path";
    private static final String TAG_LOCATIONS = "locations";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LON = "lon";

    JSONArray task = null;

    private int PICK_PHOTO_REQUEST = 1;

    private ImageView imageView;
    private Bitmap bitmap;

    ArrayList<double[]> locationData;

    private final double DISTANCE_RANGE = 10.0;

    String upLoadServerUri;
    String uploadFilePath;
    String downloadImageUri;

    private static final String TAG = "uploadedFile";
    private static final int TIME_OUT = 10 * 1000;
    private static final String CHARSET = "utf-8";
    private static final String BOUNDARY = UUID.randomUUID().toString();
    private static final String PREFIX = "--";
    private static final String LINE_END = "\r\n";
    private static final String CONTENT_TYPE = "multipart/form-data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);

        Bundle bundle = getIntent().getExtras();
        server_ip = bundle.getString(TAG_SERVER_IP);
        journey_id = bundle.getInt(TAG_JOURNEY_ID);
        task_id = bundle.getInt(TAG_TASK_ID);
        Log.d("task id", Integer.toString(task_id));
        status = bundle.getString(TAG_STATUS);

        url_get_task_details = "http://" + server_ip + "/IDS/get_task_by_id.php";
        url_get_locations = "http://" + server_ip + "/IDS/get_locations.php";
        url_update_task = "http://" + server_ip + "/IDS/update_task.php";
        url_get_image_path = "http://" + server_ip + "/IDS/get_image_path.php";

        upLoadServerUri = "http://" + server_ip + "/IDS/upload_photo.php";
        downloadImageUri = "http://" + server_ip + "/IDS/";

        new GetTaskDetails().execute();
        new DownloadImage().execute();

        imageView = (ImageView) findViewById(R.id.imageView);

        locationData = new ArrayList<double[]>();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.about_action_menu, menu);
        return(super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewTaskActivity.this);
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
     * Background Async Task to Get complete task details
     * */
    class GetTaskDetails extends AsyncTask<String, String, String> {

        ProgressDialog loading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(ViewTaskActivity.this, "Loading", "Please wait...",true,true);
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            loading.dismiss();
        }

        /**
         * Getting journey details in background thread
         * */
        protected String doInBackground(String... arg0) {

            final InternetAccess connection = new InternetAccess(ViewTaskActivity.this);
            if (connection.isOnline()) {
                // Check for success tag
                int success;
                try {
                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    Log.d("journey_id" , Integer.toString(journey_id));
                    Log.d("task_id", Integer.toString(task_id));
                    params.add(new BasicNameValuePair(TAG_JOURNEY_ID, Integer.toString(journey_id)));
                    params.add(new BasicNameValuePair(TAG_TASK_ID, Integer.toString(task_id)));

                    JSONObject json = jParser.makeHttpRequest(url_get_task_details, "GET", params);

                    Log.d("Single Task Details", json.toString());

                    // json success tag
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        JSONArray taskObj = json.getJSONArray(TAG_TASK);

                        JSONObject task = taskObj.getJSONObject(0);

                        task_name = task.getString(TAG_TASK_NAME);
                        city = task.getString(TAG_CITY);
                        content = task.getString(TAG_CONTENT);
                        points = task. getString(TAG_POINTS);
                        status = task.getString(TAG_STATUS);

                    }else{
                        // no task with id not found
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
                        ((TextView) findViewById(R.id.status)).setText("status: " + status);
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

    public void onClickChoose(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PHOTO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri filePath = data.getData();
            uploadFilePath = getPath(filePath);
            Log.d("uploadFilePath", uploadFilePath);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                int nh = (int) ( bitmap.getHeight() * (512.0 / bitmap.getWidth()) );
//                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void onClickUpload(View view) {
        if (uploadFilePath != null) {
            Log.d("uploadFilePath", uploadFilePath);
            File file = new File(uploadFilePath);
            new UploadImage().execute(file);

        } else {
            Toast.makeText(getApplicationContext(), "Please choose a photo first", Toast.LENGTH_LONG).show();
        }

    }

    class UploadImage extends AsyncTask<File, Void, String>{

        ProgressDialog loading;
        private volatile boolean running = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(ViewTaskActivity.this, "Uploading photo", "Please wait...",true,true);
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            loading.dismiss();
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onCancelled() {
            running = false;
        }

        @Override
        protected String doInBackground(File... arg0) {
            final InternetAccess connection = new InternetAccess(ViewTaskActivity.this);
            if (connection.isOnline()) {
                try {
                    URL url = new URL(upLoadServerUri);

                    // Open a HTTP  connection to  the URL
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(TIME_OUT);
                    conn.setConnectTimeout(TIME_OUT);
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Charset", CHARSET);
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                    StringBuffer sb = new StringBuffer();

                    HashMap<String, String> params = new HashMap<>();
                    params.put(TAG_JOURNEY_ID, Integer.toString(journey_id));
                    params.put(TAG_TASK_ID, Integer.toString(task_id));

                    sb.append(getRequestData(params));

                    File file = arg0[0];
                    if (file != null) {
                        sb.append(PREFIX);
                        sb.append(BOUNDARY);
                        sb.append(LINE_END);
                        sb.append("Content-Disposition: form-date; name=\"uploaded_file\";filename=\"" + file.getName() + "\"" + LINE_END);
                        sb.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
                        sb.append(LINE_END);
                    }
                    dos.write(sb.toString().getBytes());
                    if (file != null) {
                        InputStream is = new FileInputStream(file);
                        byte[] bytes = new byte[1024];
                        int len = 0;
                        while ((len = is.read(bytes)) != -1) {
                            dos.write(bytes, 0, len);
                        }
                        is.close();
                        dos.write(LINE_END.getBytes());
                        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                        dos.write(end_data);
                    }
                    dos.flush();

                    int res = conn.getResponseCode();

                    if(res == 200){
                        InputStream input = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        JSONObject jObj = new JSONObject(result.toString());
                        Log.i(TAG, "result: " + result.toString());
                        return jObj.getString(TAG_MESSAGE);
                    } else {
                        Log.e(TAG, "request error");
                        return "Oops! An error occurred";
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
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

            return "Oops! An error occurred";
        }
    }

    private static StringBuffer getRequestData(HashMap<String, String> params) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(PREFIX);
                stringBuffer.append(BOUNDARY);
                stringBuffer.append(LINE_END);
                stringBuffer.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_END);
                stringBuffer.append(LINE_END);
                stringBuffer.append(URLEncoder.encode(entry.getValue(), CHARSET));
                stringBuffer.append(LINE_END);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    /**
     * Background Async Task to download the photo
     * */
    class DownloadImage extends AsyncTask<String, String, Integer> {

        ProgressDialog loading;
        private volatile boolean running = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(ViewTaskActivity.this, "Downloading photo", "Please wait...",true,true);
        }

        @Override
        protected void onPostExecute(final Integer success) {
            super.onPostExecute(success);
            loading.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        Log.d("bitmap", bitmap.toString());
                        imageView.setImageBitmap(bitmap);
                    }
                }
            });
        }

        @Override
        protected void onCancelled() {
            running = false;
        }

        protected Integer doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess(ViewTaskActivity.this);
            if (connection.isOnline()) {
                // Check for success tag
                int success;
                try {
                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair(TAG_JOURNEY_ID, Integer.toString(journey_id)));
                    params.add(new BasicNameValuePair(TAG_TASK_ID, Integer.toString(task_id)));

                    JSONObject json = jParser.makeHttpRequest(url_get_image_path, "GET", params);

                    Log.d("Image:", json.toString());

                    // json success tag
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        JSONArray pathObj = json.getJSONArray(TAG_IMAGE);
                        JSONObject image_path = pathObj.getJSONObject(0);
                        downloadImageUri = downloadImageUri + image_path.getString(TAG_IMAGE_PATH);

                        URL url = new URL(downloadImageUri);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        bitmap = BitmapFactory.decodeStream(is);

                    }else{

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

    public void onClickValidate(View view) {
        if (status.equals("completed")) {
            Toast.makeText(getApplicationContext(), "The task is already completed", Toast.LENGTH_LONG).show();
        } else {
            if (uploadFilePath == null && downloadImageUri == null) {
                Toast.makeText(getApplicationContext(), "No photo uploaded", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                locationData = new GetLocations().execute().get();
                int i;
                Log.d("size", Integer.toString(locationData.size()));
                for (i = 0; i < locationData.size() - 1; i++) {
                    double distance = distance(locationData.get(i), locationData.get(i + 1));
                    Log.d("distance", Double.toString(distance));
                    if (distance > DISTANCE_RANGE) {
                        break;
                    }
                }
                if (i == locationData.size() - 1) {
                    // successfully validated
                    ((TextView) findViewById(R.id.status)).setText("status: " + status);
                    new UpdateTask().execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Some member(s) seem to not with the group", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Background Async Task to get locations of all members
     * */
    class GetLocations extends AsyncTask<String, String, ArrayList<double[]>> {

        protected ArrayList<double[]> doInBackground(String... arg0) {

            final InternetAccess connection = new InternetAccess(ViewTaskActivity.this);
            if (connection.isOnline()) {
                // Check for success tag
                int success;
                try {
                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair(TAG_JOURNEY_ID, Integer.toString(journey_id)));

                    JSONObject json = jParser.makeHttpRequest(url_get_locations, "GET", params);

                    Log.d("Locations:", json.toString());

                    // json success tag
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        JSONArray locations = json.getJSONArray(TAG_LOCATIONS);
                        ArrayList<double[]> temp = new ArrayList<double[]>();

                        // looping through All locations
                        for (int i = 0; i < locations.length(); i++) {
                            JSONObject c = locations.getJSONObject(i);

                            // Storing each json item in variable
                            final double lat = Double.parseDouble(c.getString(TAG_LAT));
                            final double lon = Double.parseDouble(c.getString(TAG_LON));

                            temp.add(new double[] {lat, lon});

                            return temp;
                        }

                    }else{

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
    }

    /**
     * Background Async Task to update the task status
     * */
    class UpdateTask extends AsyncTask<String, String, String> {
        ProgressDialog loading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(ViewTaskActivity.this, "Checking locations", "Please wait...",true,true);
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            loading.dismiss();
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... args) {

            final InternetAccess connection = new InternetAccess(ViewTaskActivity.this);
            if (connection.isOnline()) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair(TAG_JOURNEY_ID, Integer.toString(journey_id)));
                params.add(new BasicNameValuePair(TAG_TASK_ID, Integer.toString(task_id)));
                params.add(new BasicNameValuePair(TAG_POINTS, points));

                int success;
                final String message;
                // check for success tag
                try {
                    // getting JSON Object
                    JSONObject json = jParser.makeHttpRequest(url_update_task, "POST", params);

                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);

                    // check log cat fro response
                    Log.d("Response", json.toString());

                    return message;
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

    }

    private double distance(double[] location1, double[] location2) {
        double theta = location1[1] - location2[1];
        double dist = Math.sin(degTorad(location1[0])) * Math.sin(degTorad(location2[0]))
                + Math.cos(degTorad(location1[0])) * Math.cos(degTorad(location2[0])) * Math.cos(degTorad(theta));
        dist = Math.acos(dist);
        dist = radTodeg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344 * 1000;
        return dist;
    }

    private static double degTorad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double radTodeg(double rad) {
        return (rad * 180 / Math.PI);
    }

}
