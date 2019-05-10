package com.example.pomodoro.utilities;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ServicioFirebase extends FirebaseMessagingService {
    public ServicioFirebase() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            // el mensaje viene con datos
            String username = remoteMessage.getData().get("username");

            SharedPreferences prefs_especiales = getSharedPreferences(
                    "preferencias_especiales",
                    Context.MODE_PRIVATE);
            String activeUser = prefs_especiales.getString("activeUser", null);
            if (activeUser == null || !username.equals(activeUser)){
                // no hacer nada
            }else{
                // logout forzoso porque alguien más ha iniciado sesión con este usuario
                // TODO
                LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getBaseContext());
                Intent intent = new Intent("forceLogout");
                broadcaster.sendBroadcast(intent);
            }


        }
        if (remoteMessage.getNotification() != null) {
            // el mensaje es una notificación

        }
    }
}
