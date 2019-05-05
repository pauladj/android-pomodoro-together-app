package com.example.pomodoro.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pomodoro.R;
import com.example.pomodoro.models.MessageEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Timer extends Service {

    private CountDownTimer cTimerTrabajo = null;
    private CountDownTimer cTimerDescanso = null;

    private MediaPlayer player = null;

    private int minutosTrabajo;
    private int minutosDescanso;

    private int currentSeconds;

    private long milisecondsDescansoFin;
    private long milisecondsTrabajoFin;

    private int maxMiliseconds; // milisegundos totales del countdown

    private boolean isDescanso = false;

    private String pomodoroKey; // si es null no es el dueño del pomodoro y es uno grupal

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
            if (cTimerTrabajo != null){
                cTimerTrabajo.cancel();
            }
            if (cTimerDescanso != null){
                cTimerDescanso.cancel();
            }

            if (player != null) {
                player.stop();
            }
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
        if (extras.containsKey("horaTrabajoFin")){
            // viene de un proyecto
            pomodoroKey = extras.getString("pomodoroKey");

            Date horaTrabajoFin = stringToDate(extras.getString("horaTrabajoFin"));
            Date horaDescansoFin = stringToDate(extras.getString("horaDescansoFin"));

            java.util.Date fechaActual = new java.util.Date();
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setTime(fechaActual);
            long milisecondsNow = calendar.getTimeInMillis();

            calendar.setTime(horaTrabajoFin);
            milisecondsTrabajoFin = calendar.getTimeInMillis();

            calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setTime(horaDescansoFin);
            long milisecondsDescanso = calendar.getTimeInMillis();

            milisecondsDescansoFin = milisecondsDescanso - milisecondsTrabajoFin;
            milisecondsTrabajoFin = milisecondsTrabajoFin - milisecondsNow;
            if (milisecondsTrabajoFin < 0){
                // si el trabajo ha terminado
                if ((milisecondsDescanso - milisecondsNow) < 0){
                    // el descanso ha terminado, el pomodoro ya no está activo, enviar mensaje
                    MessageEvent a = new MessageEvent("0:00", getStringToShow(),
                            100);
                    EventBus.getDefault().post(a);
                }else{
                    // el trabajo ha terminado, el descanso sigue
                    // TODO hacer, que no sea recursivo y el texto
                    isDescanso = true;
                    long mili = milisecondsDescanso - milisecondsNow;
                    startTimer(mili);
                }
            }else{
                // el trabajo no ha terminado
                isDescanso = false;
                startTimer(milisecondsTrabajoFin);
            }
        }else {
            // es individual
            minutosTrabajo = extras.getInt("minutosTrabajo");
            minutosDescanso = extras.getInt("minutosDescanso");
            isDescanso = false;
            // empezar
            startTimer(minutosTrabajo);
        }

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
     * Miliseconds to minutes
     * @param miliseconds
     * @return
     */
    private int milisecondsToMinutes(long miliseconds){
        return Integer.valueOf(String.valueOf(miliseconds/60000));
    }

    /**
     * Start timer individual
     * @param minutos - los minutos del countdown
     */
    private void startTimer(int minutos){
        currentSeconds = 60;
        EventBus.getDefault().post(new MessageEvent(String.valueOf(minutos), getStringToShow(), 0));
        int miliseconds = minutesToMiliseconds(minutos);
        maxMiliseconds = miliseconds;

        CountDownTimer cTimer = new CountDownTimer(miliseconds, 1000) {
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

                // si está configurado activar sonido cuando el pomodoro termina
                SharedPreferences prefs_especiales = getSharedPreferences(
                        "preferencias_especiales",
                        Context.MODE_PRIVATE);
                boolean sound = prefs_especiales.getBoolean("sound", false);
                if (sound){
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    player = MediaPlayer.create(getApplicationContext(), notification);
                    player.start();
                }

                // Start relax period
                if (!isDescanso){
                    isDescanso = true;
                    startTimer(minutosDescanso);
                }else{
                    // stop
                    stopSelf();
                }
            }
        };

        if(cTimerTrabajo == null){
            cTimerTrabajo = cTimer;
        }else{
            cTimerDescanso = cTimer;
        }
        cTimer.start();
    }

    /**
     * Start timer de proyecto
     * @param miliseconds - los milisegundos del countdown
     */
    private void startTimer(long miliseconds){
        EventBus.getDefault().post(new MessageEvent(String.valueOf(milisecondsToMinutes(miliseconds)),
                getStringToShow(), 0));
        maxMiliseconds = Integer.valueOf(String.valueOf(miliseconds));

        CountDownTimer cTimer = new CountDownTimer(miliseconds, 1000) {
            public void onTick(long millisUntilFinished) {
                // Cada segundo
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                String texto;
                if (minutes == 0){
                    texto = String.valueOf(seconds);
                }else{
                    String zero = "";
                    if (seconds < 10){
                        zero = "0";
                    }
                    texto = minutes + ":" + zero + seconds;
                }

                // enviar a actividad para que actualice la UI
                MessageEvent a = new MessageEvent(texto, getStringToShow(),
                        getPercentage(Integer.valueOf(String.valueOf(millisUntilFinished))));
                EventBus.getDefault().post(a);
            }
            public void onFinish() {
                // enviar a actividad para que actualice la UI
                boolean finished = false;
                if (isDescanso){
                    finished = true;
                }
                MessageEvent a = new MessageEvent("0:00", getStringToShow(),
                        100);
                EventBus.getDefault().post(a);

                // si está configurado activar sonido cuando el pomodoro termina
                SharedPreferences prefs_especiales = getSharedPreferences(
                        "preferencias_especiales",
                        Context.MODE_PRIVATE);
                boolean sound = prefs_especiales.getBoolean("sound", false);
                if (sound){
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    player = MediaPlayer.create(getApplicationContext(), notification);
                    player.start();
                }

                // Start relax period
                if (!isDescanso){
                    isDescanso = true;
                    startTimer(milisecondsDescansoFin);
                }else{
                    // stop
                    pomodoroIsFinished();
                }
            }
        };

        if(cTimerTrabajo == null){
            cTimerTrabajo = cTimer;
        }else{
            cTimerDescanso = cTimer;
        }
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


    /**
     * COnvertir string a date
     * @param time
     * @return
     */
    private Date stringToDate(String time){
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            cal.setTime(sdf.parse(time));// all done
            return cal.getTime();
        }catch (Exception e){
            return null;
        }
    }

    /**
     * El pomodoro grupal ha terminado, actualizar datos firebase
     */
    private void pomodoroIsFinished(){
        if (pomodoroKey != null){
            // no es un pomodoro individual, y este es el que lo ha creado
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProyectosPomodoro").child(pomodoroKey);
            databaseReference.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    mutableData.child("empezado").setValue(false);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    if (databaseError != null){
                        int tiempo = Toast.LENGTH_SHORT;
                        Context context = getApplicationContext();
                        Toast aviso = Toast.makeText(context, getResources().getString(R.string.error),
                                tiempo);
                        aviso.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 100);
                        aviso.show();
                        return;
                    }

                    SharedPreferences prefs_especiales = getSharedPreferences(
                            "preferencias_especiales",
                            Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor2 = prefs_especiales.edit();
                    editor2.putString(pomodoroKey, null);
                    editor2.apply();

                    int tiempo = Toast.LENGTH_SHORT;
                    Context context = getApplicationContext();
                    Toast aviso = Toast.makeText(context, getResources().getString(R.string.pomodoroStopped),
                            tiempo);
                    aviso.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 100);
                    aviso.show();

                    stopSelf();
                }
            });
        }
    }
}
