package com.example.dave.audioshare;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

/*classname AudioListAdapater.java
date 03/07/2017
author David Gunnigan 15043754
https://firebase.google.com/docs/android/setup*/

public class AudioListAdapter extends ArrayAdapter<Audio> {

    public AudioListAdapter(@NonNull Context context, List<Audio> audios) {
        super(context, 0, audios);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Audio audio = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_items_layout, parent, false);
        }

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.fileName);
        TextView tvHome = (TextView) convertView.findViewById(R.id.createdDate);
        // Populate the data into the template view using the data object
        tvName.setText("Name: "+audio.getName());
        tvHome.setText("Created Date: "+new Date(audio.getDate()));
        // Return the completed view to render on screen
        return convertView;

    }
}
