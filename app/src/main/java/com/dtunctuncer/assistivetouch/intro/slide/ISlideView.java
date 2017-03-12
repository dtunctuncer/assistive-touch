package com.dtunctuncer.assistivetouch.intro.slide;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

public interface ISlideView {
    void initViews(@DrawableRes int image, @StringRes int title, @StringRes int body);
}
