package com.example.pomodoro.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.pomodoro.R;

public class Timer extends Service {

    private CountDownTimer cTimer = null;

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }


    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        Bundle extras = i.getExtras();
        if (extras == null){
            // si no hay parámetros
            stopSelf();
            return START_NOT_STICKY;
        }

        // generar notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager elmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel canalservicio = new NotificationChannel("IdCanal",
                    "NombreCanal",NotificationManager.IMPORTANCE_DEFAULT);
            elmanager.createNotificationChannel(canalservicio);
            Notification.Builder builder = new Notification.Builder(this, "IdCanal")
                    .setContentTitle(getString(R.string.app_name))
                    .setAutoCancel(false);
            Notification notification = builder.build();
            startForeground(1, notification);
        }

        // recuperar valores
        int minutosTrabajo = extras.getInt("minutosTrabajo");
        int minutosDescanso = extras.getInt("minutosDescanso");

        minutosLeft = -1;
        ((TextView) findViewById(R.id.textView)).setText("minutes remaining: " + minutosTrabajo);
        cTimer = new CountDownTimer(miliSeconds, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = Integer.valueOf(String.valueOf(millisUntilFinished /1000));
                minutosLeft = seconds/60;

                ((TextView) findViewById(R.id.textView)).setText("minutes remaining: " + seconds);
            }
            public void onFinish() {
                ((TextView) findViewById(R.id.textView)).setText("done!");
                startTimer(minutesToMiliseconds(minutosDescanso));
            }
        };
        cTimer.start();

        return START_NOT_STICKY;
    }

    /**
     * Minutes to miliseconds converter
     * @return
     */
    private int minutesToMiliseconds(int minutes){
        return minutes * 60000;
    }
}
