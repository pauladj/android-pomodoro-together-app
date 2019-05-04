package com.example.pomodoro;

import android.content.Intent;
import android.os.Bundle;

import com.triggertrap.seekarc.SeekArc;
import com.triggertrap.seekarc.SeekArc.OnSeekArcChangeListener;

import android.view.Menu;
import android.widget.TextView;

import com.example.pomodoro.utilities.MainToolbar;

/**
 * Usada la librería SeekArc de neild001
 * proyecto: https://github.com/neild001/SeekArc
 * usuario: https://github.com/neild001
 *
 *
 The MIT License (MIT)

 Copyright (c) 2013 Triggertrap Ltd
 Author Neil Davies

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 the Software, and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */



public class NewPomodoro extends MainToolbar {

    private SeekArc mSeekArc;
    private TextView mSeekArcProgress;

    private int arco = 50;
    private int textoArco = 50;

    private String projectKey = null; // la clave del proyecto si no es individual
    private String projectName = null;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("arco", mSeekArc.getProgress());
        outState.putInt("progress", Integer.valueOf(mSeekArcProgress.getText().toString()));
        outState.putString("projectKey", projectKey);
        outState.putString("projectName", projectName);
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
        getMenuInflater().inflate(R.menu.new_pomodoro, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_individual_pomodoro);

        // cargar barra
        loadToolbar(getResources().getString(R.string.tiempoTrabajo));
        showBackButtonOption();
        removeElevation();

        // obtener atributos del intent si el pomodoro es grupal
        Bundle extras = getIntent().getExtras();
        if (extras != null && savedInstanceState == null){
            // la primera vez que se carga la actividad
            projectKey = extras.getString("projectKey");
            projectName = extras.getString("projectName");
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

    }

    @Override
    public void onBackPressed() {
       super.onBackPressed();
    }

    /**
     * El usuario quiere seguir al siguiente paso, elegir los minutos de descanso
     */
    public void creacionPomodoroIndividual(){
        int minutes = Integer.valueOf(mSeekArcProgress.getText().toString());
        if (minutes == 0){
            showToast(false, R.string.pomodoroZero);
            return;
        }

        // Siguiente pantalla
        Intent i = new Intent(this, NewPomodoroRelax.class);
        i.putExtra("minutosTrabajo", minutes);
        if (projectKey != null){
            // el pomodoro es de un proyecto
            i.putExtra("projectKey", projectKey);
            i.putExtra("projectName", projectName);
        }
        startActivity(i);
    }


}
