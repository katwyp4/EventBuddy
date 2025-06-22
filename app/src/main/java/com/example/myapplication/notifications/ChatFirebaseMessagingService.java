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
            String type  = msg.getData().get("type");
            String title = msg.getData().get("title");
            String body  = msg.getData().get("body");

            Log.d("FCM", "ODEBRANO wiadomość typu DATA: " + type + " / " + title + " / " + body);
                String eventIdStr = msg.getData().get("eventId");
                Log.d("FCM2", eventIdStr);
                if (title != null && body != null && eventIdStr != null) {
                    try {
                        long eventId = Long.parseLong(eventIdStr);
                        if ("event".equals(type)) {
                            NotificationHelper.showEventNotification(this, title, body);
                        }
                        else {
                            NotificationHelper.showChatNotification(this, title, body, eventId);
                        }
                    } catch (NumberFormatException e) {
                    Log.e("FCM", "Nieprawidłowy eventId: " + eventIdStr);
                    }
            }
        }
    }
}
