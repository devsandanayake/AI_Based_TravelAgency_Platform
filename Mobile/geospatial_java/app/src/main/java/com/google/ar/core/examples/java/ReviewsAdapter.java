package com.google.ar.core.examples.java;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.BaseAdapter;

import com.google.ar.core.examples.java.geospatial.R;

import java.util.List;

public class ReviewsAdapter extends BaseAdapter {
    private Context context;
    private List<String> reviewsList;

    public ReviewsAdapter(Context context, List<String> reviewsList) {
        this.context = context;
        this.reviewsList = reviewsList;
    }

    @Override
    public int getCount() {
        return reviewsList.size();
    }

    @Override
    public Object getItem(int position) {
        return reviewsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        }

        TextView reviewText = convertView.findViewById(R.id.reviewText);
        reviewText.setText(reviewsList.get(position));

        return convertView;
    }
}
