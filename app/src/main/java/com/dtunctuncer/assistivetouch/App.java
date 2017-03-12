package com.dtunctuncer.assistivetouch;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.dtunctuncer.assistivetouch.di.ApplicationComponent;
import com.dtunctuncer.assistivetouch.di.ApplicationModule;
import com.dtunctuncer.assistivetouch.di.DaggerApplicationComponent;
import com.dtunctuncer.assistivetouch.utils.analytics.AnalyticsTracker;
import com.dtunctuncer.assistivetouch.utils.timber.CrashReportTree;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class App extends Application {

    private static ApplicationComponent component;

    public static ApplicationComponent getComponent() {
        return component;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //di
        component = DaggerApplicationComponent.builder().applicationModule(new ApplicationModule(this)).build();

        //Google Anaylicts
        AnalyticsTracker.initialize(this);

        //Crashlatics
        CrashlyticsCore core = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();

        Fabric.with(this, new Crashlytics.Builder().core(core).build());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportTree());
        }
    }
}