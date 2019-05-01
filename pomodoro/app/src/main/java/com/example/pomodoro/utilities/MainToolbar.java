package com.example.pomodoro.utilities;

import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.pomodoro.R;
import com.example.pomodoro.dialogs.ConfirmAbandonarProyecto;
import com.example.pomodoro.dialogs.NuevoProyecto;

public class MainToolbar extends Common {

    Menu menu;



    /**
     * Loads top toolbar
     */
    public void loadToolbar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.labarra);
        setSupportActionBar(toolbar);
    }

    /**
     * Loads top toolbar
     */
    public void loadToolbar(String title){
        Toolbar toolbar = (Toolbar)findViewById(R.id.labarra);
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

        }else if(id == R.id.menuLeaveProject){
            // El usuario quiere abandonar un proyecto, pedir confirmación
            DialogFragment dialog = new ConfirmAbandonarProyecto();
            dialog.show(getSupportFragmentManager(), "abandonarProyecto");
        }

        return super.onOptionsItemSelected(item);
    }



}
