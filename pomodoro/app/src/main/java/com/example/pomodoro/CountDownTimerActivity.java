package com.example.pomodoro;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pomodoro.models.MessageEvent;
import com.example.pomodoro.services.Timer;
import com.example.pomodoro.utilities.MainToolbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    private boolean empezarNuevo;
    private boolean individual; // es individual y no de un proyecto

    private boolean finish; // si es true solo cerrar esta actividad

    private boolean descanso; // si está en el descanso

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("pomodoroKey", pomodoroKey);
        outState.putString("key", key);
        outState.putBoolean("empezarNuevo", empezarNuevo);
        outState.putBoolean("individual", individual);
        outState.putBoolean("finish", finish);
        outState.putBoolean("descanso", descanso);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("pomodoroKey")) {
            pomodoroKey = savedInstanceState.getString("pomodoroKey", null);
        }
        if (savedInstanceState.containsKey("key")) {
            pomodoroKey = savedInstanceState.getString("key", null);
        }
        empezarNuevo = savedInstanceState.getBoolean("empezarNuevo");
        individual = savedInstanceState.getBoolean("individual");
        finish = savedInstanceState.getBoolean("finish", false);
        descanso = savedInstanceState.getBoolean("descanso", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkFCMAvailable()){
            return;
        }

        setContentView(R.layout.activity_count_down_timer);

        seekArc = findViewById(R.id.seekArc);

        boolean timerOnline = servicioEnMarcha(Timer.class); // el servicio está en marcha
        if (!timerOnline || getBooleanPreference("individual")){
            // si el servicio no está en marcha no mostrar el botón de chat, o es uno individual
            findViewById(R.id.buttonChat).setVisibility(View.GONE);
        }else if(timerOnline && descanso){
            // el servicio está activo y está en el descanso, no es individual
            findViewById(R.id.buttonChat).setVisibility(View.VISIBLE);
        }

        // valores por defecto
        ((TextView) findViewById(R.id.seekArcProgress)).setText("0:00");
        seekArc.setProgress(100);

        boolean conImagen = false;
        if (getBooleanPreference("withimage") && getStringPreference("imagepath") != null){
            // cargar imagen en el fondo
            ((ImageView) findViewById(R.id.backgroundImage)).setImageURI(Uri.parse(getStringPreference("imagepath")));
            conImagen = true;
        }

        if (getStringPreference("colors") != null && getStringPreference("colors").equals("white")){
            // cambiar el color texto y ruleta
            ((Button) findViewById(R.id.buttonDetener)).setTextColor(getResources().getColor(R.color.colorWhite));
            ((TextView) findViewById(R.id.textMin)).setTextColor(getResources().getColor(R.color.colorWhite));
            ((TextView) findViewById(R.id.seekArcProgress)).setTextColor(getResources().getColor(R.color.colorWhite));
            ((SeekArc) findViewById(R.id.seekArc)).setProgressColor(getResources().getColor(R.color.colorWhite));
            ((Button) findViewById(R.id.buttonChat)).setBackgroundColor(getResources().getColor(R.color.colorWhite));
            ((Button) findViewById(R.id.buttonChat)).setTextColor(getResources().getColor(R.color.colorAccent));

            if (!conImagen){
                ((LinearLayout) findViewById(R.id.bigContainer)).setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }

        }

        if (savedInstanceState == null) {
            // primera vez
            Intent i = getIntent();

            pomodoroKey = getStringPreference("pomodoroKey"); // es null si el pomodoro no es de
            // un proyecto y si no lo ha puesto en marcha el usuario
            empezarNuevo = i.getBooleanExtra("nuevo", true);
            finish = i.getBooleanExtra("finish", false);

            if (i.hasExtra("key")) {
                // si no es null el pomodoro es de un proyecto pero no lo ha puesto en marcha
                // este usuario
                key = i.getStringExtra("key");
            }

            if (empezarNuevo && !servicioEnMarcha(Timer.class)) {
                if (i.hasExtra("horaTrabajoFin")) {
                    // Si se está iniciando un pomodoro de un proyecto, inicializar timer servicio
                    setBooleanPreference("individual", false);
                    Intent e = new Intent(this, Timer.class);
                    e.putExtra("horaTrabajoFin", i.getStringExtra("horaTrabajoFin"));
                    e.putExtra("horaDescansoFin", i.getStringExtra("horaDescansoFin"));
                    e.putExtra("pomodoroKey", pomodoroKey);

                    if (pomodoroKey == null && key != null){
                        setStringPreference("timerKey", key);
                    }else if(pomodoroKey != null & key == null){
                        setStringPreference("timerKey", pomodoroKey);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(e);
                    } else {
                        startService(e);
                    }
                } else {
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

        individual = getBooleanPreference("individual");

        if (getBooleanPreference("keepScreenOn")) {
            // si se quiere que la ventana se quede activada
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if ((pomodoroKey == null && key != null) || (!empezarNuevo && pomodoroKey == null && !individual)) {
            // si es de un proyecto y no lo ha creado él

            // cambiar texto botón
            ((Button) findViewById(R.id.buttonDetener)).setText(R.string.abandonar);
        }

        // si el dueño cancela el pomodoro grupal aqui también se cancela
        if (empezarNuevo && !individual) {
            // no ejecutar esto si se accede desde el menú de abajo o es un pomodoro individual
            String finalKey;
            if (pomodoroKey == null) {
                finalKey = key;
            } else {
                finalKey = pomodoroKey;
            }
            databaseReference = FirebaseDatabase.getInstance().getReference(
                    "ProyectosPomodoro").child(finalKey);
            ChildEventListener listener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    // el dato "empezado" ha cambiado, por lo que el dueño ha detenido el pomodoro
                    // o se ha detenido solo
                    try {
                        boolean empezado = dataSnapshot.getValue(Boolean.class);

                        if (!empezado && servicioEnMarcha(Timer.class)) {
                            // si no está empezado es que se ha cancelado
                            pararServicio();
                        }
                    } catch (Exception e) {
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
        if (finish){
            // solo cerrar
            super.onBackPressed();
        }else{
            Intent intent = new Intent(this, ProyectosActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Se recibe un mensaje desde el servicio
     *
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

        if (!descanso && event.getText() == R.string.descansar && !getBooleanPreference(
                "individual")){
            // si empieza el descanso y no es individual
            descanso = true;
            findViewById(R.id.buttonChat).setVisibility(View.VISIBLE);
        }
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
     * Start the pomodoro chat
     * @param v
     */
    public void startChat(View v){
        boolean timerOnline = servicioEnMarcha(Timer.class); // el servicio está en marcha
        if (!timerOnline){
            // si el servicio no está en marcha mensaje error
            showToast(false, R.string.error);
            return;
        }

        if (!isNetworkAvailable()){
            showToast(true, R.string.internetNeeded);
            return;
        }
        // Iniciar la actividad de chat
        Intent i = new Intent(CountDownTimerActivity.this, ChatPomodoro.class);
        startActivity(i);
    }

    /**
     * Botón para parar el pomodoro
     *
     * @param v
     */
    public void stopPomodoro(View v) {
        if (!servicioEnMarcha(Timer.class)) {
            return;
        }

        stopProjectPomodoro();

    }

    /**
     * Parar un pomodoro de un proyecto
     */
    private void stopProjectPomodoro() {
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
        } else {
            // si es de un grupo y no es el creador pararlo
            setBooleanPreference("individual", false);
            pararServicio();
        }
    }

    /**
     * Parar servicio pomodoro
     */
    private void pararServicio() {
        // parar el servicio pomodoro
        Intent e = new Intent(this, Timer.class);
        e.putExtra("stop", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(e);
        } else {
            startService(e);
        }
        showToast(true, R.string.pomodoroStopped);
        ((TextView) findViewById(R.id.seekArcProgress)).setText("0:00");
        seekArc.setProgress(100);
    }

}
