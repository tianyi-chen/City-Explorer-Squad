package com.example.cityexplorersquad;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {

    private String server_ip;
    private String user_name;
    private double lat;
    private double lon;

    JSONParser jParser = new JSONParser();

    // url to update location info
    private String url_update_location;

    private final IBinder binder = new LocationBinder();

    // location variables
    private LocationManager locationManager;
    private MyLocationListener locationListener;

    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_USERNAME= "user_name";
    private static final String TAG_LAT= "lat";
    private static final String TAG_LON= "lon";

    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        server_ip = bundle.getString(TAG_SERVER_IP);
        user_name = bundle.getString(TAG_USERNAME);
        url_update_location = "http://" + server_ip + "/IDS/update_location.php";

        Log.d("Location service", "service onStart");

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        locationListener = new MyLocationListener();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                5000, 5, locationListener);

        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Location service", "service onBind");
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.d("Location service", "service onDestroy");
        super.onDestroy();
    }

    public class LocationBinder extends Binder {

    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location == null)
                return;

            if (isConnectingToInternet(getApplicationContext())) {
                Log.e("latitude", location.getLatitude() + "");
                Log.e("longitude", location.getLongitude() + "");

                lat = location.getLatitude();
                lon = location.getLongitude();

                new UpdateLocation().execute();

            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("onStatusChanged", provider + " " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("onProviderEnabled", provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("onProviderDisabled", provider);
        }

    }

    public static boolean isConnectingToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    /**
     * Background Async Task to update the location of the user
     * */
    class UpdateLocation extends AsyncTask<String, String, String> {

        protected String doInBackground(String... arg0) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_USERNAME, user_name));
            params.add(new BasicNameValuePair(TAG_LAT, Double.toString(lat)));
            params.add(new BasicNameValuePair(TAG_LON, Double.toString(lon)));

            JSONObject json = jParser.makeHttpRequest(url_update_location, "POST", params);

            try {
                String message = json.getString("message");
                Log.d("message", message);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
