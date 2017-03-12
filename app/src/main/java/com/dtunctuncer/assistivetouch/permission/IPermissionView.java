package com.dtunctuncer.assistivetouch.permission;


public interface IPermissionView {
    void openRuntimePermissionDialog();

    void openWriteSettingsPermissionDialog();

    void openDeviceAdminPermissionDialog();
}
