package com.example.pomodoro;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.pomodoro.fragments.loginfragment;
import com.example.pomodoro.fragments.registro;


public class PagerAdapter extends FragmentStatePagerAdapter {

    private int numberOfTabs;


    public PagerAdapter(FragmentManager fm, int numberOfTabs) {
        super(fm);
        this.numberOfTabs = numberOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new loginfragment();
            case 1:
                return new registro();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}