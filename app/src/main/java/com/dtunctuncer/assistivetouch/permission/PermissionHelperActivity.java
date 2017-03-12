package com.dtunctuncer.assistivetouch.permission;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.dtunctuncer.assistivetouch.App;
import com.dtunctuncer.assistivetouch.R;

import javax.inject.Inject;

public class PermissionHelperActivity extends Activity implements IPermissionView {

    @Inject
    PermissionPresenter presenter;
    @Inject
    Context appContext;

    private String permission, message;
    private ComponentName componentName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerPermissionComponent.builder().applicationComponent(App.getComponent()).permissionModule(new PermissionModule(this)).build().inject(this);
        componentName = new ComponentName(appContext, AdminReceiver.class);
        permission = getIntent().getStringExtra("permission_name");
        message = getIntent().getStringExtra("message");
        int type = getIntent().getIntExtra("type", 0);
        presenter.openDialogForType(type);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 1905);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1905) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.permission_error, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public void openRuntimePermissionDialog() {
        if (permission != null && !permission.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.need_permission)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ActivityCompat.requestPermissions(PermissionHelperActivity.this, new String[]{permission}, 1905);
                            }

                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            PermissionHelperActivity.this.finish();
                        }
                    })
                    .show();
        } else {
            finish();
        }
    }

    @Override
    public void openWriteSettingsPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.need_permission)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            askPermission();
                        }

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PermissionHelperActivity.this.finish();
                    }
                })
                .show();
    }

    @Override
    public void openDeviceAdminPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.need_permission)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        askDeviceAdminPermission();
                    }

                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PermissionHelperActivity.this.finish();
                    }
                })
                .show();
    }

    private void askDeviceAdminPermission() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_permission));
        startActivityForResult(intent, 666);
    }
}