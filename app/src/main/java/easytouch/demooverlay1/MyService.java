package easytouch.demooverlay1;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import static android.content.ContentValues.TAG;

/**
 * Created by VuDuc on 8/7/2017.
 */

public class MyService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private View overlayBackGround;
    WindowManager.LayoutParams params;
    WindowManager.LayoutParams backgoundParams;

    private Animation animVisible;
    private Animation animGone;

    @Override
    public void onCreate() {
        super.onCreate();
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
        params.y = -300;

        overlayView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.floating_view, null);

        animVisible = AnimationUtils.loadAnimation(overlayView.getContext(), R.anim.translate_in);
        animGone = AnimationUtils.loadAnimation(overlayView.getContext(), R.anim.translate_out);


        backgoundParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        backgoundParams.gravity = Gravity.BOTTOM;
        overlayBackGround = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.floating_view2, null);

        windowManager.addView(overlayBackGround, backgoundParams);
        windowManager.addView(overlayView, params);

        overlayView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            long startTime = System.currentTimeMillis();

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
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

        overlayBackGround.setOnTouchListener(new View.OnTouchListener() {
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
                            overlayView.setVisibility(View.VISIBLE);
                            overlayView.animate()
                                    .translationYBy(0)
                                    .translationY(-overlayView.getHeight())
                                    .setDuration(3000)
                                    .setListener(null);
                        } else {
                            overlayView.startAnimation(animGone);
                            overlayView.setVisibility(View.GONE);
                        }
                    }
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
            wm.removeView(overlayView);
            wm.removeView(overlayBackGround);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
