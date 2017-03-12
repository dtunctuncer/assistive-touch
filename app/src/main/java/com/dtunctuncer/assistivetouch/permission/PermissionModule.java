package com.dtunctuncer.assistivetouch.permission;

import com.dtunctuncer.assistivetouch.di.ActivityScope;

import dagger.Module;
import dagger.Provides;

@Module
public class PermissionModule {
    private IPermissionView view;

    public PermissionModule(IPermissionView view) {
        this.view = view;
    }


    @Provides
    @ActivityScope
    IPermissionView provideView() {
        return view;
    }
}
