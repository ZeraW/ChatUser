package com.teleone.chatuser.PushNotifications;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.teleone.chatuser.R;


public class MyFireBaseInstanceService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        //save your token in sharedpref  (the token is String s )

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData() != null) {
            //get note title
            String title = remoteMessage.getData().get("title");
            //get note body
            String body = remoteMessage.getData().get("body");

            //display the note
            showNotification(title, body);


        }
        Log.e("dadada", "onMessageReceived: " );

    }

    private void showNotification(String title, String body) {
        new CustomNotification(getApplicationContext()).startNewNotification(title, body, R.drawable.ic_stat_msg);
    }
}
