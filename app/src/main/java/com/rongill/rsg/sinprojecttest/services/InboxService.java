package com.rongill.rsg.sinprojecttest.services;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rongill.rsg.sinprojecttest.R;
import com.rongill.rsg.sinprojecttest.activities.FriendProfileActivity;
import com.rongill.rsg.sinprojecttest.activities.MainDrowerActivity;
import com.rongill.rsg.sinprojecttest.basic_objects.MyCalendar;
import com.rongill.rsg.sinprojecttest.basic_objects.RequestMessage;
import com.rongill.rsg.sinprojecttest.basic_objects.User;

public class InboxService extends Service {

    private final long POKE_MAX_TIME_IN_SECONDS = 24*60*60;
    private final long FRIEND_REQUEST_MAX_TIME_IN_SECONDS = 7*24*60*60;
    private final long DYNAMIC_NAV_MAX_TIME_IN_SECONDS = 60*60;

    private DatabaseReference userInboxRef;
    private User currentUser;


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        currentUser = (User)intent.getSerializableExtra("CURRENT_USER");

        userInboxRef = FirebaseDatabase.getInstance().getReference()
                .child("users-inbox").child(currentUser.getUserId());

        setInboxAndListener();

        return super.onStartCommand(intent, flags, startId);
    }

    //method to handle the notification inbox system service.
    private void setInboxAndListener(){

        userInboxRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                RequestMessage tempMessage = dataSnapshot.getValue(RequestMessage.class);

                switch (tempMessage.getRequestType()){
                    case "poke":
                        if(checkMessageAge(POKE_MAX_TIME_IN_SECONDS, tempMessage, dataSnapshot.getKey())){
                            Intent pokeIntent = new Intent(getBaseContext(), FriendProfileActivity.class);
                            pokeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            //put extras for the friendprofile page to receive and init the page accordingly
                            pokeIntent.putExtra("FRIEND_UID", tempMessage.getSenderUid());
                            pokeIntent.putExtra("CURRENT_USER", currentUser);
                            pokeIntent.putExtra("KEY_FROM_POKE_MESSAGE", dataSnapshot.getKey());

                            // build the stack so that the launch of the profile page will be defined as a child of main activity.
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getBaseContext());
                            stackBuilder.addNextIntentWithParentStack(pokeIntent);

                            PendingIntent pi = PendingIntent.getActivity(getBaseContext(), 0, pokeIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(),
                                    "SIN")
                                    .setContentTitle("Friendly Poke")
                                    .setContentText(tempMessage.getSenderUsername() + " poked you! Tap to launch " + tempMessage.getSenderUsername() + " profile page")
                                    .setSmallIcon(R.drawable.sinicon)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setContentIntent(pi)
                                    .setAutoCancel(true);

                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getBaseContext());
                            notificationManagerCompat.notify(0, notificationBuilder.build());

                        }
                        break;

                    case "friend request":
                        if(checkMessageAge(FRIEND_REQUEST_MAX_TIME_IN_SECONDS, tempMessage, dataSnapshot.getKey())) {

                            Intent requestConfirmedIntent = new Intent(getBaseContext(), NotificationReceiver.class);
                            // if confirmed we put the friend UID in the extra to add to the users friend list in the receiver
                            requestConfirmedIntent.putExtra("CONFIRMED_MESSAGE_KEY", dataSnapshot.getKey());
                            requestConfirmedIntent.putExtra("FRIEND_REQUEST_MESSAGE", tempMessage);
                            PendingIntent actionConfirmed = PendingIntent.getBroadcast(getBaseContext(), 0, requestConfirmedIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                            Intent requestDeniedIntent = new Intent(getBaseContext(), NotificationReceiver.class);
                            // putting the data key in the intent extra for the receiver to delete.
                            requestDeniedIntent.putExtra("DENIED_MESSAGE_KEY", dataSnapshot.getKey());
                            requestDeniedIntent.putExtra("FRIEND_REQUEST_MESSAGE", tempMessage);
                            PendingIntent actionDenied = PendingIntent.getBroadcast(getBaseContext(), 0, requestConfirmedIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);

                            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(),
                                    "SIN")
                                    .setContentTitle("Friend Request")
                                    .setContentText("You have a friend request from " + tempMessage.getSenderUsername() +".")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setSmallIcon(R.drawable.sinicon)
                                    .addAction(R.drawable.sinicon, "CONFIRM", actionConfirmed)
                                    .addAction(R.drawable.sinicon, "DENY", actionDenied)
                                    .setAutoCancel(true);
                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getBaseContext());
                            notificationManagerCompat.notify(1, notificationBuilder.build());

                        }
                        break;
                    case "dynamic-navigation-request":
                        if(checkMessageAge(DYNAMIC_NAV_MAX_TIME_IN_SECONDS, tempMessage, dataSnapshot.getKey())) {

                            //Navigation confirmed intent, move to main activity and start navigating.
                            Intent dynamicNavRequestConfirmed = new Intent(getBaseContext(), MainDrowerActivity.class);
                            dynamicNavRequestConfirmed.putExtra("CONFIRMED_MESSAGE_KEY",dataSnapshot.getKey());
                            dynamicNavRequestConfirmed.putExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE", tempMessage);
                            dynamicNavRequestConfirmed.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            //Navigation denied intent, send to notification receiver and delete the message.
                            Intent dynamicNavDeniedIntent = new Intent(getBaseContext(), NotificationReceiver.class);
                            dynamicNavDeniedIntent.putExtra("DENIED_MESSAGE_KEY", dataSnapshot.getKey());
                            dynamicNavDeniedIntent.putExtra("DYNAMIC_NAVIGATION_REQUEST_MESSAGE", tempMessage);

                            //setting the action on the buttons of the notifications.
                            PendingIntent actionConfirmed = PendingIntent.getActivity(getBaseContext(), 0,
                                    dynamicNavRequestConfirmed, PendingIntent.FLAG_UPDATE_CURRENT);
                            PendingIntent actionDenied = PendingIntent.getActivity(getBaseContext(), 0,
                                    dynamicNavDeniedIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(), "SIN")
                                    .setContentTitle("Navigate to friend")
                                    .setContentText(tempMessage.getSenderUsername() + " wants to start navigate.")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setSmallIcon(R.drawable.sinicon)
                                    .addAction(R.drawable.sinicon,"START",actionConfirmed)
                                    .addAction(R.drawable.sinicon,"DENY",actionDenied)
                                    .setAutoCancel(true);
                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getBaseContext());
                            notificationManagerCompat.notify(2, notificationBuilder.build());

                        }
                        break;
                }


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
    }

    //if time diff is lower then max, return true/false.
    private boolean checkMessageAge(long maxMessageAge, RequestMessage message, String messageKey){
        if(message.getDateCreated().timeDiffInSeconds(new MyCalendar()) > maxMessageAge) {
            userInboxRef.child(messageKey).removeValue();
            return false;
        }
        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
