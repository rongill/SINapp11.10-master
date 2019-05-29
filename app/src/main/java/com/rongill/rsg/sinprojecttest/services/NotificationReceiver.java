package com.rongill.rsg.sinprojecttest.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rongill.rsg.sinprojecttest.basic_objects.RequestMessage;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {

        // if the intent carry a friend uid then that means the user pressed confirm on the notification, add friend.
        if(intent.getStringExtra("CONFIRMED_MESSAGE_KEY") != null){

            RequestMessage friendRequest = (RequestMessage) intent.getSerializableExtra("FRIEND_REQUEST_MESSAGE");
            addFriend(friendRequest);

            DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference()
                    .child("users-inbox")
                    .child(friendRequest.getReceiverUid())
                    .child(intent.getStringExtra("CONFIRMED_MESSAGE_KEY"));
            messageRef.removeValue();

            intent.removeExtra("CONFIRMED_MESSAGE_KEY");
            intent.removeExtra("FRIEND_REQUEST_MESSAGE");

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(1);
        }
        // if the intent carry a message key then that means the user pressed deny on the notification, delete the message.
        else if(intent.getStringExtra("DENIED_MESSAGE_KEY") != null){
            //if the notification denied was from a friend request.
            if(intent.getSerializableExtra("FRIEND_REQUEST_MESSAGE") != null) {
                RequestMessage message = (RequestMessage) intent.getSerializableExtra("FRIEND_REQUEST_MESSAGE");
                DatabaseReference userInboxRef = FirebaseDatabase.getInstance().getReference()
                        .child("users-inbox")
                        .child(message.getReceiverUid())
                        .child(intent.getStringExtra("DENIED_MESSAGE_KEY"));
                userInboxRef.removeValue();

                intent.removeExtra("DENIED_MESSAGE_KEY");
                intent.removeExtra("FRIEND_REQUEST_MESSAGE");

                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(1);
            }

            //if the notification denied was from a dynamic navigation request, delete the message.
            if(intent.getSerializableExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE") != null){
                RequestMessage message = (RequestMessage) intent.getSerializableExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE");
                DatabaseReference userInboxRef = FirebaseDatabase.getInstance().getReference()
                        .child("users-inbox")
                        .child(message.getReceiverUid())
                        .child(intent.getStringExtra("DENIED_MESSAGE_KEY"));
                userInboxRef.removeValue();

                intent.removeExtra("DENIED_MESSAGE_KEY");
                intent.removeExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE");
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(2);
            }
        }


    }

    private void addFriend(RequestMessage message) {
        DatabaseReference currentUserFriendsRef = FirebaseDatabase.getInstance().getReference()
                .child("users-friends").child(message.getReceiverUid());
        DatabaseReference friendUserFriendsRef = FirebaseDatabase.getInstance().getReference()
                .child("users-friends").child(message.getSenderUid());
        currentUserFriendsRef.push().setValue(message.getSenderUid());
        friendUserFriendsRef.push().setValue(message.getReceiverUid());
    }

}
