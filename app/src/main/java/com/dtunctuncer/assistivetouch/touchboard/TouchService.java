package com.dtunctuncer.assistivetouch.touchboard;

import android.Manifest;
import android.animation.Animator;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.dtunctuncer.assistivetouch.App;
import com.dtunctuncer.assistivetouch.R;
import com.dtunctuncer.assistivetouch.core.AnalyticsEvents;
import com.dtunctuncer.assistivetouch.permission.AdminReceiver;
import com.dtunctuncer.assistivetouch.permission.PermissionHelperActivity;
import com.dtunctuncer.assistivetouch.utils.RxBus;
import com.dtunctuncer.assistivetouch.utils.analytics.AnalyticsUtils;
import com.dtunctuncer.assistivetouch.utils.events.CloseTouchBoardEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;
import rx.internal.schedulers.ExecutorScheduler;
import timber.log.Timber;

public class TouchService extends Service {

    public static final String SERVICE_SP_KEY = "service";

    @Inject
    SharedPreferences.Editor editor;
    @Inject
    RxBus rxBus;
    @Inject
    Context appContext;

    private boolean isFlashOn = false;
    private WindowManager windowManager;
    private ImageView assistiveTouch;
    private View touchBoard;
    private LinearLayout touchboardCenter;
    private WindowManager.LayoutParams touchboardParams;
    private Subscription subscription;
    private Camera camera;

    private void openTouch() {
        assistiveTouch.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
        assistiveTouch.setAlpha(0.5f);
    }

    private void closeTouch() {
        assistiveTouch.animate().scaleX(0f).scaleY(0f).setDuration(200).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int getDp(int pixel) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, getResources().getDisplayMetrics());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.getComponent().inject(this);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        initAssistiveTouch();
        initTouchBoard();
        setServiceRunning(true);
        subscribe();

    }

    private void subscribe() {
        subscription = rxBus.toObserverable()
                .subscribeOn(new ExecutorScheduler(AsyncTask.THREAD_POOL_EXECUTOR))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (o instanceof CloseTouchBoardEvent) {
                            closeTouchBoard();
                        }
                    }
                });
    }

    private void setServiceRunning(boolean isRunning) {
        editor.putBoolean(SERVICE_SP_KEY, isRunning);
        editor.apply();
    }

    private void initTouchBoard() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        touchBoard = inflater.inflate(R.layout.touch_board, null, false);

        //region initButtons
        initSoundSeekBar();

        initFlash();

        initBrightnessSeekBar();

        intCamera();

        initCalculator();

        initAlarm();

        initAutoRotate();

        initHomeScreen();

        initLockScreen();

        initWifi();
        //endregion

        touchBoard.findViewById(R.id.touchBoardMain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTouchBoard();
            }
        });

        touchboardParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        touchboardParams.gravity = Gravity.CENTER;

