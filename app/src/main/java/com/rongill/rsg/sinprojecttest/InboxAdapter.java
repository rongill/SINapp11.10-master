package com.rongill.rsg.sinprojecttest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InboxAdapter extends ArrayAdapter<RequestMessage> {
    private ArrayList<RequestMessage> inbox;
    private Context mContext;

    private static class ViewHolder{
        TextView message;
        Button comfirmBtn, denyBtn;
    }

    public InboxAdapter(ArrayList<RequestMessage> inbox, Context context){
        super(context,R.layout.inbox_list_layout, inbox);
        this.inbox = inbox;
    }

    @Override
    public int getCount() {
        return inbox.size();
    }

    @Override
    public RequestMessage getItem(int position){
       return inbox.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        RequestMessage message = getItem(position);
        ViewHolder viewHolder;

        if(convertView == null){

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.inbox_list_layout, parent, false);

            viewHolder.message = (TextView)convertView.findViewById(R.id.request_message_TV);
            viewHolder.comfirmBtn = (Button)convertView.findViewById(R.id.confirm_request_btn);
            viewHolder.denyBtn = (Button)convertView.findViewById(R.id.deny_request_btn);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(message.getRequestType()!=null){
            switch (message.getRequestType()){
                case "friend request":
                    String text = message.getSenderUsername() + " has sent you a friend request";
                    viewHolder.message.setText(text);
                    break;

                    default: //TODO other requests.

            }
        }

        viewHolder.comfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriendInDatabase(position);
            }
        });
        viewHolder.denyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            denyRequestInDB(position);
            }
        });

        return convertView;
    }

    private void addFriendInDatabase(int position){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userUid  = mAuth.getCurrentUser().getUid();

        DatabaseReference requestMessageRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(userUid).child(getItem(position).getFriendUid()).child("request-status");
        requestMessageRef.setValue("confirmed");

        final DatabaseReference userFriendRef = FirebaseDatabase.getInstance().getReference()
                .child("users-friends").child(mAuth.getUid()).child(inbox.get(position).getFriendUid());
        Map<String,String> newPost = new HashMap<>();
        newPost.put("status","disconnected");
        userFriendRef.setValue(newPost);

        //TODO check why listener for statuse not work properly (probably because the listener is defined here, and not in the friend list corresponding activity)
        DatabaseReference statusUpdaterRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(inbox.get(position).getFriendUid()).child("status");
        statusUpdaterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userFriendRef.child("status").setValue(dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void denyRequestInDB(int position){
        String userUid  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference requestMessageRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(userUid).child(getItem(position).getFriendUid()).child("request-status");
        requestMessageRef.setValue("denied");
    }
}
