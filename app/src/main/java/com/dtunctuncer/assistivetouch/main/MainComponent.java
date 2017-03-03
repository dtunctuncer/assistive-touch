package com.dtunctuncer.assistivetouch.main;

import com.dtunctuncer.assistivetouch.di.ActivityScope;
import com.dtunctuncer.assistivetouch.di.ApplicationComponent;
import com.dtunctuncer.assistivetouch.di.ApplicationModule;

import dagger.Component;

@ActivityScope
@Component(modules = {MainModule.class}, dependencies = {ApplicationComponent.class})
public interface MainComponent {
    void inject(MainActivity mainActivity);
}
