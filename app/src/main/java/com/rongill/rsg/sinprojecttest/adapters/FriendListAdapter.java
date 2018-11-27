package com.rongill.rsg.sinprojecttest.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rongill.rsg.sinprojecttest.R;

import java.util.ArrayList;

public class FriendListAdapter extends ArrayAdapter<String> {


    private ArrayList<String> friendsUidList;
    private Context mContext;

    private static class ViewHolder{
        ImageView connectionStatus;
        TextView friendName;
    }

    public FriendListAdapter(final ArrayList<String> friendsUidList, Context context, DatabaseReference userFriendsRef){
        super(context, R.layout.friend_list_layout, friendsUidList);

        userFriendsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                friendsUidList.add(dataSnapshot.getValue().toString());
                FriendListAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        this.friendsUidList = friendsUidList;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return friendsUidList.size();
    }

    @Override
    public String getItem(int position) {
        return friendsUidList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        String friendUid = getItem(position);
        final ViewHolder viewHolder;


        if (convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.friend_list_layout, parent, false);

            viewHolder.connectionStatus = (ImageView)convertView.findViewById(R.id.status_imageView);
            viewHolder.friendName = (TextView)convertView.findViewById(R.id.friend_name);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        DatabaseReference friendUserRefByUid = FirebaseDatabase.getInstance().getReference()
                .child("users").child(friendUid).child("username");
        friendUserRefByUid.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                viewHolder.friendName.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference friendStatusRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(friendsUidList.get(position)).child("status");
        friendStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                switch (dataSnapshot.getValue().toString()){
                    case "connected":
                        viewHolder.connectionStatus.setImageResource(R.drawable.friend_connected_icon);
                        break;

                    case "disconnected":
                        viewHolder.connectionStatus.setImageResource(R.drawable.friend_disconnected_icon);
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return convertView;
    }

    public void myAddAll(ArrayList<String> friendsUidList){

        for(String s : friendsUidList) {
            super.add(s);
        }

    }


}
