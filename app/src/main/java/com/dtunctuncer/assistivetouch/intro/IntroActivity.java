package com.dtunctuncer.assistivetouch.intro;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.FloatRange;
import android.support.v4.app.Fragment;
import android.view.View;

import com.dtunctuncer.assistivetouch.R;
import com.dtunctuncer.assistivetouch.core.PermissionTypes;
import com.dtunctuncer.assistivetouch.intro.slide.CustomSlideFragment;
import com.dtunctuncer.assistivetouch.main.MainActivity;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;

public class IntroActivity extends MaterialIntroActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addSlide(new SlideFragmentBuilder()
                    .backgroundColor(R.color.first_slide_background)
                    .buttonsColor(R.color.first_slide_buttons)
                    .neededPermissions(new String[]{Manifest.permission.CAMERA})
                    .image(R.drawable.ic_camera_alt_white_24dp)
                    .title(getString(R.string.camera_permission_title))
                    .description(getString(R.string.camera_permission))
                    .build());
            if (!Settings.canDrawOverlays(this))
                addSlide(CustomSlideFragment.getInstance(PermissionTypes.TYPE_DRAW));
            if (!Settings.System.canWrite(this))
                addSlide(CustomSlideFragment.getInstance(PermissionTypes.TYPE_WRITE_SETTINGS));
        }
        addSlide(CustomSlideFragment.getInstance(PermissionTypes.TYPE_DEVICE_ADMIN));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof CustomSlideFragment) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onFinish() {
        super.onFinish();
        startActivity(new Intent(this, MainActivity.class));
    }
}
