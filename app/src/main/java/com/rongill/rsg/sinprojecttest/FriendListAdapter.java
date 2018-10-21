package com.rongill.rsg.sinprojecttest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendListAdapter extends ArrayAdapter<User> {

    private ArrayList<User> friends;
    private Context mContext;

    private static class ViewHolder{
        ImageView connectionStatus;
        TextView friendName;
    }

    public FriendListAdapter(ArrayList<User> friends, Context context){
        super(context, R.layout.friend_list_layout, friends);
        this.friends = friends;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public User getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        User friend = getItem(position);
        ViewHolder viewHolder;


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

        if(friend.getUserName()!= null) viewHolder.friendName.setText(friend.getUserName());
        if(friend.isConnected()){
            viewHolder.connectionStatus.setImageResource(R.drawable.friend_connected_icon);
        } else {
            viewHolder.connectionStatus.setImageResource(R.drawable.friend_disconnected_icon);
        }

        return convertView;
    }
}
