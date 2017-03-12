package com.dtunctuncer.assistivetouch.intro;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtunctuncer.assistivetouch.R;

import agency.tango.materialintroscreen.SlideFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomSlideFragment extends SlideFragment {

    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.body)
    TextView body;
    @BindView(R.id.requestButton)
    Button requestButton;

    private int type;
    private boolean canMoveFurther;

    public static CustomSlideFragment getInstance(int type) {
        CustomSlideFragment fragment = new CustomSlideFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt("type");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_custom_slide, container, false);
        ButterKnife.bind(this, view);
        initViews();
        return view;
    }

    private void initViews() {
        switch (type) {
            case IntroActivity.TYPE_DRAW:
                image.setImageResource(R.drawable.ic_image_black_24dp);
                title.setText(R.string.draw_permission);
                body.setText(R.string.draw_explanation);
                break;
            case IntroActivity.TYPE_WRITE_SETTINGS:
                image.setImageResource(R.drawable.ic_settings_black_24dp);
                title.setText(R.string.write_settings);
                body.setText(R.string.write_settings_explanation);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (1905 == requestCode) {
            canMoveFurther = true;
            requestButton.setVisibility(View.GONE);
        } else if (666 == requestCode) {
            canMoveFurther = true;
            requestButton.setVisibility(View.GONE);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.requestButton)
    public void requestPermission() {
        switch (type) {
            case IntroActivity.TYPE_DRAW:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(getActivity())) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
                        startActivityForResult(intent, 1905);
                    }
                }
                break;
            case IntroActivity.TYPE_WRITE_SETTINGS:
                @SuppressLint("InlinedApi") Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                startActivityForResult(intent, 666);
                break;
        }
    }

    @Override
    public int backgroundColor() {
        if (type == IntroActivity.TYPE_DRAW)
            return R.color.second_slide_background;
        return R.color.custom_slide_background;
    }

    @Override
    public int buttonsColor() {
        if (type == IntroActivity.TYPE_DRAW)
            return R.color.second_slide_buttons;
        return R.color.custom_slide_buttons;
    }

    @Override
    public boolean canMoveFurther() {
        return canMoveFurther;
    }

    @Override
    public String cantMoveFurtherErrorMessage() {
        return getString(R.string.permission_error);
    }
}
