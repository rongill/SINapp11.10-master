package com.rongill.rsg.sinprojecttest.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.activities.AddModifyLocationActivity;
import com.rongill.rsg.sinprojecttest.navigation.Location;

import java.util.ArrayList;

public class LocationListAdapter extends ArrayAdapter<Location> {

    private ArrayList<Location> locations;
    private Context context;

    private static class ViewHolder{
        TextView nameTv, categoryTv, beaconTv, coordinatesTv, floorTv, lastModifiedTv;
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
        return locations.size()!=0? locations.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Location location = getItem(position);
        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.location_list_layout, parent, false);

            viewHolder.nameTv = (TextView)convertView.findViewById(R.id.location_name_item_listView);
            viewHolder.categoryTv = (TextView)convertView.findViewById(R.id.location_category_item_listView);
            viewHolder.beaconTv = (TextView)convertView.findViewById(R.id.related_beacon_item_listView);
            viewHolder.coordinatesTv = (TextView)convertView.findViewById(R.id.coordinates_item_listView);
            viewHolder.floorTv = (TextView)convertView.findViewById(R.id.floor_item_listView);
            viewHolder.lastModifiedTv = (TextView) convertView.findViewById(R.id.date_modified_item_listView);
            viewHolder.modifyBtn = (Button) convertView.findViewById(R.id.modify_location_button);
            viewHolder.removeBtn = (Button) convertView.findViewById(R.id.remove_location_button);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(location != null){
            viewHolder.nameTv.setText(location.getName());
            viewHolder.categoryTv.setText(location.getCategory());
            viewHolder.beaconTv.setText(location.getBeaconName());
            viewHolder.floorTv.setText(location.getFloor());

            String coordinates = "( " + String.valueOf(location.getCoordinates().getX()) + " , " + String.valueOf(location.getCoordinates().getY() + " )");
            viewHolder.coordinatesTv.setText(coordinates);

            String lastModified = location.getDateModified().getDate() + " - " + location.getDateModified().getTime();
            viewHolder.lastModifiedTv.setText(lastModified);

            viewHolder.modifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //if modified pressed, will send to add/modifylocation popup page, sending the item to modify.
                    Intent modifyLocationIntent = new Intent(context, AddModifyLocationActivity.class);
                    modifyLocationIntent.putExtra("LOCATION_MODIFY", location);
                    context.startActivity(modifyLocationIntent);
                }
            });

            viewHolder.removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    locations.remove(location);
                    LocationListAdapter.this.notifyDataSetChanged();

                    Query locationToRemoveQuery = FirebaseDatabase.getInstance().getReference()
                            .child("structure").child(location.getStructure()).child("locations").orderByChild("name").equalTo(location.getName());
                    locationToRemoveQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            DatabaseReference locationToRemove = FirebaseDatabase.getInstance().getReference()
                                    .child("structure").child(location.getStructure()).child("locations");
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                locationToRemove.child(ds.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(context, "Location Deleted.", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

        }

        return convertView;
    }
}
