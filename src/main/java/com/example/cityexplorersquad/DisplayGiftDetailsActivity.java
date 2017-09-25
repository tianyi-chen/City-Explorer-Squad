package com.example.cityexplorersquad;

import android.app.Activity;
import android.content.DialogInterface;
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

public class DisplayGiftDetailsActivity extends AppCompatActivity {


    private String gift_name;
    private String gift_points;
    private String points;

    // JSON Node names
    private static final String TAG_GIFT_NAME = "gift_name";
    private static final String TAG_POINTS = "points";
    private static final String TAG_GIFT_POINTS = "gift_points";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_gift_details);

        Bundle bundle = getIntent().getExtras();
        gift_name = bundle.getString(TAG_GIFT_NAME);
        points = bundle.getString(TAG_POINTS);
        gift_points = bundle.getString(TAG_GIFT_POINTS);

        ((TextView) findViewById(R.id.gift_name)).setText(gift_name);
        ((TextView) findViewById(R.id.points)).setText("points: " + gift_points);
    }

    public void onClickPurchase(View view) {
        if (Integer.parseInt(points) < Integer.parseInt(gift_points)) {
            dialog("Insufficient points", "Do you want to have a look at other items?");
        } else {
            Toast.makeText(DisplayGiftDetailsActivity.this, "You have purchased this gift for your team!", Toast.LENGTH_SHORT).show();
        }
    }

    private void dialog(String title, String content) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(DisplayGiftDetailsActivity.this);
        builder.setMessage(content);
        builder.setTitle(title);
        builder.setPositiveButton("I SEE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setIcon(R.drawable.ic_launcher);
        builder.create().show();
    }

}
