package com.example.pomodoro.utilities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.pomodoro.AsyncTasks.ConectarAlServidor;
import com.example.pomodoro.CountDownTimerActivity;
import com.example.pomodoro.PreferencesActivity;
import com.example.pomodoro.ProyectosActivity;
import com.example.pomodoro.R;
import com.example.pomodoro.services.Timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class Common extends LanguageActivity implements ConectarAlServidor.TaskCallbacks  {

    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private ConectarAlServidor mTaskFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // fragmento que contiene la tarea asíncrona
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (ConectarAlServidor) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // El fragmento solo es null cuando la actividad se crea por primera vez, cuando se rota
        // el fragmento se mantiene
        if (mTaskFragment == null) {
            mTaskFragment = new ConectarAlServidor();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

    }

    /**
     * Show a toast in the view
     * @param acrossWindows - if true the toast does not disappear when view changes
     * @param messageId - the message id to show
     */

    public void showToast(Boolean acrossWindows, int messageId) {
        int tiempo = Toast.LENGTH_SHORT;
        Context context;
        if (acrossWindows) {
            context = getApplicationContext();
        } else {
            context = this;
        }
        Toast aviso = Toast.makeText(context, getResources().getString(messageId), tiempo);
        aviso.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 100);
        aviso.show();
    }

    @Override
    public void loginSuccess(String username) {

    }

    @Override
    public void signUpSuccess() {

    }

    /**
     * Get the preference value
     * @param key - the key of the preference
     * @return - the value of the preference
     */
    public String getStringPreference(String key){
        SharedPreferences prefs_especiales = getSharedPreferences(
                "preferencias_especiales",
                Context.MODE_PRIVATE);

        return prefs_especiales.getString(key, null);
    }

    /**
     * Get the preference value
     * @param key - the key of the preference
     * @return - the value of the preference
     */
    public Boolean getBooleanPreference(String key){
        SharedPreferences prefs_especiales = getSharedPreferences(
                "preferencias_especiales",
                Context.MODE_PRIVATE);

        return prefs_especiales.getBoolean(key, false);
    }

    /**
     * set the preference value
     * @param key - the key of the preference
     * @param value - the value for that key
     */
    public void setBooleanPreference(String key, Boolean value){
        SharedPreferences prefs_especiales = getSharedPreferences(
                "preferencias_especiales",
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor2 = prefs_especiales.edit();
        editor2.putBoolean(key, value);
        editor2.apply();
    }

    /**
     * set the preference value
     * @param key - the key of the preference
     * @param value - the value for that key
     */
    public void setStringPreference(String key, String value){
        SharedPreferences prefs_especiales = getSharedPreferences(
                "preferencias_especiales",
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor2 = prefs_especiales.edit();
        editor2.putString(key, value);
        editor2.apply();
    }

    /**
     * Get the active username
     * @param - the active username (token)
     */
    public String getActiveUsername() {
        return getStringPreference("activeUsername");
    }

    /**
     * Set the active username
     * @param username - the active username
     */

    public void setActiveUsername(String username) {
        setStringPreference("activeUsername", username);
    }

    /**
     * Add listener to the bottom menu
     * @param bottomMenu - the menu
     */
    public void addListenerToBottomMenu(final BottomNavigationView bottomMenu){
        bottomMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.projects && !item.isChecked()) {
                    // ver todos los proyectos
                    Intent i = new Intent(Common.this, ProyectosActivity.class);
                    startActivity(i);
                    finish();
                    return true;
                }else if(item.getItemId() == R.id.active && !item.isChecked()){
                    // ver el pomodoro activo individual si hay
                    boolean servicioEnMarcha = servicioEnMarcha(Timer.class);
                    if (servicioEnMarcha){
                        Intent i = new Intent(Common.this, CountDownTimerActivity.class);
                        i.putExtra("nuevo", false);
                        startActivity(i);
                        finish();
                        return true;
                    }else{
                        showToast(false, R.string.noPomodoroActive);
                        return false;
                    }
                }else if(item.getItemId() == R.id.settings && !item.isChecked()){
                    // ver la configuración
                    Intent i = new Intent(Common.this, PreferencesActivity.class);
                    startActivity(i);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Change the UI of the bottom menu
     * @param bottomMenu
     */
    public void selectProjects(final BottomNavigationView bottomMenu){
        bottomMenu.setSelectedItemId(R.id.projects);
    }

    /**
     * Change the UI of the bottom menu
     * @param bottomMenu
     */
    public void selectConfiguration(final BottomNavigationView bottomMenu){
        bottomMenu.setSelectedItemId(R.id.settings);
    }

    /**
     * Get the fragment containing the asynctask
     *
     * @return - The async task fragment
     */
    public ConectarAlServidor getmTaskFragment(){
        return mTaskFragment;
    }

    /**
     * Check if a service is running
     * @param serviceClass
     * @return
     */
    public boolean servicioEnMarcha(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



}