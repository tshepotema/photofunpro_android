package com.example.photofunpro;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(360, 360));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 2, 8, 2);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // categories
    private Integer[] mThumbIds = {
        R.drawable.cat_sport, R.drawable.cat_nature,
        R.drawable.cat_people, R.drawable.cat_wildlife,
        R.drawable.cat_architecture, R.drawable.cat_technology,
        R.drawable.cat_sport, R.drawable.cat_nature,
        R.drawable.cat_people, R.drawable.cat_wildlife,
        R.drawable.cat_architecture, R.drawable.cat_technology
    };
}