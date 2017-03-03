package com.dtunctuncer.assistivetouch.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dtunctuncer.assistivetouch.App;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    private App app;

    public ApplicationModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    App provideApp(){
        return app;
    }

    @Provides
    @Singleton
    Context provideContex(){
        return app.getApplicationContext();
    }

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(App application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    SharedPreferences.Editor provideEditor(SharedPreferences sharedPreferences) {
        return sharedPreferences.edit();
    }
}
