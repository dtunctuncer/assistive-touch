package com.dtunctuncer.assistivetouch.intro.slide;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtunctuncer.assistivetouch.R;
import com.dtunctuncer.assistivetouch.core.PermissionTypes;
import com.dtunctuncer.assistivetouch.permission.AdminReceiver;

import javax.inject.Inject;

import agency.tango.materialintroscreen.SlideFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomSlideFragment extends SlideFragment implements ISlideView {

    @Inject
    SlidePresenter presenter;

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
        DaggerSlideComponent.builder().slideModule(new SlideModule(this)).build().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_custom_slide, container, false);
        ButterKnife.bind(this, view);
        presenter.checkTypeForView(type);
        return view;
    }

    @Override
    public void initViews(@DrawableRes int image, @StringRes int title, @StringRes int body, @ColorRes int button) {
        this.image.setImageResource(image);
        this.title.setText(title);
        this.body.setText(body);
        this.requestButton.setBackgroundColor(ContextCompat.getColor(getActivity(), button));
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (1905 == requestCode && Settings.canDrawOverlays(getActivity())) {
            grantPermission();
        } else if (666 == requestCode && Settings.System.canWrite(getActivity())) {
            grantPermission();
        } else if (15 == requestCode && Activity.RESULT_OK == resultCode) {
            grantPermission();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void grantPermission() {
        canMoveFurther = true;
        requestButton.setVisibility(View.GONE);
    }

    @SuppressLint("NewApi")
    @OnClick(R.id.requestButton)
    public void requestPermission() {
        switch (type) {
            case PermissionTypes.TYPE_DRAW:
                if (!Settings.canDrawOverlays(getActivity())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
                    startActivityForResult(intent, 1905);
                }
                break;
            case PermissionTypes.TYPE_WRITE_SETTINGS:
                @SuppressLint("InlinedApi") Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                startActivityForResult(intent, 666);
                break;
            case PermissionTypes.TYPE_DEVICE_ADMIN:
                ComponentName componentName = new ComponentName(getActivity(), AdminReceiver.class);
                Intent deviceAdmin = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                deviceAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                deviceAdmin.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_permission));
                startActivityForResult(deviceAdmin, 15);
                break;
        }
    }

    @Override
    public int backgroundColor() {
        if (type == PermissionTypes.TYPE_DRAW)
            return R.color.second_slide_background;
        else if (type == PermissionTypes.TYPE_DEVICE_ADMIN)
            return R.color.third_slide_background;
        return R.color.custom_slide_background;
    }

    @Override
    public int buttonsColor() {
        if (type == PermissionTypes.TYPE_DRAW)
            return R.color.second_slide_buttons;
        else if (type == PermissionTypes.TYPE_DEVICE_ADMIN)
            return R.color.third_slide_buttons;
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