package com.example.myapplication.notifications;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChatFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.i("FCM", "Nowy token: " + token);
        TokenRepository.register(token);          // zapisz + wyślij do backendu
    }

    @Override
    public void onMessageReceived(RemoteMessage msg) {
        super.onMessageReceived(msg);

        if (msg.getNotification() != null) {
            String title = msg.getNotification().getTitle() != null
                    ? msg.getNotification().getTitle() : "EventBuddy";
            String body  = msg.getNotification().getBody()  != null
                    ? msg.getNotification().getBody()  : "";

            NotificationHelper.showChatNotification(this, title, body);
        }
    }
}
