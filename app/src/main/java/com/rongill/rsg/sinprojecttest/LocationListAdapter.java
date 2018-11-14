package com.rongill.rsg.sinprojecttest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class LocationListAdapter extends ArrayAdapter<Location> {

    private ArrayList<Location> locations;
    private Context context;

    private static class ViewHolder{
        TextView nameTv, categoryTv, beaconTv;
        Button modifyBtn, removeBtn;
    }

    public LocationListAdapter(ArrayList<Location> locations, Context context){
        super (context, R.layout.location_list_layout, locations);
        this.locations = locations;
        this.context = context;
    }

    @Override
    public int getCount() {
        return locations.size();
    }

    @Nullable
    @Override
    public Location getItem(int position) {
        return locations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Location location = getItem(position);
        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.location_list_layout, parent, false);

            viewHolder.nameTv = (TextView)convertView.findViewById(R.id.location_name_item_listView);
            viewHolder.categoryTv = (TextView)convertView.findViewById(R.id.location_category_item_listView);
            viewHolder.beaconTv = (TextView)convertView.findViewById(R.id.related_beacon_item_listView);
            viewHolder.modifyBtn = (Button) convertView.findViewById(R.id.modify_location_button);
            viewHolder.removeBtn = (Button) convertView.findViewById(R.id.remove_location_button);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(location != null){
            viewHolder.nameTv.setText(location.getName());
            viewHolder.categoryTv.setText(location.getCategory());
            viewHolder.beaconTv.setText(location.getBeacon());
            viewHolder.modifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO make an alert dialog with fields to modify the location data
                }
            });

            viewHolder.removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO remove location from list and DATABASE
                }
            });

        }

        return convertView;
    }
}
