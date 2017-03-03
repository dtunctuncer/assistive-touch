package com.dtunctuncer.assistivetouch.touchboard;

import android.animation.Animator;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.dtunctuncer.assistivetouch.App;
import com.dtunctuncer.assistivetouch.R;

import javax.inject.Inject;

import timber.log.Timber;

public class TouchService extends Service {

    public static final String SERVICE_SP_KEY = "service";
    @Inject
    SharedPreferences.Editor editor;
    private boolean isFlashOn = false;
    private WindowManager windowManager;
    private ImageView assistiveTouch;
    private View touchBoard;
    private FrameLayout touchboardCenter;
    private WindowManager.LayoutParams touchboardParams;
    private AudioManager audioManager;

    private void openTouch() {
        assistiveTouch.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
        assistiveTouch.setAlpha(0.5f);
        assistiveTouch.setVisibility(View.VISIBLE);
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
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        initAssistiveTouch();
        initTouchBoard();
        setServiceRunning(true);

    }

    private void setServiceRunning(boolean isRunning) {
        editor.putBoolean(SERVICE_SP_KEY, isRunning);
        editor.apply();
    }

    private void initTouchBoard() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        touchBoard = inflater.inflate(R.layout.touch_board, null, false);

        initSoundSeekBar();

        initFlash();

        initBrightnessSeekBar();


        touchBoard.findViewById(R.id.touchBoardMain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTouchBoard();
            }
        });

        touchboardParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        touchboardParams.gravity = Gravity.CENTER;
    }

    private void initBrightnessSeekBar() {
        final SeekBar brightSeekBar = (SeekBar) touchBoard.findViewById(R.id.brightSeekbar);
        brightSeekBar.setEnabled(false);
        brightSeekBar.setMax(255);
        brightSeekBar.setKeyProgressIncrement(50);

        touchBoard.findViewById(R.id.brightUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brightSeekBar.setProgress(brightSeekBar.getProgress() + 50);
            }
        });


        touchBoard.findViewById(R.id.brightDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brightSeekBar.setProgress(brightSeekBar.getProgress() - 50);
            }
        });

        ContentResolver contentResolver = getContentResolver();


        try {
            int brightness = android.provider.Settings.System.getInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS);
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
                    Camera camera = Camera.open();
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
                        isFlashOn = false;
                    }
                }
            }
        });

    }

    private void initSoundSeekBar() {
        final SeekBar seekBar = ((SeekBar) touchBoard.findViewById(R.id.soundSeekbar));
        touchboardCenter = (FrameLayout) touchBoard.findViewById(R.id.touchboardCenter);
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
                seekBar.setProgress(seekBar.getProgress() + 1);
            }
        });

        touchBoard.findViewById(R.id.volumeDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(seekBar.getProgress() - 1);
            }
        });
    }

    private void initAssistiveTouch() {
        assistiveTouch = new ImageView(this);
        assistiveTouch.setImageResource(R.drawable.touch_icon);
        assistiveTouch.setClickable(true);
        assistiveTouch.setAlpha(0.5f);

        int dimensionInDp = getDp(48);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                dimensionInDp,
                dimensionInDp,
                WindowManager.LayoutParams.TYPE_TOAST,
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
        touchboardCenter.animate().scaleX(1.0f).scaleY(1.0f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).setDuration(200).start();
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
        super.onDestroy();
    }
}