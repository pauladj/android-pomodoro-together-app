package com.example.pomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.widget.TextView;

import com.example.pomodoro.models.Pomodoro;
import com.example.pomodoro.utilities.MainToolbar;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.triggertrap.seekarc.SeekArc;
import com.triggertrap.seekarc.SeekArc.OnSeekArcChangeListener;

/**
 * Usada la librería SeekArc de neild001
 * proyecto: https://github.com/neild001/SeekArc
 * usuario: https://github.com/neild001
 * <p>
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2013 Triggertrap Ltd
 * Author Neil Davies
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


public class NewPomodoroRelax extends MainToolbar {

    private SeekArc mSeekArc;
    private TextView mSeekArcProgress;

    private int minutosTrabajo; // los minutos de trabajo decididos en la pantalla anterior

    private int arco = 50;
    private int textoArco = 50;

    private int minutosDescanso = -1;
    private String projectKey = null;
    private String projectName = null;

    private DatabaseReference databaseReference;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("minutosTrabajo", minutosTrabajo);
        outState.putInt("minutosDescanso", minutosDescanso);
        outState.putString("projectKey", projectKey);
        outState.putString("projectName", projectName);

        outState.putInt("arco", mSeekArc.getProgress());
        outState.putInt("progress", Integer.valueOf(mSeekArcProgress.getText().toString()));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("arco")) {
            arco = savedInstanceState.getInt("arco");
            textoArco = savedInstanceState.getInt("progress");
            projectKey = savedInstanceState.getString("projectKey");
            projectName = savedInstanceState.getString("projectName");

            // recuperar valores si hay un cambio de orientación
            mSeekArc.setProgress(arco);
            mSeekArcProgress.setText(String.valueOf(textoArco));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_pomodoro_last, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkFCMAvailable()){
            return;
        }

        setContentView(R.layout.activity_new_individual_pomodoro_next);

        // cargar barra
        loadToolbar(getResources().getString(R.string.tiempoDescanso));
        showBackButtonOption();
        removeElevation();

        // recuperar minutos de trabajo
        if (savedInstanceState != null) {
            minutosTrabajo = savedInstanceState.getInt("minutosTrabajo", 50);
            minutosDescanso = savedInstanceState.getInt("minutosDescanso", 10);
        } else {
            Intent mIntent = getIntent();
            minutosTrabajo = mIntent.getIntExtra("minutosTrabajo", 50);
            if (mIntent.hasExtra("projectKey")) {
                // si es un pomodoro grupal
                projectKey = mIntent.getStringExtra("projectKey");
                projectName = mIntent.getStringExtra("projectName");
            }
        }


        // circular seek arc
        mSeekArc = (SeekArc) findViewById(R.id.seekArc);
        mSeekArcProgress = (TextView) findViewById(R.id.seekArcProgress);

        mSeekArc.setOnSeekArcChangeListener(new OnSeekArcChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onProgressChanged(SeekArc seekArc, int progress,
                                          boolean fromUser) {
                mSeekArcProgress.setText(String.valueOf(progress));
            }
        });

        // firebase realtime database
        databaseReference = FirebaseDatabase.getInstance().getReference("ProyectosPomodoro");

    }


    /**
     * El usuario quiere seguir al siguiente paso, finalizar e iniciar
     */
    public void creacionPomodoroIndividual() {
        minutosDescanso = Integer.valueOf(mSeekArcProgress.getText().toString());
        if (minutosDescanso == 0) {
            showToast(false, R.string.pomodoroZero);
            return;
        }

        if (projectKey != null) {
            // el pomodoro es de un proyecto, guardar datos en firebase
            uploadDataToFirebase();
        } else {
            // Siguiente pantalla
            Intent i = new Intent(this, CountDownTimerActivity.class);
            i.putExtra("minutosTrabajo", minutosTrabajo);
            i.putExtra("minutosDescanso", minutosDescanso);
            setBooleanPreference("individual", true);
            startActivity(i);
            finish();
        }
    }

    /**
     * Upload pomodoro data to firebase
     */
    private void uploadDataToFirebase() {
        if (!isNetworkAvailable()) {
            // no hay internet
            showToast(false, R.string.internetNeeded);
            return;
        }

        // crear objeto pomodoro
        Pomodoro nuevoPomodoro = new Pomodoro();
        nuevoPomodoro.setEmpezado(false);
        nuevoPomodoro.setProyecto(projectKey);
        nuevoPomodoro.setRelax(minutosDescanso);
        nuevoPomodoro.setWork(minutosTrabajo);

        databaseReference.push().setValue(nuevoPomodoro, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    // error
                    showToast(false, R.string.error);
                    return;
                }

                showToast(true, R.string.pomodoroCreated);

                // go to the project
                Intent intent = new Intent(NewPomodoroRelax.this, ProyectoPomodorosActivity.class);
                intent.putExtra("projectKey", projectKey);
                intent.putExtra("projectName", projectName);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }
        });
    }

}
