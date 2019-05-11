package com.example.pomodoro.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.widget.Toast;

import com.example.pomodoro.CountDownTimerActivity;
import com.example.pomodoro.ProyectosActivity;
import com.example.pomodoro.R;
import com.example.pomodoro.models.MessageEvent;
import com.example.pomodoro.utilities.Common;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Timer extends Service {

    private final int NOTIFICACION_POMODORO_CODE = 100;
    private final String NOTIFICACION_CHANNEL_ID = "101";

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

    private boolean internet; // si hay internet

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }


    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        Bundle extras = i.getExtras();
        if (extras == null) {
            // si no hay parámetros
            stopSelf();
            return START_NOT_STICKY;
        }

        if (extras.containsKey("stop")) {
            // parar el servicio y el timer
            if (cTimerTrabajo != null) {
                cTimerTrabajo.cancel();
            }
            if (cTimerDescanso != null) {
                cTimerDescanso.cancel();
            }

            if (player != null) {
                player.stop();
            }
            borrarNotificacion();
            stopSelf();
            return START_NOT_STICKY;
        }

        // generar notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager elmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel canalservicio = new NotificationChannel("IdCanal",
                    "Pomodoros", NotificationManager.IMPORTANCE_DEFAULT);
            elmanager.createNotificationChannel(canalservicio);
            Notification.Builder builder = new Notification.Builder(this, "IdCanal")
                    .setContentTitle(getString(R.string.app_name))
                    .setAutoCancel(false);
            Notification notification = builder.build();
            startForeground(1, notification);
        }

        // recuperar valores
        if (extras.containsKey("horaTrabajoFin")) {
            // viene de un proyecto
            pomodoroKey = extras.getString("pomodoroKey");

            Date horaTrabajoFin = stringToDate(extras.getString("horaTrabajoFin"));
            Date horaDescansoFin = stringToDate(extras.getString("horaDescansoFin"));

            Calendar calendar = Calendar.getInstance(Locale.US);
            calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            long milisecondsNow = calendar.getTimeInMillis();

            calendar.setTime(horaTrabajoFin);
            milisecondsTrabajoFin = calendar.getTimeInMillis();

            calendar = Calendar.getInstance(Locale.US);
            calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            calendar.setTime(horaDescansoFin);
            long milisecondsDescanso = calendar.getTimeInMillis();

            milisecondsDescansoFin = milisecondsDescanso - milisecondsTrabajoFin;
            milisecondsTrabajoFin = milisecondsTrabajoFin - milisecondsNow;
            if (milisecondsTrabajoFin < 0) {
                // si el trabajo ha terminado
                if ((milisecondsDescanso - milisecondsNow) < 0) {
                    // el descanso ha terminado, el pomodoro ya no está activo, enviar mensaje
                    MessageEvent a = new MessageEvent("0:00", getStringToShow(),
                            100);
                    EventBus.getDefault().post(a);
                } else {
                    // el trabajo ha terminado, el descanso sigue
                    isDescanso = true;
                    long mili = milisecondsDescanso - milisecondsNow;
                    lanzarNotificacion();
                    startTimer(mili);
                }
            } else {
                // el trabajo no ha terminado
                isDescanso = false;
                lanzarNotificacion();
                startTimer(milisecondsTrabajoFin);
            }
        } else {
            // es individual
            minutosTrabajo = extras.getInt("minutosTrabajo");
            minutosDescanso = extras.getInt("minutosDescanso");
            isDescanso = false;
            // empezar
            lanzarNotificacion();
            startTimer(minutosTrabajo);
        }

        return START_NOT_STICKY;
    }

    /**
     * Minutes to miliseconds converter
     *
     * @return
     */
    private int minutesToMiliseconds(int minutes) {
        return minutes * 60000;
    }

    /**
     * Miliseconds to minutes
     *
     * @param miliseconds
     * @return
     */
    private int milisecondsToMinutes(long miliseconds) {
        return Integer.valueOf(String.valueOf(miliseconds / 60000));
    }

    /**
     * Start timer individual
     *
     * @param minutos - los minutos del countdown
     */
    private void startTimer(int minutos) {
        currentSeconds = 60;
        EventBus.getDefault().post(new MessageEvent(String.valueOf(minutos), getStringToShow(), 0));
        int miliseconds = minutesToMiliseconds(minutos);
        maxMiliseconds = miliseconds;

        CountDownTimer cTimer = new CountDownTimer(miliseconds, 1000) {
            public void onTick(long millisUntilFinished) {
                // Cada segundo
                int seconds = Integer.valueOf(String.valueOf(millisUntilFinished / 1000));
                int minutosLeft = seconds / 60;

                currentSeconds--;

                String zero = "";
                if (currentSeconds < 10) {
                    zero = "0";
                }
                String texto = minutosLeft + ":" + zero + currentSeconds;

                if (currentSeconds == 0) {
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
                if (sound) {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    player = MediaPlayer.create(getApplicationContext(), notification);
                    player.start();
                }

                // Start relax period
                if (!isDescanso) {
                    isDescanso = true;
                    lanzarNotificacion();
                    startTimer(minutosDescanso);
                } else {
                    // stop
                    prefs_especiales = getSharedPreferences(
                            "preferencias_especiales",
                            Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor2 = prefs_especiales.edit();
                    editor2.putBoolean("individual", false);
                    editor2.apply();

                    borrarNotificacion();
                    stopSelf();
                }
            }
        };

        if (cTimerTrabajo == null) {
            cTimerTrabajo = cTimer;
        } else {
            cTimerDescanso = cTimer;
        }
        cTimer.start();
    }

    /**
     * Start timer de proyecto
     *
     * @param miliseconds - los milisegundos del countdown
     */
    private void startTimer(long miliseconds) {
        EventBus.getDefault().post(new MessageEvent(String.valueOf(milisecondsToMinutes(miliseconds)),
                getStringToShow(), 0));
        maxMiliseconds = Integer.valueOf(String.valueOf(miliseconds));

        CountDownTimer cTimer = new CountDownTimer(miliseconds, 1000) {
            public void onTick(long millisUntilFinished) {
                // Cada segundo
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                String texto;

                String zero = "";
                if (seconds < 10) {
                    zero = "0";
                }
                texto = minutes + ":" + zero + seconds;


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
                if (sound) {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    player = MediaPlayer.create(getApplicationContext(), notification);
                    player.start();
                }

                // Start relax period
                if (!isDescanso) {
                    isDescanso = true;
                    lanzarNotificacion();
                    startTimer(milisecondsDescansoFin);
                } else {
                    // stop
                    pomodoroIsFinished();
                }
            }
        };

        if (cTimerTrabajo == null) {
            cTimerTrabajo = cTimer;
        } else {
            cTimerDescanso = cTimer;
        }
        cTimer.start();
    }

    /**
     * Get id of the string to show
     *
     * @return - the id of the string to show
     */
    private int getStringToShow() {
        int stringId;
        if (isDescanso) {
            stringId = R.string.descansar;
        } else {
            stringId = R.string.work;
        }
        return stringId;
    }

    /**
     * Transformar milisegundos en porcentaje
     *
     * @param milisecondsLeft
     * @return
     */
    private int getPercentage(int milisecondsLeft) {
        int miliSegundosPasados = maxMiliseconds - milisecondsLeft;
        return (miliSegundosPasados * 100) / maxMiliseconds;
    }


    /**
     * COnvertir string a date
     *
     * @param time
     * @return
     */
    private Date stringToDate(String time) {
        try {
            // TODO recoger este null
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
            cal.setTime(sdf.parse(time));// all done
            return cal.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * El pomodoro grupal ha terminado, actualizar datos firebase
     */
    private void pomodoroIsFinished() {
        borrarNotificacion();
        if (pomodoroKey != null) {
            // no es un pomodoro individual, y este es el que lo ha creado
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProyectosPomodoro").child(pomodoroKey);
            if (!isNetworkAvailable()) {
                // No hay internet
                int tiempo = Toast.LENGTH_SHORT;
                Context context = getApplicationContext();
                Toast aviso = Toast.makeText(context, getResources().getString(R.string.internetNeeded),
                        tiempo);
                aviso.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 100);
                aviso.show();
            }
            databaseReference.child("empezado").setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
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

                    stopChat();

                    stopSelf();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int tiempo = Toast.LENGTH_SHORT;
                    Context context = getApplicationContext();
                    Toast aviso = Toast.makeText(context, getResources().getString(R.string.error),
                            tiempo);
                    aviso.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 100);
                    aviso.show();
                }
            });
        } else {
            // si es un pomodoro de un proyecto y no es el dueño pararlo
            stopChat();
            stopSelf();
        }
    }

    /**
     * Enviar broadcast para cerrar el chat
     */
    private void stopChat() {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getBaseContext());
        Intent intent = new Intent("stopChat");
        broadcaster.sendBroadcast(intent);
    }

    /**
     * Comprueba si está conectado a internet
     *
     * @return Extraído de Stack Overflow
     * Pregunta: https://stackoverflow.com/q/32547006/11002531
     * Autor: https://stackoverflow.com/users/546717/kyleed
     */
    public boolean isNetworkAvailable() {
        Context context = this;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to mobile data
                return true;
            }
        } else {
            // not connected to the internet
            return false;
        }
        return false;
    }

    /**
     * Borrar la notificación actual
     */
    private void borrarNotificacion() {
        NotificationManager elManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        elManager.cancel(NOTIFICACION_POMODORO_CODE);
    }

    /**
     * Lanza una nueva notificación
     */
    private void lanzarNotificacion() {


        int stringOfNotifications = R.string.timeToRelax;
        if (getStringToShow() == R.string.work){
            stringOfNotifications = R.string.timeToWork;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICACION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_clock)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(stringOfNotifications))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.activePomodoro);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICACION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // mostrar notificación
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICACION_POMODORO_CODE, builder.build());

    }
}
