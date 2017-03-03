package com.dtunctuncer.assistivetouch.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;

import com.dtunctuncer.assistivetouch.R;
import com.dtunctuncer.assistivetouch.touchboard.TouchService;

import javax.inject.Inject;

public class MainPresenter {

    private IMainView view;
    private Context context;
    private SharedPreferences preferences;

    @Inject
    public MainPresenter(IMainView view, Context context, SharedPreferences preferences) {
        this.view = view;
        this.context = context;
        this.preferences = preferences;
    }


    public void checkServiceRunning() {
        if (preferences.getBoolean(TouchService.SERVICE_SP_KEY, false)) {
            view.changeStartButtonText(context.getString(R.string.stop));
        } else {
            view.changeStartButtonText(context.getString(R.string.start));
        }
    }
}
