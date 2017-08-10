package easytouch.demooverlay1;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

/**
 * Created by VuDuc on 8/7/2017.
 */

public class MyService extends Service {

    WindowManager.LayoutParams params;
    WindowManager.LayoutParams bottomParams;
    WindowManager.LayoutParams backgroundParams;
    Animation inAnimation;
    Animation outAnimation;
    Context mcontext;
    private WindowManager windowManager;
    private View overlayView;
    private View overlayBottom;
    private View overlayBackground;

    @Override
    public void onCreate() {
        super.onCreate();
        mcontext = this;
        initAnimations();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        addOverlayView();
        return super.onStartCommand(intent, flags, startId);
    }

    private void addOverlayView() {
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * 0.95f);
        params = new WindowManager.LayoutParams(
                width,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER | Gravity.BOTTOM;
        params.x = 0;
        params.y = 20;

        overlayView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.floating_view, null);

        //Phần dưới màn hình
        bottomParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        bottomParams.gravity = Gravity.BOTTOM;
        overlayBottom = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.floating_view2, null);

        //Cho phần backgound
        backgroundParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        backgroundParams.gravity = Gravity.CENTER;
        overlayBackground = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.floating_view3, null);

        windowManager.addView(overlayBackground, backgroundParams);
        windowManager.addView(overlayBottom, bottomParams);
        windowManager.addView(overlayView, params);

        overlayView.setOnTouchListener(new View.OnTouchListener() {
            long startTime = System.currentTimeMillis();
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private float startY;

            private Rect rect;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        startY = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endY = motionEvent.getY();
                        if (endY > startY && endY - startY > 150) {
                            //Move down
                            overlayView.startAnimation(outAnimation);
                            overlayView.setVisibility(View.GONE);
                            overlayBackground.setVisibility(View.GONE);
                            overlayBottom.setVisibility(View.VISIBLE);
                        }

                        params.y = 20;
                        windowManager.updateViewLayout(overlayView, params);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX - (int) (motionEvent.getRawX() - initialTouchX);
                        params.y = initialY - (int) (motionEvent.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(overlayView, params);
                        break;
                }
                params.x = 0;
                windowManager.updateViewLayout(overlayView, params);
                return false;
            }
        });

        overlayBottom.setOnTouchListener(new View.OnTouchListener() {

            private float starty;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        starty = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_UP: {
                        float endY = motionEvent.getY();
                        if (endY < starty) {
                            //Move up
                            overlayView.startAnimation(inAnimation);
                            overlayView.setVisibility(View.VISIBLE);
                            overlayBackground.setVisibility(View.VISIBLE);
                            overlayBottom.setVisibility(View.GONE);
                        }
                    }
                }
                return false;
            }
        });

        overlayBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        overlayView.startAnimation(outAnimation);
                        overlayView.setVisibility(View.GONE);
                        overlayBackground.setVisibility(View.GONE);
                        overlayBottom.setVisibility(View.VISIBLE);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(overlayBackground);
            wm.removeView(overlayView);
            wm.removeView(overlayBottom);
        }
    }

    private void initAnimations() {
        inAnimation = AnimationUtils.loadAnimation(mcontext, R.anim.in_animation);
        outAnimation = AnimationUtils.loadAnimation(mcontext, R.anim.out_animation);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
