package com.example.cityexplorersquad;

import android.content.Context;
import android.graphics.Bitmap;
import java.util.List;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

public class PhotoCardsAdapter extends BaseAdapter {

    private List<Bitmap> photos;
    private List<String> uris;
    private final OnClickListener itemButtonClickListener;
    private final Context context;

    public PhotoCardsAdapter(Context context, List<Bitmap> photos, List<String> uris,  OnClickListener itemButtonClickListener) {
        this.context = context;
        this.photos = photos;
        this.uris = uris;
        this.itemButtonClickListener = itemButtonClickListener;
        Log.d("debug", "card adapter initialised");
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public String getItem(int position) {
        return uris.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_photo_card, null);

            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageBitmap(photos.get(position));

        if (itemButtonClickListener != null) {
            holder.imageView.setOnClickListener(itemButtonClickListener);
        }

        return convertView;
    }

    private static class ViewHolder {
        private ImageView imageView;
    }
}
