package com.dtunctuncer.assistivetouch.main;

import android.content.Context;
import android.content.SharedPreferences;

import com.dtunctuncer.assistivetouch.R;
import com.dtunctuncer.assistivetouch.touchboard.TouchService;

import javax.inject.Inject;

public class MainPresenter {

    private IMainView view;
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Inject
    public MainPresenter(IMainView view, Context context, SharedPreferences preferences, SharedPreferences.Editor editor) {
        this.view = view;
        this.context = context;
        this.preferences = preferences;
        this.editor = editor;
    }


    public void checkServiceRunning() {
        if (preferences.getBoolean(TouchService.SERVICE_SP_KEY, false)) {
            view.changeStartButtonText(context.getString(R.string.stop));
        } else {
            view.changeStartButtonText(context.getString(R.string.start));
        }
    }

    public void setFistOpen() {
        editor.putBoolean("first_open", false);
        editor.apply();
    }
}
