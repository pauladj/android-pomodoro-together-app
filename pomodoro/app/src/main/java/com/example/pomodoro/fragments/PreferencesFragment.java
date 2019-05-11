package com.example.pomodoro.fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.example.pomodoro.PreferencesActivity;
import com.example.pomodoro.R;

public class PreferencesFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    int CODIGO_GALERIA = 111;
    int CODIGO_FOTO =222;

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
        }else if(key.equals("keepScreenOn")){
            // mantener pantalla activa
            SharedPreferences prefs_especiales = getActivity().getSharedPreferences(
                    "preferencias_especiales",
                    Context.MODE_PRIVATE);

            boolean keepOn = prefs_especiales.getBoolean(key, false);
            SharedPreferences.Editor editor2 = prefs_especiales.edit();
            editor2.putBoolean(key, !keepOn);
            editor2.apply();
        }else if(key.equals("sound")){
            // sonido cuando el pomodoro acaba
            SharedPreferences prefs_especiales = getActivity().getSharedPreferences(
                    "preferencias_especiales",
                    Context.MODE_PRIVATE);

            boolean sound = prefs_especiales.getBoolean(key, false);
            SharedPreferences.Editor editor2 = prefs_especiales.edit();
            editor2.putBoolean(key, !sound);
            editor2.apply();
        }else if(key.equals("image")){
            Log.d("imagen", "onSharedPreferenceChanged: se ha pulsado imagen ");
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String language = prefs.getString("image", "gallery");

            Log.i("aqui", language);

            if(language.equals("gallery")){
                Intent elIntentGal = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                getActivity().startActivityForResult(elIntentGal, CODIGO_GALERIA);
            }
            else if(language.equals("photo")){
                ((PreferencesActivity)getActivity()).tryTakingPhotoWithTheCamera();
            }


        }else if(key.equals("colors")){
            Log.d("colors", "onSharedPreferenceChanged: se ha pulsado colors");
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String color = prefs.getString("colors", "blanco");

            Log.i("aqui", color);

            if (color.equals("white")) {
                //cambiar el color a blanco
            }
            else if (color.equals("black")){
                // cambiamos el color
            }
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
