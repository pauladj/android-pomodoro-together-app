package com.example.pomodoro.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.pomodoro.R;
import com.example.pomodoro.models.MessageEvent;

import org.greenrobot.eventbus.EventBus;

public class Timer extends Service {

    private CountDownTimer cTimer = null;
    private int minutosTrabajo;
    private int minutosDescanso;

    private int currentSeconds;
    private int maxMiliseconds; // milisegundos totales del countdown

    private boolean isDescanso = false;

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

        if (extras.containsKey("stop")){
            // parar el servicio y el timer
            cTimer.cancel();
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
        minutosTrabajo = extras.getInt("minutosTrabajo");
        minutosDescanso = extras.getInt("minutosDescanso");

        // empezar
        startTimer(minutosTrabajo);

        return START_NOT_STICKY;
    }

    /**
     * Minutes to miliseconds converter
     * @return
     */
    private int minutesToMiliseconds(int minutes){
        return minutes * 60000;
    }

    /**
     * Start timer
     * @param minutos - los minutos del countdown
     */
    private void startTimer(int minutos){
        currentSeconds = 60;
        EventBus.getDefault().post(new MessageEvent(String.valueOf(minutos), getStringToShow(), 0));
        int miliseconds = minutesToMiliseconds(minutos);
        maxMiliseconds = miliseconds;

        cTimer = new CountDownTimer(miliseconds, 1000) {
            public void onTick(long millisUntilFinished) {
                // Cada segundo
                int seconds = Integer.valueOf(String.valueOf(millisUntilFinished /1000));
                int minutosLeft = seconds/60;

                currentSeconds--;

                String zero= "";
                if (currentSeconds < 10){
                    zero = "0";
                }
                String texto = minutosLeft + ":" + zero + currentSeconds;

                if (currentSeconds == 0){
                    currentSeconds = 60;
                }

                // enviar a actividad para que actualice la UI
                MessageEvent a = new MessageEvent(texto, getStringToShow(),
                        getPercentage(Integer.valueOf(String.valueOf(millisUntilFinished))));
                EventBus.getDefault().post(a);
            }
            public void onFinish() {
                // enviar a actividad para que actualice la UI
                MessageEvent a = new MessageEvent("0:00", getStringToShow(),
                        100);
                EventBus.getDefault().post(a);

                // Start relax period
                if (!isDescanso){
                    isDescanso = true;
                    startTimer(minutosDescanso);
                }
                // stop
                stopSelf();
            }
        };
        cTimer.start();
    }

    /**
     * Get id of the string to show
     * @return - the id of the string to show
     */
    private int getStringToShow(){
        int stringId;
        if (isDescanso){
            stringId = R.string.descansar;
        }else{
            stringId = R.string.work;
        }
        return stringId;
    }

    /**
     * Transformar milisegundos en porcentaje
     * @param milisecondsLeft
     * @return
     */
    private int getPercentage(int milisecondsLeft){
        int miliSegundosPasados = maxMiliseconds - milisecondsLeft;
        return (miliSegundosPasados * 100) / maxMiliseconds;
    }

}
