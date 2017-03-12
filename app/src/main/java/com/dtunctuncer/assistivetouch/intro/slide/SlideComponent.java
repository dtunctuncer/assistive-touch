package com.dtunctuncer.assistivetouch.intro.slide;

import dagger.Component;

@Component(modules = {SlideModule.class})
public interface SlideComponent {
    void inject(CustomSlideFragment customSlideFragment);
}
