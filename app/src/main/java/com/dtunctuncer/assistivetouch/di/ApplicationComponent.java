package com.dtunctuncer.assistivetouch.di;

import android.content.Context;
import android.content.SharedPreferences;

import com.dtunctuncer.assistivetouch.App;
import com.dtunctuncer.assistivetouch.touchboard.TouchService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
    App app();

    Context context();

    SharedPreferences preferences();

    SharedPreferences.Editor editor();

    void inject(TouchService service);
}
