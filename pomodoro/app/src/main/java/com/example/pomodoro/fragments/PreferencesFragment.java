package com.example.pomodoro.fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
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
    boolean img = false;
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
        }else if(key.equals("withimage")){
            // sonido cuando el pomodoro acaba
            SharedPreferences prefs_especiales = getActivity().getSharedPreferences(
                    "preferencias_especiales",
                    Context.MODE_PRIVATE);

            boolean withImage = prefs_especiales.getBoolean(key, false);
            SharedPreferences.Editor editor2 = prefs_especiales.edit();
            editor2.putBoolean(key, !withImage);
            editor2.apply();

            img = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.adimagen);
            String[] opciones = {getResources().getString(R.string.gallery),getResources().getString(R.string.photo)};
            builder.setItems(opciones, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String text = "";
                    if (which == 0) {  // en blanco
                        Intent elIntentGal = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        getActivity().startActivityForResult(elIntentGal, CODIGO_GALERIA);
                    } else if (which == 1) {
                        ((PreferencesActivity)getActivity()).tryTakingPhotoWithTheCamera();
                    }
                }
            });
            builder.show();

        }else if(key.equals("colors")){
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String color = prefs.getString("colors", "blanco");

            SharedPreferences prefs_especiales = getActivity().getSharedPreferences(
                    "preferencias_especiales",
                    Context.MODE_PRIVATE);

            SharedPreferences.Editor editor2 = prefs_especiales.edit();
            editor2.putString(key, color);
            editor2.apply();
        }
        // Notification saying that the preference has been changed
        int tiempo = Toast.LENGTH_SHORT;
        Toast aviso = Toast.makeText(getActivity(), R.string.preferences_saved, tiempo);
        aviso.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 100);
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
