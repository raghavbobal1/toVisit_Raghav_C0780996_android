package com.king.tovisit_raghav_c0780996_android.adapter;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.king.tovisit_raghav_c0780996_android.R;

import com.king.tovisit_raghav_c0780996_android.databaseHelper.MapsDatabase;
import com.king.tovisit_raghav_c0780996_android.helperClass.Places;

import java.util.List;

public class PlacesAdapter extends ArrayAdapter {

    Context context;
    List<Places> places;
    int layoutResource;
    MapsDatabase mDatabase;

    public PlacesAdapter(@NonNull Context context, int resource, List<Places> places, MapsDatabase mDatabase) {
        super(context, resource, places);
        this.context = context;
        this.places = places;
        this.layoutResource = resource;
        this.mDatabase = mDatabase;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layoutResource, null);

        TextView tvName = v.findViewById(R.id.name_ID);

        final Places p = places.get(position);
        tvName.setText(p.getName());

        if (p.getVisited()) {
            ImageView i = v.findViewById(R.id.placeImage);
            i.setImageResource(R.drawable.visited);
            v.setBackgroundColor(Color.rgb(125, 252,116));
        }
        return v;
    }
}

