package com.example.cityexplorersquad;

import java.util.List;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/*******************************************************************************************
 * * Reused work:
 *    Title: Creating a Cards UI on Android
 *    Author: Yasin Yildirim
 *    Date: 03/06/2014
 *    Code version: N/A
 *    Availability: https://github.com/vudin/android-cards-ui-example
 ********************************************************************************************/
public class JourneyCardsAdapter extends BaseAdapter {

    private List<Integer> ids;
    private List<String> cities;
    private List<String> dates;
    private final OnClickListener itemButtonClickListener;
    private final Context context;

    public JourneyCardsAdapter(Context context, List<Integer> ids, List<String> cities, List<String> dates, OnClickListener itemButtonClickListener) {
        this.context = context;
        this.ids = ids;
        this.cities = cities;
        this.dates = dates;
        this.itemButtonClickListener = itemButtonClickListener;
        Log.d("debug", "card adapter initialised");
    }

    @Override
    public int getCount() {
        return ids.size();
    }

    @Override
    public Integer getItem(int position) {
        return ids.get(position);
    }

    @Override
    public long getItemId(int journey_id) {
        int position = 0;
        for (int id : ids) {
            if (id == journey_id) {
                break;
            }
            position++;
        }
        return position;
    }

    public String getCity(int position) {
        return cities.get(position);
    }

    public String getDate(int position) {
        return dates.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_journey_card, null);

            holder = new ViewHolder();
            holder.cityText = (TextView) convertView.findViewById(R.id.list_item_city);
            holder.dateText = (TextView) convertView.findViewById(R.id.list_item_date);
            holder.itemButton1 = (Button) convertView.findViewById(R.id.list_item_card_button_1);
            holder.itemButton2 = (Button) convertView.findViewById(R.id.list_item_card_button_2);
            holder.itemButton3 = (Button) convertView.findViewById(R.id.list_item_card_button_3);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.cityText.setText(cities.get(position));
        holder.dateText.setText(dates.get(position));

        if (itemButtonClickListener != null) {
            holder.itemButton1.setOnClickListener(itemButtonClickListener);
            holder.itemButton2.setOnClickListener(itemButtonClickListener);
            holder.itemButton3.setOnClickListener(itemButtonClickListener);
        }
        
        return convertView;
    }

    public void insertItem(int id, String city, String date) {
        ids.add(id);
        cities.add(city);
        dates.add(date);
    }

    public void removeItem(int journey_id) {
        int position = (int) getItemId(journey_id);
        cities.remove(position);
        dates.remove(position);
        ids.remove(position);
    }

    private static class ViewHolder {
        private TextView cityText;
        private TextView dateText;
        private Button itemButton1;
        private Button itemButton2;
        private Button itemButton3;
    }

}
