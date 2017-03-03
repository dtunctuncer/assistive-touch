package com.dtunctuncer.assistivetouch.touchboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class BrightnessHelperActivity extends AppCompatActivity {

    private int progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progress = getIntent().getIntExtra("brightness", 0);
        if (checkSystemWritePermission()) {
            setBrightness();
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
            if (!retVal) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1905);
            }
        }
        return retVal;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1905) {
            setBrightness();
        }
    }
}
