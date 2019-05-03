package com.example.pomodoro;

import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.pomodoro.services.Timer;
import com.example.pomodoro.utilities.MainToolbar;

public class CountDownTimerActivity extends MainToolbar {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down_timer);

        if (savedInstanceState == null){
            Intent i = getIntent();

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, ProyectosActivity.class);
        startActivity(i);
    }


}
