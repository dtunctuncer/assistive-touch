package com.dtunctuncer.assistivetouch.main;

import com.dtunctuncer.assistivetouch.di.ActivityScope;

import dagger.Module;
import dagger.Provides;

@Module
public class MainModule {

    private IMainView view;

    public MainModule(IMainView view) {
        this.view = view;
    }

    @Provides
    @ActivityScope
    IMainView provideView(){
        return view;
    }
}
