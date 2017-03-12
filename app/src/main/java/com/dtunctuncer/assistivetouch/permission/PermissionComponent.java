package com.dtunctuncer.assistivetouch.permission;

import com.dtunctuncer.assistivetouch.di.ActivityScope;
import com.dtunctuncer.assistivetouch.di.ApplicationComponent;

import dagger.Component;

@ActivityScope
@Component(modules = {PermissionModule.class}, dependencies = {ApplicationComponent.class})
public interface PermissionComponent {
    void inject(PermissionHelperActivity permissionHelperActivity);
}
