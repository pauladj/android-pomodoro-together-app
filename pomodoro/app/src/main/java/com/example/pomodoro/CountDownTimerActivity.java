package com.example.pomodoro;

import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pomodoro.models.MessageEvent;
import com.example.pomodoro.services.Timer;
import com.example.pomodoro.utilities.MainToolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CountDownTimerActivity extends MainToolbar {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down_timer);

        if (savedInstanceState == null){
            // primera vez
            Intent i = getIntent();

            boolean empezarNuevo = i.getBooleanExtra("nuevo", true);

            if (empezarNuevo){
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

        if (getBooleanPreference("keepScreenOn")){
            // si se quiere que la ventana se quede activada
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, ProyectosActivity.class);
        startActivity(i);
    }

    /**
     * Se recibe un mensaje desde el servicio
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        ((TextView) findViewById(R.id.textView)).setText(event.message);

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


}
