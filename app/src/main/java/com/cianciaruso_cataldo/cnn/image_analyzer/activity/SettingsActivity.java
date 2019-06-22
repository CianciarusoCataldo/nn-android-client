package com.cianciaruso_cataldo.cnn.image_analyzer.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cianciaruso_cataldo.cnn.image_analyzer.fragment.SettingsFragment;



public class SettingsActivity extends AppCompatActivity {

    public static boolean is_animations_enabled= true;
    public static boolean pause=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
