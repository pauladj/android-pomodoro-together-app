package com.example.pomodoro.utilities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String language = prefs.getString("language", "default");
        Locale system = Resources.getSystem().getConfiguration().locale;

        Locale nuevaloc = null;
        if (language.equals("default")) {
            // the system locale
            nuevaloc = system;
        } else {
            if (language.equals("spanish")) {
                // change the language to spanish
                nuevaloc = new Locale("es");
            } else if (language.equals("english")) {
                nuevaloc = new Locale("en");
            }
        }
        changeLanguage(nuevaloc);
    }

    /**
     * Change language
     *
     * @param nuevaloc - the new locale
     */
    private void changeLanguage(Locale nuevaloc) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = nuevaloc;
        conf.setLocale(nuevaloc);
        res.updateConfiguration(conf, dm);
    }

}
