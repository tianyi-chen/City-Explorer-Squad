package com.example.cityexplorersquad;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class TaskCardsAdapter extends BaseAdapter {

    private List<Integer> ids;
    private List<String> names;
    private List<String> points;
    private final Context context;

    public TaskCardsAdapter(Context context, List<Integer> ids, List<String> names, List<String> points) {
        this.context = context;
        this.ids = ids;
        this.names = names;
        this.points = points;
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
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TaskCardsAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_task_card, null);

            holder = new TaskCardsAdapter.ViewHolder();
            holder.nameText = (TextView) convertView.findViewById(R.id.list_item_name);
            holder.pointText = (TextView) convertView.findViewById(R.id.list_item_points);
            convertView.setTag(holder);

        } else {
            holder = (TaskCardsAdapter.ViewHolder) convertView.getTag();
        }

        holder.nameText.setText(names.get(position));
        holder.pointText.setText(points.get(position) + " points");

        return convertView;
    }

    private static class ViewHolder {
        private TextView nameText;
        private TextView pointText;
    }

}
