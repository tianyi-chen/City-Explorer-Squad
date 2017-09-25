package com.example.cityexplorersquad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class GiftShopCardLayout extends Fragment {


    private String server_ip;
    private String members;
    private String total_points;
    private ListView cardsList;
    private GiftCardsAdapter cardsAdapter;

    ArrayList<Integer> ids;
    ArrayList<String> names;
    ArrayList<String> points;

    JSONParser jParser = new JSONParser();

    // url to get all tasks list
    private String url_all_gifts;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SERVER_IP = "server_ip";
    private static final String TAG_GIFTS = "gifts";
    private static final String TAG_GIFT_ID = "gift_id";
    private static final String TAG_GIFTNAME = "gift_name";
    private static final String TAG_POINTS = "points";
    private static final String TAG_MEMBERS = "members";
    private static final String TAG_GIFT_POINTS = "gift_points";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Notify the system to allow an options menu for this fragment.
        setHasOptionsMenu(true);

        ids = new ArrayList<>();
        names = new ArrayList<>();
        points = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gift_card_layout, container, false);
        cardsList = (ListView) rootView.findViewById(R.id.cards_list);

        // initialise urls
        server_ip = getArguments().getString(TAG_SERVER_IP);
        members = getArguments().getString(TAG_MEMBERS);
        total_points = getArguments().getString(TAG_POINTS);

        url_all_gifts = "http://" + server_ip + "/IDS/get_all_gifts.php";

        new GetAllGifts().execute();

        cardsList.setOnItemClickListener(new GiftShopCardLayout.ListItemClickListener());

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.about_action_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_message).setTitle(R.string.app_name);
                builder.setPositiveButton(R.string.dialog_ok, null);
                builder.setIcon(R.drawable.ic_launcher);

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    Background Async Task to Load all tasks by making HTTP Request
 */
    private class GetAllGifts extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            final InternetAccess connection = new InternetAccess((FragmentActivity) getActivity());
            if (connection.isOnline()) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                // getting JSON string from URL
                JSONObject json = jParser.makeHttpRequest(url_all_gifts, "GET", params);

                Log.d("All Gifts: ", json.toString());

                int success;
                final String message;
                try {
                    // Checking for SUCCESS TAG
                    success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        JSONArray tasks = json.getJSONArray(TAG_GIFTS);

                        ids = new ArrayList<>();
                        names = new ArrayList<>();
                        points = new ArrayList<>();

                        // looping through All gifts
                        for (int i = 0; i < tasks.length(); i++) {
                            JSONObject c = tasks.getJSONObject(i);

                            // Storing each json item in variable
                            int id = c.getInt(TAG_GIFT_ID);
                            String name = c.getString(TAG_GIFTNAME);
                            String point = c.getString(TAG_POINTS);

                            ids.add(id);
                            names.add(name);
                            points.add(point);

                        }
                    } else {
                        // no task found
                        message = json.getString(TAG_MESSAGE);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        connection.dialog();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            // updating UI from Background Thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        cardsAdapter = new GiftCardsAdapter(getActivity(), ids, names, points);
                        cardsList.setAdapter(cardsAdapter);
                    }
                });
            }

            ((TextView) getView().findViewById(R.id.points)).setText(total_points + " points");
            ((TextView) getView().findViewById(R.id.members)).setText(members + " members");

        }
    }


    private final class ListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // view gift
            Intent intent = new Intent(getActivity(), DisplayGiftDetailsActivity.class);
            Bundle bundle = new Bundle();
            String gift_name = cardsAdapter.getGiftName(position);
            bundle.putString(TAG_GIFTNAME, gift_name);
            String points = cardsAdapter.getPoints(position);
            bundle.putString(TAG_GIFT_POINTS, points);
            bundle.putString(TAG_POINTS, total_points);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

}