//        windowManager.addView(touchBoard, touchboardParams);
    }

    //region initButtons
    private void initWifi() {
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        final ImageView wifi = (ImageView) touchBoard.findViewById(R.id.wifi);

        if (wifiManager.isWifiEnabled())
            wifi.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_signal_wifi_off_black_24dp));
        else
            wifi.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_network_wifi_black_24dp));


        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.isWifiEnabled()) {
                    wifi.setImageDrawable(ContextCompat.getDrawable(TouchService.this, R.drawable.ic_network_wifi_black_24dp));
                    wifiManager.setWifiEnabled(false);
                } else {
                    wifi.setImageDrawable(ContextCompat.getDrawable(TouchService.this, R.drawable.ic_signal_wifi_off_black_24dp));
                    wifiManager.setWifiEnabled(true);
                }
            }
        });
    }

    private void initLockScreen() {
        final DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        final ComponentName componentName = new ComponentName(appContext, AdminReceiver.class);
        touchBoard.findViewById(R.id.lock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (policyManager.isAdminActive(componentName)) {
                    policyManager.lockNow();
                } else {
                    Intent intent = new Intent(TouchService.this, PermissionHelperActivity.class);
                    intent.putExtra("message", getString(R.string.device_admin_permission));
                    intent.putExtra("type", 2);
                    startActivity(intent);
                }
                closeTouchBoard();
            }
        });
    }

    private void initHomeScreen() {
        touchBoard.findViewById(R.id.mainMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Click Home Screen Button", "Home Screen butonuna tıklandı");
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(startMain);
                closeTouchBoard();
            }
        });
    }

    private void initAutoRotate() {

        try {
            int rotation = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            if (rotation == 1) {
                ((ImageView) touchBoard.findViewById(R.id.autoRotate)).setImageDrawable(ContextCompat.getDrawable(TouchService.this, R.drawable.ic_stay_current_portrait_black_24dp));
            } else {
                ((ImageView) touchBoard.findViewById(R.id.autoRotate)).setImageDrawable(ContextCompat.getDrawable(TouchService.this, R.drawable.ic_screen_rotation_black_24dp));
            }
        } catch (Settings.SettingNotFoundException e) {
            Timber.e(e);
        }


        touchBoard.findViewById(R.id.autoRotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Click Auto Rotate Button", "Auto Rotate butonuna tıklandı");
                if (checkSystemWritePermission()) {
                    int rotation = 0;

                    try {
                        rotation = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
                    } catch (Settings.SettingNotFoundException e) {
                        Timber.e(e);
                    }

                    if (rotation == 1) {
                        ((ImageView) touchBoard.findViewById(R.id.autoRotate)).setImageDrawable(ContextCompat.getDrawable(TouchService.this, R.drawable.ic_stay_current_portrait_black_24dp));
                    } else {
                        ((ImageView) touchBoard.findViewById(R.id.autoRotate)).setImageDrawable(ContextCompat.getDrawable(TouchService.this, R.drawable.ic_screen_rotation_black_24dp));
                    }

                    Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, rotation == 0 ? 1 : 0);
                } else {
                    Intent intent = new Intent(TouchService.this, PermissionHelperActivity.class);
                    intent.putExtra("type", 1);
                    intent.putExtra("message", getString(R.string.system_write_permission));
                    startActivity(intent);
                    closeTouchBoard();
                }
            }
        });
    }

    private void initAlarm() {
        touchBoard.findViewById(R.id.alarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Click Alarm Button", "Alarm butonuna tıklandı");
                Intent openClockIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
                openClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(openClockIntent);
                closeTouchBoard();
            }
        });
    }

    private void initCalculator() {

        final ArrayList<HashMap<String, Object>> items = new ArrayList<>();

        final PackageManager pm = getPackageManager();
        List<PackageInfo> packs = pm.getInstalledPackages(0);
        for (PackageInfo pi : packs) {
            if (pi.packageName.toLowerCase().contains("calcul")) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("appName", pi.applicationInfo.loadLabel(pm));
                map.put("packageName", pi.packageName);
                items.add(map);
            }
        }
        touchBoard.findViewById(R.id.calculator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Click Calculator Button", "Hesap makinesi butonuna tıklandı");
                if (items.size() >= 1) {
                    String packageName = (String) items.get(0).get("packageName");
                    Intent i = pm.getLaunchIntentForPackage(packageName);
                    if (i != null)
                        startActivity(i);
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_APP_CALCULATOR);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                closeTouchBoard();
            }
        });
    }

    private void intCamera() {
        touchBoard.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Click Camera Button", "Camera butonuna tıklandı");
                int permissionCheck = ContextCompat.checkSelfPermission(TouchService.this, Manifest.permission.CAMERA);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    closeTouchBoard();
                } else {
                    Intent intent = new Intent(TouchService.this, PermissionHelperActivity.class);
                    intent.putExtra("permission_name", Manifest.permission.CAMERA);
                    intent.putExtra("message", getString(R.string.camera_permission));
                    intent.putExtra("type", 0);
                    startActivity(intent);
                    closeTouchBoard();
                }
            }
        });

    }

    private void initBrightnessSeekBar() {
        final SeekBar brightSeekBar = (SeekBar) touchBoard.findViewById(R.id.brightSeekbar);
        brightSeekBar.setEnabled(false);
        brightSeekBar.setMax(255);
        brightSeekBar.setKeyProgressIncrement(50);

        touchBoard.findViewById(R.id.brightUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Click Brightness Up Button", "Parlaklık artırma butonuna tıklandı");
                brightSeekBar.setProgress(brightSeekBar.getProgress() + 50);
            }
        });


        touchBoard.findViewById(R.id.brightDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Click Brightness Down Button", "Parlaklık azaltma butonuna tıklandı");
                brightSeekBar.setProgress(brightSeekBar.getProgress() - 50);
            }
        });


        try {
            int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            brightSeekBar.setProgress(brightness);
        } catch (Settings.SettingNotFoundException e) {
            Timber.e(e);
        }

        final Intent intent = new Intent(this, BrightnessHelperActivity.class);


        brightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 20) {
                    intent.putExtra("brightness", 20);
                } else {
                    intent.putExtra("brightness", progress);
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                TouchService.this.startActivity(intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void initFlash() {
        touchBoard.findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Click Flash Button", "Flash butonuna tıklandı");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    try {
                        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                        String cameraId = camManager.getCameraIdList()[0];
                        if (!isFlashOn) {
                            camManager.setTorchMode(cameraId, true);
                            isFlashOn = true;
                        } else {
                            camManager.setTorchMode(cameraId, false);
                            isFlashOn = false;
                        }

                    } catch (Exception e) {
                        Timber.e(e);
                    }
                } else {
                    if (camera == null)
                        camera = Camera.open();
                    Camera.Parameters parameters = camera.getParameters();
                    camera.startPreview();
                    if (!isFlashOn) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        camera.setParameters(parameters);
                        camera.startPreview();
                        isFlashOn = true;
                    } else {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        camera.setParameters(parameters);
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                        isFlashOn = false;
                    }
                }
            }
        });

    }

    private void initSoundSeekBar() {
        final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final SeekBar seekBar = ((SeekBar) touchBoard.findViewById(R.id.soundSeekbar));
        touchboardCenter = (LinearLayout) touchBoard.findViewById(R.id.touchboardCenter);
        try {
            seekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
            seekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
            seekBar.setEnabled(false);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, progress, 0);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        } catch (Exception e) {
            Timber.e(e);
        }
        touchBoard.findViewById(R.id.volumeUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Click Sound Up Button", "Ses arttırma butonuna tıklandı");
                seekBar.setProgress(seekBar.getProgress() + 1);
            }
        });

        touchBoard.findViewById(R.id.volumeDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnalyticsUtils.trackEvent(AnalyticsEvents.CLIKCK_EVENT, "Click Sound Down Button", "Ses azaltma butonuna tıklandı");
                seekBar.setProgress(seekBar.getProgress() - 1);
            }
        });
    }
    //endregion

    private void initAssistiveTouch() {
        assistiveTouch = new ImageView(this);
        assistiveTouch.setImageResource(R.drawable.touch_icon);
        assistiveTouch.setClickable(true);
        assistiveTouch.setAlpha(0.5f);

        int dimensionInDp = getDp(48);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                dimensionInDp,
                dimensionInDp,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;


        assistiveTouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTouch();
                openTouchBoard();
            }
        });

        assistiveTouch.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        assistiveTouch.setAlpha(1f);
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                    case MotionEvent.ACTION_UP:
                        assistiveTouch.setAlpha(0.5f);
                        return !(event.getRawX() == initialTouchX && event.getRawY() == initialTouchY);
                    case MotionEvent.ACTION_MOVE:
                        assistiveTouch.setAlpha(1f);
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(assistiveTouch, params);
                        return true;
                }
                return false;
            }
        });
        windowManager.addView(assistiveTouch, params);
    }

    private void closeTouchBoard() {
        touchboardCenter.animate().scaleX(0f).scaleY(0f).setDuration(200).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                openTouch();
                try {
                    if (touchBoard != null) {
                        windowManager.removeView(touchBoard);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

    private void openTouchBoard() {
        windowManager.addView(touchBoard, touchboardParams);

        touchboardCenter.animate().scaleX(1.0f).scaleY(1.0f).setListener(null).setDuration(200).start();
    }

    private boolean checkSystemWritePermission() {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(this);
        }
        return retVal;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            if (assistiveTouch != null) windowManager.removeView(assistiveTouch);
            if (touchBoard != null) windowManager.removeView(touchBoard);
        } catch (Exception e) {
            Timber.d(e);
        }
        setServiceRunning(false);
        if (subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        Toast.makeText(appContext, "destroy", Toast.LENGTH_SHORT).show();
        Log.e("TOUCH", "onDestroy: ");
        super.onDestroy();
    }
}