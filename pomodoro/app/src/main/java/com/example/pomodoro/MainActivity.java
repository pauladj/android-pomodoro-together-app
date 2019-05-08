package com.example.pomodoro;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.example.pomodoro.services.Timer;
import com.example.pomodoro.utilities.Common;


public class MainActivity extends Common {

    private ImageView imagen;
    private final int DURACION_SPLASH = 800; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //imagen = findViewById(R.id.imagen1);
        // hace que la aplicacion espere unos minutos
        //imagen.setImageResource(R.drawable.umlogo);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, LoginRegistroActivity.class);
                if (getActiveUsername() != null) {
                    // el usuario ha iniciado sesión previamente

                    // parar el servicio si estaba activo
                    if (servicioEnMarcha(Timer.class)){
                        Intent e = new Intent(MainActivity.this, Timer.class);
                        e.putExtra("stop", true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(e);
                        } else {
                            startService(e);
                        }
                    }
                    setStringPreference("pomodoroKey", null);
                    setBooleanPreference("individual", false);

                    intent = new Intent(MainActivity.this, ProyectosActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, DURACION_SPLASH);
    }

}
