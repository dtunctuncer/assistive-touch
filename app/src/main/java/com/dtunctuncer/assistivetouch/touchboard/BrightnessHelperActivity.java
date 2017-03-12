package com.dtunctuncer.assistivetouch.touchboard;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.dtunctuncer.assistivetouch.App;
import com.dtunctuncer.assistivetouch.R;
import com.dtunctuncer.assistivetouch.utils.RxBus;
import com.dtunctuncer.assistivetouch.utils.events.CloseTouchBoardEvent;

import javax.inject.Inject;

public class BrightnessHelperActivity extends AppCompatActivity {

    @Inject
    RxBus rxBus;

    private int progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getComponent().inject(this);

        progress = getIntent().getIntExtra("brightness", 0);
        if (checkSystemWritePermission()) {
            setBrightness();
        } else {
            rxBus.send(new CloseTouchBoardEvent());
            new AlertDialog.Builder(this)
                    .setTitle(R.string.need_permission)
                    .setMessage(R.string.brightness_settings_content)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            askPermission();
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .show();

        }

    }

    private void setBrightness() {
        android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, progress);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = progress / 255.0f;
        getWindow().setAttributes(layoutParams);
        finish();
    }

    private boolean checkSystemWritePermission() {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(this);
        }
        return retVal;
    }

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 1905);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1905) {
            setBrightness();
        }
    }
}