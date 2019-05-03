package com.example.pomodoro.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationMenu;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.pomodoro.ProyectosActivity;
import com.example.pomodoro.R;

/*import com.example.pomodoro.fragments.AsyncTaskFragment;

public class Common extends LanguageActivity  implements AsyncTaskFragment.TaskCallbacks  {

    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private AsyncTaskFragment mTaskFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // fragmento que contiene la tarea asíncrona
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (AsyncTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // El fragmento solo es null cuando la actividad se crea por primera vez, cuando se rota
        // el fragmento se mantiene
        if (mTaskFragment == null) {
            mTaskFragment = new AsyncTaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
    }
**/

/**
 * Get the fragment containing the asynctask
 *
 * @return - The async task fragment
 */

/**
 public AsyncTaskFragment getmTaskFragment(){
 return mTaskFragment;
 }


 }
 */

public class Common extends LanguageActivity {

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

    /**
     * Get the active username
     * @param - the active username (token)
     */
    public String getActiveUsername() {
        SharedPreferences prefs_especiales = getSharedPreferences(
                "preferencias_especiales",
                Context.MODE_PRIVATE);

        return prefs_especiales.getString("activeUsername", null);
    }

    /**
     * Set the active username
     * @param username - the active username
     */

    public void setActiveUsername(String username) {
        SharedPreferences prefs_especiales = getSharedPreferences(
                "preferencias_especiales",
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor2 = prefs_especiales.edit();
        editor2.putString("activeUsername", username);
        editor2.apply();
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
                    Intent i = new Intent(Common.this, ProyectosActivity.class);
                    startActivity(i);
                    finish();
                    return true;
                }else if(item.getItemId() == R.id.active && !item.isChecked()){

                    return true;
                }else if(item.getItemId() == R.id.settings && !item.isChecked()){

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
}