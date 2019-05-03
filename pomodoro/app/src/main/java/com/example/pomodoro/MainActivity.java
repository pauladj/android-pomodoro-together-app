package com.example.pomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

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
                    intent = new Intent(MainActivity.this, ProyectosActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, DURACION_SPLASH);
    }

}
