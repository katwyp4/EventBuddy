package com.example.myapplication.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.R;
import com.example.myapplication.chat.ChatActivity;


public final class NotificationHelper {

    private static final String CHANNEL_ID   = "chat_messages";
    private static final String CHANNEL_NAME = "Wiadomości czatu";

    private NotificationHelper() { }

    private static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Powiadomienia o nowych wiadomościach");
            ch.enableVibration(true);
            ch.enableLights(true);
            nm.createNotificationChannel(ch);
        }
    }

    /** Wywołuj z FirebaseMessagingService. */
    public static void showChatNotification(Context ctx, String title, String body) {
        ensureChannel(ctx);

        PendingIntent tap = PendingIntent.getActivity(
                ctx, 0,
                new Intent(ctx, ChatActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(tap);

        NotificationManagerCompat.from(ctx)
                .notify((int) System.currentTimeMillis(), nb.build());
    }
}
