package com.example.pomodoro;

import android.os.Bundle;

import com.example.pomodoro.utilities.MainToolbar;

public class PaulaActivity extends MainToolbar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load main activity with fragment(s)
        setContentView(R.layout.activity_paula);
        // load top toolbar
        loadToolbar();
    }
}
