package com.example.cityexplorersquad;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ShareToFacebookActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private LoginManager manager;
    private String uri;
    private String city;
    private String date;

    private static final String TAG_IMAGE_PATH = "image_path";
    private static final String TAG_CITY = "city";
    private static final String TAG_DATE = "date";

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_to_facebook);

        Bundle bundle = getIntent().getExtras();
        uri = bundle.getString(TAG_IMAGE_PATH);
        city = bundle.getString(TAG_CITY);
        date = bundle.getString(TAG_DATE);
        ((TextView) findViewById(R.id.city)).setText(city);
        ((TextView) findViewById(R.id.date)).setText(date);
        Log.d("image_path", uri);

        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        List<String> permissionNeeds = Arrays.asList("publish_actions");

        manager = LoginManager.getInstance();
        manager.logInWithPublishPermissions(this, permissionNeeds);
        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    sharePhotoToFacebook();
                }

                @Override
                public void onCancel() {
                    System.out.println("onCancel");
                    finish();
                }

                @Override
                public void onError(FacebookException exception) {
                    System.out.println("onError");
                    finish();
                }
            });
        }

    private void sharePhotoToFacebook(){
        new DownloadPhoto().execute();

    }

    private class DownloadPhoto extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            runOnUiThread(new Runnable() {
                public void run() {

                    Log.d("debug", bitmap.toString());

                    ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitmap);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShareToFacebookActivity.this);
                    builder.setTitle("Do you want to share this photo to Facebook?");
                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            SharePhoto photo = new SharePhoto.Builder()
                                    .setBitmap(bitmap)
                                    .setCaption("Shared from City Explorer Squad")
                                    .build();

                            SharePhotoContent content = new SharePhotoContent.Builder()
                                    .addPhoto(photo)
                                    .build();

                            ShareApi.share(content, null);
                            Toast.makeText(getApplicationContext(), "Photo successfully shared to Facebook!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    BitmapDrawable icon = new BitmapDrawable(getResources(), bitmap);
                    builder.setIcon(icon);

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            });

        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(uri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }
}
