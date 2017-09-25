package com.example.cityexplorersquad;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static com.example.cityexplorersquad.R.id.imageView;
import static com.example.cityexplorersquad.R.id.points;

public class TaskStatusCardsAdapter extends BaseAdapter {

    private List<Integer> ids;
    private List<String> names;
    private List<String> points;
    private List<String> status;
    private final Context context;

    public TaskStatusCardsAdapter(Context context, List<Integer> ids, List<String> names, List<String> points, List<String> status) {
        this.context = context;
        this.ids = ids;
        this.names = names;
        this.points = points;
        this.status = status;
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

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_task_layout, null);

            holder = new ViewHolder();
            holder.nameText = (TextView) convertView.findViewById(R.id.list_item_name);
            holder.pointText = (TextView) convertView.findViewById(R.id.list_item_points);
            holder.statusImage = (ImageView) convertView.findViewById(R.id.list_item_status);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.nameText.setText(names.get(position));
        holder.pointText.setText(points.get(position) + " points");
        if (status.get(position).equals("completed")) {
            int imageResource = context.getResources().getIdentifier("tick", "mipmap", context.getPackageName());
            Drawable res = context.getResources().getDrawable(imageResource);
            holder.statusImage.setImageDrawable(res);
        }

        return convertView;
    }

    public void removeItem(int position) {
        ids.remove(position);
        names.remove(position);
        points.remove(position);
        status.remove(position);
    }

    private static class ViewHolder {
        private TextView nameText;
        private TextView pointText;
        private ImageView statusImage;
    }

}
