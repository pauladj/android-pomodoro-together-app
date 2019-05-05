package com.example.pomodoro;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.pomodoro.utilities.Common;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrevioAActivo extends Common {

    private String pomodoroKey;
    private int trabajar;
    private int descansar;

    private String workFin; // hora fin de work
    private String relaxFin; // hora fin de relax

    private DatabaseReference databaseReference;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("pomodoroKey", pomodoroKey);
        outState.putInt("trabajar", trabajar);
        outState.putInt("descansar", descansar);

        outState.putString("workFin", workFin);
        outState.putString("relaxFin", relaxFin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previo_a_activity);

        if (savedInstanceState == null){
            // primera vez que se inicia la actividad
            Bundle extras = getIntent().getExtras();
            pomodoroKey = extras.getString("pomodoroKey");
            trabajar = extras.getInt("trabajar");
            descansar = extras.getInt("descansar");
        }else{
            // se recupera de un cambio de orientación
            pomodoroKey = savedInstanceState.getString("pomodoroKey");
            trabajar = savedInstanceState.getInt("trabajar", 50);
            descansar = savedInstanceState.getInt("descansar", 10);

            workFin = savedInstanceState.getString("workFin", null);
            relaxFin = savedInstanceState.getString("relaxFin", null);
        }

        // Definir los valores descansar/trabajar
        ((TextView) findViewById(R.id.textView)).setText(String.valueOf(trabajar));
        ((TextView) findViewById(R.id.textView3)).setText(String.valueOf(descansar));
    }

    /**
     * El usuario quiere empezar el pomodoro del proyecto
     * @param v
     */
    public void empezarPomodoroProyecto(View v){
        // definir referencia a la bd
        databaseReference =
                FirebaseDatabase.getInstance().getReference("ProyectosPomodoro").child(pomodoroKey);
        databaseReference.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // actualizar pomodoro
                if ((boolean) mutableData.child("empezado").getValue()){
                    // si ya ha sido empezado justo antes
                    // TODO probar
                    showToast(false, R.string.pomodoroAlreadyActive);
                    return Transaction.abort();
                }
                java.util.Date fechaActual = new java.util.Date();
                Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                calendar.setTime(fechaActual);
                calendar.add(Calendar.MINUTE, trabajar);
                Date a = calendar.getTime();
                workFin = a.toString();
                mutableData.child("horaWorkFin").setValue(workFin);

                calendar = Calendar.getInstance();
                calendar.setTime(a);
                calendar.add(Calendar.MINUTE, descansar);
                relaxFin = calendar.getTime().toString();
                mutableData.child("horaDescansoFin").setValue(relaxFin);

                mutableData.child("empezado").setValue(true);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null){
                    showToast(false, R.string.error);
                    return;
                }
                // ENVIAR MINUTOS Y SI ES O NO EL QUE LO HA INICIADO
                Intent i = new Intent(PrevioAActivo.this, CountDownTimerActivity.class);
                i.putExtra("horaTrabajoFin", workFin);
                i.putExtra("horaDescansoFin", relaxFin);
                setStringPreference("pomodoroKey", pomodoroKey);
                startActivity(i);
                finish();
            }
        });
    }


}
