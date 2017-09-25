package com.example.cityexplorersquad;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class ViewPhotosActivity extends AppCompatActivity {

    private ViewPhotoListFragmentFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photos);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment = new ViewPhotoListFragmentFragment();
            transaction.replace(R.id.container, fragment);
            Bundle bundle = getIntent().getExtras();
            fragment.setArguments(bundle);
            transaction.commit();
        }
    }
}
