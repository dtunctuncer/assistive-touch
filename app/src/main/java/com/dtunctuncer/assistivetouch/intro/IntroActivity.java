package com.dtunctuncer.assistivetouch.intro;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.v4.app.Fragment;
import android.view.View;

import com.dtunctuncer.assistivetouch.R;
import com.dtunctuncer.assistivetouch.main.MainActivity;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;

public class IntroActivity extends MaterialIntroActivity {
    public static final int TYPE_DRAW = 1;
    public static final int TYPE_WRITE_SETTINGS = 2;
    public static final int TYPE_DEVICE_ADMIN = 3;

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


        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.first_slide_background)
                .buttonsColor(R.color.first_slide_buttons)
                .neededPermissions(new String[]{Manifest.permission.CAMERA})
                .image(agency.tango.materialintroscreen.R.drawable.ic_next)
                .title(getString(R.string.camera_permission_title))
                .description(getString(R.string.camera_permission))
                .build());

        addSlide(CustomSlideFragment.getInstance(TYPE_DEVICE_ADMIN));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addSlide(CustomSlideFragment.getInstance(TYPE_DRAW));
            addSlide(CustomSlideFragment.getInstance(TYPE_WRITE_SETTINGS));
        }

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
