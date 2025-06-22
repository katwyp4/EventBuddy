package com.kompetencyjny.EventBuddySpring.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    public void sendPushNotification(String token, String title, String body, String type, Long eventId) {
        Message message = Message.builder()
                .setToken(token)
                .putData("title", title)
                .putData("type", type)
                .putData("body", body)
                .putData("eventId", String.valueOf(eventId))
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Wysłano powiadomienie: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
