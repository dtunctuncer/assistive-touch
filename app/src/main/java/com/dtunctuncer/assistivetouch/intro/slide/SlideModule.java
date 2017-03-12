package com.dtunctuncer.assistivetouch.intro.slide;

import dagger.Module;
import dagger.Provides;

@Module
public class SlideModule {
    private ISlideView view;

    public SlideModule(ISlideView view) {
        this.view = view;
    }

    @Provides
    ISlideView provideView() {
        return view;
    }
}
