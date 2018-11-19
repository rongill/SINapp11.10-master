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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

public class InboxAdapter extends ArrayAdapter<RequestMessage> {
    private ArrayList<RequestMessage> mInbox = new ArrayList<>();
    private DatabaseReference userInboxRef;
    private Context mContext;

    private static class ViewHolder{
        TextView message;
        Button comfirmBtn, denyBtn, pokeBackBtn;
    }

    public InboxAdapter(final ArrayList<RequestMessage> inbox, DatabaseReference userInboxRef, Context context){
        super(context,R.layout.inbox_list_layout, inbox);
        this.mContext = context;
        this.userInboxRef = userInboxRef;

        userInboxRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    inbox.add(ds.getValue(RequestMessage.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        this.mInbox = inbox;
    }

    @Override
    public int getCount() {
        return mInbox.size();
    }

    @Override
    public RequestMessage getItem(int position){
       return mInbox.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final RequestMessage message = getItem(position);
        final ViewHolder viewHolder;
        InboxAdapter.this.notifyDataSetChanged();

        if(convertView == null){

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.inbox_list_layout, parent, false);

            viewHolder.message = (TextView)convertView.findViewById(R.id.request_message_TV);
            viewHolder.comfirmBtn = (Button)convertView.findViewById(R.id.confirm_request_btn);
            viewHolder.denyBtn = (Button)convertView.findViewById(R.id.deny_request_btn);
            viewHolder.pokeBackBtn = (Button)convertView.findViewById(R.id.poke_back_btn);

            //set view according to request type
            switch (message.getRequestType()){
                case "friend request":
                    ViewGroup confirmDenyButtonLayout = (ViewGroup)convertView.findViewById(R.id.confirm_deny_inbox_layout);
                    confirmDenyButtonLayout.setVisibility(View.VISIBLE);
                    break;

                //TODO some devices show 2 lines for this request (poke) for some reason.. debug
                case "poke":
                    ViewGroup pokeBackButtonLayout = (ViewGroup)convertView.findViewById(R.id.poke_back_layout);
                    pokeBackButtonLayout.setVisibility(View.VISIBLE);
                    break;
            }

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // set request message text according to request type, poke back will show only text and delete request from db
        if(message.getRequestType()!=null){
            switch (message.getRequestType()){
                case "friend request":
                    String friendRequestText = message.getSenderUsername() + " has sent you a friend request";
                    viewHolder.message.setText(friendRequestText);
                    break;

                case "poke":
                    String pokeRequestText = "You have been poked by " + message.getSenderUsername() + " ,poke back?";
                    viewHolder.message.setText(pokeRequestText);
                    break;

                case "poke back":
                    String pokeBackText = message.getSenderUsername()+" poked you back!";
                    viewHolder.message.setText(pokeBackText);
                    Toast.makeText(mContext, pokeBackText, Toast.LENGTH_LONG).show();
                    deleteRequest(position, "poke back");
                    break;

                    default:
                        //TODO handle other requests

            }
        }

        // set the confirmBtn functionality, will apply for friend requests and navigation requests.
        viewHolder.comfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (message.getRequestType()) {
                    case "friend request":

                        DatabaseReference currentUserFriendsRef = FirebaseDatabase.getInstance().getReference()
                                .child("users-friends").child(message.getReceiverUid());
                        DatabaseReference friendUserFriendsRef = FirebaseDatabase.getInstance().getReference()
                                .child("users-friends").child(message.getSenderUid());

                        currentUserFriendsRef.push().setValue(message.getSenderUid());
                        friendUserFriendsRef.push().setValue(message.getReceiverUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(mContext, message.getSenderUsername() + " has been added to your friend list", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        viewHolder.comfirmBtn.setVisibility(GONE);
                        viewHolder.denyBtn.setVisibility(GONE);
                        //TODO make the text change when pressed, currently not changing, added notifyDataSetChanged, check if works
                        String confirmedText = "friend request from " + message.getSenderUsername() + " confirmed";
                        viewHolder.message.setText(confirmedText);
                        InboxAdapter.this.notifyDataSetChanged();

                        deleteRequest(position, message.getRequestType());




                }
            }
        });
        viewHolder.denyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.comfirmBtn.setVisibility(GONE);
                viewHolder.denyBtn.setVisibility(GONE);
                String denyText = "friend request from " + message.getSenderUsername() + " denied";
                viewHolder.message.setText(denyText);
                InboxAdapter.this.notifyDataSetChanged();
                deleteRequest(position, message.getRequestType());
            }
        });

        viewHolder.pokeBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.pokeBackBtn.setVisibility(GONE);
                String pokeBackText = "Poke back to " + message.getSenderUsername() + " sent";
                viewHolder.message.setText(pokeBackText);
                pokeBack(message);
                deleteRequest(position,message.getRequestType());
            }
        });



        return convertView;
    }




    private void deleteRequest(final int position, final String requestType){


        mInbox.remove(position);
        InboxAdapter.this.notifyDataSetChanged();

        Query query = userInboxRef.orderByChild("requestType").equalTo(requestType);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for (DataSnapshot ds : dataSnapshot.getChildren()) {
                   if (ds.child("senderUid").getValue().toString().equals(getItem(position).getSenderUid()))
                       userInboxRef.child(ds.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful()){
                                   makeToastMessage("message deleted.");

                               }
                           }
                       });
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

    }

    private void makeToastMessage(String message){
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    private void pokeBack(final RequestMessage message){
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(message.getReceiverUid()).child("username");
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                RequestMessage newMessage = new RequestMessage(message.getSenderUid(), message.getReceiverUid()
                , dataSnapshot.getValue().toString(), "poke back","pending");
                DatabaseReference friendInboxRef = FirebaseDatabase.getInstance().getReference()
                        .child("users-inbox").child(newMessage.getReceiverUid());
                friendInboxRef.push().setValue(newMessage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
