package com.example.cityexplorersquad;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ViewJourneyActivity extends AppCompatActivity {

    private ViewJourneyFragmentFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_journey);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment = new ViewJourneyFragmentFragment();
            transaction.replace(R.id.container, fragment);
            Bundle bundle = getIntent().getExtras();
            fragment.setArguments(bundle);
            transaction.commit();
        }

    }

    public void onClickGiftShop(View view) {
        fragment.onClickGiftShop();
    }

}
