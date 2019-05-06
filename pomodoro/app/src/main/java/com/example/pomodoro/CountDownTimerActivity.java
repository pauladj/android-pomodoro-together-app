package com.example.pomodoro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pomodoro.models.MessageEvent;
import com.example.pomodoro.models.Pomodoro;
import com.example.pomodoro.services.Timer;
import com.example.pomodoro.utilities.MainToolbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.triggertrap.seekarc.SeekArc;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CountDownTimerActivity extends MainToolbar {

    private SeekArc seekArc;
    private DatabaseReference databaseReference;
    private ChildEventListener listener;

    private String pomodoroKey; // si es de un proyecto & es el creador

    private String key; // si es de un proyecto y no es el creador

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("pomodoroKey", pomodoroKey);
        outState.putString("key", key);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("pomodoroKey")){
            pomodoroKey = savedInstanceState.getString("pomodoroKey", null);
        }
        if (savedInstanceState.containsKey("key")){
            pomodoroKey = savedInstanceState.getString("key", null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down_timer);

        seekArc = findViewById(R.id.seekArc);

        // actualizar texto: work / relax
        // actualizar remaining time
        ((TextView) findViewById(R.id.seekArcProgress)).setText("0:00");
        // seekarc
        seekArc.setProgress(100);

        if (savedInstanceState == null){
            // primera vez
            Intent i = getIntent();

            pomodoroKey = getStringPreference("pomodoroKey"); // es null si el pomodoro no es de
            // un proyecto y si no lo ha puesto en marcha el usuario

            if (i.hasExtra("key")){
                // si no es null el pomodoro es de un proyecto pero no lo ha puesto en marcha
                // este usuario
                key = i.getStringExtra("key");
            }

            boolean empezarNuevo = i.getBooleanExtra("nuevo", true);

            if (empezarNuevo && !servicioEnMarcha(Timer.class)){
                if (i.hasExtra("horaTrabajoFin")){
                    // Si se está iniciando un pomodoro de un proyecto, inicializar timer servicio
                    Intent e = new Intent(this, Timer.class);
                    e.putExtra("horaTrabajoFin", i.getStringExtra("horaTrabajoFin"));
                    e.putExtra("horaDescansoFin", i.getStringExtra("horaDescansoFin"));
                    e.putExtra("pomodoroKey", pomodoroKey);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(e);
                    } else {
                        startService(e);
                    }
                }else{
                    // es un pomodoro individual
                    // si no hay ninguno empezado
                    int minutosTrabajo = i.getIntExtra("minutosTrabajo", 50);
                    int minutosDescanso = i.getIntExtra("minutosDescanso", 10);

                    // solo inicializarlo la primera vez
                    // inicializar timer servicio
                    Intent e = new Intent(this, Timer.class);
                    e.putExtra("minutosTrabajo", minutosTrabajo);
                    e.putExtra("minutosDescanso", minutosDescanso);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(e);
                    } else {
                        startService(e);
                    }

                }

            }

        }

        if (getBooleanPreference("keepScreenOn")){
            // si se quiere que la ventana se quede activada
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (pomodoroKey == null && key != null){
            // si es de un proyecto y no lo ha creado él
            // si el dueño cancela el pomodoro aqui también se cancela
            databaseReference = FirebaseDatabase.getInstance().getReference(
                    "ProyectosPomodoro").child(key);
            ChildEventListener listener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    // el dato "empezado" ha cambiado, por lo que el dueño ha detenido el pomodoro
                    // o se ha detenido solo
                    try{
                        boolean empezado = dataSnapshot.getValue(Boolean.class);

                        if (!empezado && servicioEnMarcha(Timer.class)){
                            // si no está empezado es que se ha cancelado
                            pararServicio();
                        }
                    }catch(Exception e){
                        //
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            databaseReference.addChildEventListener(listener);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, ProyectosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Se recibe un mensaje desde el servicio
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        // actualizar texto: work / relax
        ((TextView) findViewById(R.id.textMin)).setText(event.getText());
        // actualizar remaining time
        ((TextView) findViewById(R.id.seekArcProgress)).setText(event.getTime());
        // seekarc
        seekArc.setProgress(event.getPercentage());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Botón para parar el pomodoro
     * @param v
     */
    public void stopPomodoro(View v){
        if (!servicioEnMarcha(Timer.class)){
            return;
        }

        stopProjectPomodoro();

    }

    /**
     * Parar un pomodoro de un proyecto
     */
    private void stopProjectPomodoro(){
        if (pomodoroKey != null){
            // no es un pomodoro individual, y este es el que lo ha creado
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ProyectosPomodoro").child(pomodoroKey);
            if (!isNetworkAvailable()){
                // No hay internet
                int tiempo = Toast.LENGTH_SHORT;
                Context context = getApplicationContext();
                Toast aviso = Toast.makeText(context, getResources().getString(R.string.internetNeeded),
                        tiempo);
                aviso.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 100);
                aviso.show();
                return;
            }
            databaseReference.child("empezado").setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    setStringPreference("pomodoroKey", null);
                    pararServicio();
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
        }else{
            // si es de un grupo y no es el creador pararlo
            pararServicio();
        }
    }

    /**
     * Parar servicio pomodoro
     */
    private void pararServicio(){
        // parar el servicio pomodoro
        Intent e = new Intent(this, Timer.class);
        e.putExtra("stop", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(e);
        } else {
            startService(e);
        }
        showToast(true, R.string.pomodoroStopped);

        // ver los proyectos
        Intent intent = new Intent(this, ProyectosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
