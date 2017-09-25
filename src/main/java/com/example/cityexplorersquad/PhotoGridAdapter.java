package com.example.cityexplorersquad;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.List;

public class PhotoGridAdapter extends ArrayAdapter<Bitmap> {


    //private final ColorMatrixColorFilter grayscaleFilter;
    private List<Bitmap> photos;
    private List<String> uris;
    private final View.OnClickListener itemButtonClickListener;
    private Context context;

    public PhotoGridAdapter(Context context, List<Bitmap> photos, List<String> uris, View.OnClickListener itemButtonClickListener) {
        super(context, R.layout.list_photo_card, photos);
        this.context = context;
        this.photos = photos;
        this.uris = uris;
        this.itemButtonClickListener = itemButtonClickListener;
    }

    public String getPhotoUri(int position) {
        return uris.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            row = LayoutInflater.from(context).inflate(R.layout.list_photo_card, null);
            holder = new ViewHolder();
            holder.imageButton = (ImageButton) row.findViewById(R.id.imageButton);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.imageButton.setImageBitmap(photos.get(position));

        if (itemButtonClickListener != null) {
            holder.imageButton.setOnClickListener(itemButtonClickListener);
        }

        return row;
    }

    static class ViewHolder {
        ImageButton imageButton;
    }
}
