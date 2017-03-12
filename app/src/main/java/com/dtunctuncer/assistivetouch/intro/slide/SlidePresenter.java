package com.dtunctuncer.assistivetouch.intro.slide;

import com.dtunctuncer.assistivetouch.R;
import com.dtunctuncer.assistivetouch.core.PermissionTypes;

import javax.inject.Inject;

public class SlidePresenter {

    private ISlideView view;

    @Inject
    public SlidePresenter(ISlideView view) {
        this.view = view;
    }

    public void checkTypeForView(int type) {
        switch (type) {
            case PermissionTypes.TYPE_DRAW:
                view.initViews(R.drawable.ic_image_black_24dp, R.string.draw_permission, R.string.draw_explanation);
                break;
            case PermissionTypes.TYPE_WRITE_SETTINGS:
                view.initViews(R.drawable.ic_settings_black_24dp, R.string.write_settings, R.string.write_settings_explanation);
                break;
            case PermissionTypes.TYPE_DEVICE_ADMIN:
                view.initViews(R.drawable.ic_perm_device_information_black_24dp, R.string.device_admin_permission_title, R.string.device_admin_permission);
                break;
        }
    }
}
