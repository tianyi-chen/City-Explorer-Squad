package com.example.cityexplorersquad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GiftShopActivity extends AppCompatActivity {

    private GiftShopCardLayout giftShopCardLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift_shop);

        if (savedInstanceState == null) {
            giftShopCardLayout = new GiftShopCardLayout();
            Bundle bundle = getIntent().getExtras();
            giftShopCardLayout.setArguments(bundle);
            getFragmentManager().beginTransaction().add(R.id.container, giftShopCardLayout).commit();
        }
    }
}
