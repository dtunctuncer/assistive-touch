package com.dtunctuncer.assistivetouch.permission;


import javax.inject.Inject;

public class PermissionPresenter {

    private IPermissionView view;

    @Inject
    public PermissionPresenter(IPermissionView view) {
        this.view = view;
    }


    public void openDialogForType(int type) {
        switch (type) {
            case 0:
                view.openRuntimePermissionDialog();
                break;
            case 1:
                view.openWriteSettingsPermissionDialog();
                break;
            case 2:
                view.openDeviceAdminPermissionDialog();
                break;
        }
    }
}
