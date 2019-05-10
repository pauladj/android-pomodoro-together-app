package com.example.pomodoro.utilities;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ServicioFirebase extends FirebaseMessagingService {
    public ServicioFirebase() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            // el mensaje viene con datos
            // logout forzoso porque alguien más ha iniciado sesión
            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getBaseContext());
            Intent intent = new Intent("forceLogout");
            broadcaster.sendBroadcast(intent);
        }
        if (remoteMessage.getNotification() != null) {
            // el mensaje es una notificación
        }
    }
}
