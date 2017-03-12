package com.dtunctuncer.assistivetouch.intro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dtunctuncer.assistivetouch.touchboard.TouchService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean(TouchService.SERVICE_SP_KEY, false)) {
            context.startService(new Intent(context, TouchService.class));
        }
    }
}