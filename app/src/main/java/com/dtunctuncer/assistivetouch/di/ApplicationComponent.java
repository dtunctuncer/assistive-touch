package com.dtunctuncer.assistivetouch.di;

import android.content.Context;
import android.content.SharedPreferences;

import com.dtunctuncer.assistivetouch.App;
import com.dtunctuncer.assistivetouch.intro.SplashActivity;
import com.dtunctuncer.assistivetouch.touchboard.BrightnessHelperActivity;
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

    void inject(BrightnessHelperActivity activity);

    void inject(TouchService service);

    void inject(SplashActivity splashActivity);
}
