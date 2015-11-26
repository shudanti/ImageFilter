package com.example.saturn.imagefilter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ListFilterAdapter extends BaseAdapter {
    private int[] arrImg;
    private Context context;

    public ListFilterAdapter(Context context, int[] arrImg) {
        this.arrImg = arrImg;
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrImg.length;
    }

    @Override
    public Object getItem(int arg0) {
        return arrImg[arg0];
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(final int arg0, View convertView, ViewGroup arg2) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_horizontal_listview,
                    null);
        }

        ImageView imgItem = (ImageView) convertView.findViewById(R.id.imgItem);
        imgItem.setImageResource(arrImg[arg0]);

        return convertView;
    }

}