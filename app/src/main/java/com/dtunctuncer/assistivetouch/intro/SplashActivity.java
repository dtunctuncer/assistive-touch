package com.dtunctuncer.assistivetouch.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.dtunctuncer.assistivetouch.App;
import com.dtunctuncer.assistivetouch.main.MainActivity;

import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {
    @Inject
    SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getComponent().inject(this);
        boolean firstOpen = preferences.getBoolean("first_open", true);
        if (firstOpen) {
            startActivity(new Intent(this, IntroActivity.class));

        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}