package com.example.pomodoro.utilities;

import android.content.Intent;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.pomodoro.LoginRegistroActivity;
import com.example.pomodoro.R;
import com.example.pomodoro.dialogs.AddUserToProject;
import com.example.pomodoro.dialogs.ConfirmAbandonarProyecto;
import com.example.pomodoro.services.Timer;

public class MainToolbar extends Common {

    Menu menu;
    Toolbar toolbar;


    /**
     * Loads top toolbar
     */
    public void loadToolbar(){
        toolbar = (Toolbar)findViewById(R.id.labarra);
        setSupportActionBar(toolbar);
    }

    /**
     * Loads top toolbar
     */
    public void loadToolbar(String title){
        toolbar = (Toolbar)findViewById(R.id.labarra);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        this.menu = menu;
        return true;
    }


    /**
     * Remove the elevation
     */
    public void removeElevation(){
        toolbar.setElevation(0);
        toolbar.bringToFront();
    }

    /**
     * It adds a back arrow to the option menu
     */
    public void showBackButtonOption(){
        // Mostrar flecha para ir para atrás si se quiere
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Si se pulsa en la flecha del toolbar se va para atrás
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        if (id == R.id.menuLogout){
            // El usuario quiere salir de su cuenta
            setActiveUsername(null);

            // Parar el pomodoro activo si tiene
            if (servicioEnMarcha(Timer.class)){
                // parar el servicio pomodoro
                Intent e = new Intent(this, Timer.class);
                e.putExtra("stop", true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(e);
                } else {
                    startService(e);
                }
            }

            setStringPreference("pomodoroKey", null);
            setBooleanPreference("individual", false);
            setStringPreference("imagepath", null);

            Intent i = new Intent(this, LoginRegistroActivity.class);
            startActivity(i);
            finish();
        }else if(id == R.id.menuLeaveProject){
            // El usuario quiere abandonar un proyecto, pedir confirmación
            DialogFragment dialog = new ConfirmAbandonarProyecto();
            dialog.show(getSupportFragmentManager(), "abandonarProyecto");
        }else if(id == R.id.menuAddUser){
            // El usuario quiere añadir un usuario a un proyecto
            DialogFragment dialog = new AddUserToProject();
            dialog.show(getSupportFragmentManager(), "anadirUsuario");
        }else if(id== R.id.menuNext){
            // El usuario está creando un pomodoro individual
            creacionPomodoroIndividual();
        }

        return super.onOptionsItemSelected(item);
    }


    public void creacionPomodoroIndividual(){}



}
