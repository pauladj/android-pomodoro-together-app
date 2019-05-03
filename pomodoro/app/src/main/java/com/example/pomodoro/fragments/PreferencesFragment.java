package com.example.pomodoro.fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.Gravity;
import android.widget.Toast;

import com.example.pomodoro.PreferencesActivity;
import com.example.pomodoro.R;

public class PreferencesFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.conf_preferencias);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key){
        if (key.equals("language")){
            // recargar app
            Intent i = new Intent(getActivity(), PreferencesActivity.class);
            startActivity(i);
            getActivity().finish();
        }

       // Notification saying that the preference has been changed
        int tiempo = Toast.LENGTH_SHORT;
        Toast aviso = Toast.makeText(getActivity(), R.string.preferences_saved, tiempo);
        aviso.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 100);
        aviso.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
