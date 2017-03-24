package com.dtunctuncer.assistivetouch.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;

import com.dtunctuncer.assistivetouch.App;
import com.dtunctuncer.assistivetouch.R;
import com.dtunctuncer.assistivetouch.core.AnalyticsEvents;
import com.dtunctuncer.assistivetouch.touchboard.TouchService;
import com.dtunctuncer.assistivetouch.utils.analytics.AnalyticsUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements IMainView {

    @Inject
    MainPresenter presenter;

    @BindView(R.id.serviceButton)
    Button serviceButton;
    @BindView(R.id.activity_main)
    LinearLayout activityMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        DaggerMainComponent.builder().mainModule(new MainModule(this)).applicationComponent(App.getComponent()).build().inject(this);
        presenter.checkServiceRunning();
        presenter.setFistOpen();
    }

    @OnClick(R.id.serviceButton)
    public void clickServiceButton() {
        if (serviceButton.getText().equals(getString(R.string.start))) {
            AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Start Service", "Start service butonuna basıldı");
            moveTaskToBack(true);
            startService(new Intent(this, TouchService.class));
            serviceButton.setText(R.string.stop);
            exit();
        } else {
            AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Stop Service", "Stop service butonuna basıldı");
            stopService(new Intent(this, TouchService.class));
            serviceButton.setText(R.string.start);
        }
    }

    @Override
    public void changeStartButtonText(String contextString) {
        serviceButton.setText(contextString);
    }

    private void exit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            finishAndRemoveTask();
        else
            finish();
    }
}