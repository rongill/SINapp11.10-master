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
import com.rongill.rsg.sinprojecttest.activities.AddModifyBeaconActivity;
import com.rongill.rsg.sinprojecttest.navigation.MyBeacon;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

public class BeaconListAdapter extends ArrayAdapter<MyBeacon> {
    private ArrayList<MyBeacon> beacons;
    private Context context;

    private static class ViewHolder{
        TextView beaconNameTv, beaconFloorTv, beaconCoordinatesTv, beaconDateModifiedTv;
        Button modifyBtn, removeBtn;
    }

    public BeaconListAdapter(ArrayList<MyBeacon> beacons, Context context){
        super(context, R.layout.beacon_list_layout, beacons);
        this.beacons = beacons;
        this.context = context;
    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Nullable
    @Override
    public MyBeacon getItem(int position) {
        return beacons.size() != 0 ? beacons.get(position):null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final MyBeacon beacon = getItem(position);
        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.beacon_list_layout, parent, false);
            viewHolder.beaconNameTv = (TextView)convertView.findViewById(R.id.beacon_name_item_listView);
            viewHolder.beaconFloorTv = (TextView)convertView.findViewById(R.id.beacon_floor_indicator_item_listView);
            viewHolder.beaconCoordinatesTv = (TextView)convertView.findViewById(R.id.beacon_coordinates_item_listView);
            viewHolder.beaconDateModifiedTv = (TextView)convertView.findViewById(R.id.beacon_date_modified_item_listView);
            viewHolder.modifyBtn = (Button) convertView.findViewById(R.id.modify_beacon_button);
            viewHolder.removeBtn = (Button) convertView.findViewById(R.id.remove_beacon_button);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if(beacon != null){
            viewHolder.beaconNameTv.setText(beacon.getName());
            viewHolder.beaconFloorTv.setText(beacon.getFloor());
            viewHolder.beaconCoordinatesTv.setText(beacon.getCoordinates().toString());

            String dateModifiedString = beacon.getDateModified().getDate() + " - " + beacon.getDateModified().getTime();
            viewHolder.beaconDateModifiedTv.setText(dateModifiedString);

            //on modified pressed send the beacon to modify to AddModifyBeaconActivity
            viewHolder.modifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent modifyBeaconIntent = new Intent(context, AddModifyBeaconActivity.class);
                    modifyBeaconIntent.putExtra("BEACON_MODIFY", getItem(position));
                    context.startActivity(modifyBeaconIntent);
                }
            });

            viewHolder.removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    beacons.remove(getItem(position));
                    BeaconListAdapter.this.notifyDataSetChanged();

                    Query beaconRemoveQuery = FirebaseDatabase.getInstance().getReference()
                            .child("beacons").orderByChild("name").equalTo(beacon.getName());
                    beaconRemoveQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            DatabaseReference beaconToRemove = FirebaseDatabase.getInstance().getReference()
                                    .child("beacons");

                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                beaconToRemove.child(ds.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(context, "beacon Deleted", Toast.LENGTH_LONG).show();
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
