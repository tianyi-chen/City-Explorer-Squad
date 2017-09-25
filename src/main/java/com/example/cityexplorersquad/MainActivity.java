package com.example.cityexplorersquad;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private ViewJourneyListFragmentFragment fragment;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment = new ViewJourneyListFragmentFragment();
            transaction.replace(R.id.container, fragment);
            Bundle bundle = getIntent().getExtras();
            fragment.setArguments(bundle);
            transaction.commit();

            // starts the service that continuously gets the gps
            Log.d("Location service", "start service");
            serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.putExtras(bundle);
            this.startService(serviceIntent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            fragment.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        stopService(serviceIntent);
        super.onDestroy();
    }

}
