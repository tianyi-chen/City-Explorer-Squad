package com.example.cityexplorersquad;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

public class InternetAccess {

    Activity activity;
    private static final String DIALOG_TITLE = "No Internet Connection";
    private static final String DIALOG_CONTENT = "Please connect to the Internet before using this app.";

    public InternetAccess(AppCompatActivity activity) {
        this.activity = activity;
    }

    public InternetAccess(FragmentActivity activity) {
        this.activity = activity;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void dialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(activity);
        builder.setMessage(DIALOG_CONTENT);
        builder.setTitle(DIALOG_TITLE);
        builder.setPositiveButton("I see", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setIcon(R.drawable.ic_launcher);
        builder.create().show();
    }
}
