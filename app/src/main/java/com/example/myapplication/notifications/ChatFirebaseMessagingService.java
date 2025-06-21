package com.example.myapplication.notifications;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.i("FCM", "Nowy token: " + token);
        TokenRepository.register(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage msg) {
        super.onMessageReceived(msg);
        Log.i("FCM", "ODEBRANO wiadomość FCM: " + msg.getNotification());

        if (msg.getData() != null) {
            String title = msg.getData().get("title");
            String body  = msg.getData().get("body");

            Log.d("FCM", "ODEBRANO wiadomość typu DATA: " + title + " / " + body);

            NotificationHelper.showChatNotification(this, title, body);
        }
    }
}
