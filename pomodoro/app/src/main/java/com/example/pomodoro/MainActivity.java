package com.example.pomodoro;

import android.os.Bundle;

import com.example.pomodoro.utilities.MainToolbar;

public class MainActivity extends MainToolbar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load main activity with fragment(s)
        setContentView(R.layout.activity_main);
        // load top toolbar
        loadToolbar();
    }
}
