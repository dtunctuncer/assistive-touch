package com.dtunctuncer.assistivetouch;

import android.app.Application;

import com.dtunctuncer.assistivetouch.di.ApplicationComponent;
import com.dtunctuncer.assistivetouch.di.ApplicationModule;
import com.dtunctuncer.assistivetouch.di.DaggerApplicationComponent;

public class App extends Application {

    private static ApplicationComponent component;

    public static ApplicationComponent getComponent() {
        return component;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        component = DaggerApplicationComponent.builder().applicationModule(new ApplicationModule(this)).build();
    }
}
