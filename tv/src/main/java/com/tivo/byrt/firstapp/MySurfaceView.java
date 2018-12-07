package com.tivo.byrt.firstapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import static android.view.accessibility.AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CLEAR_FOCUS;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CLEAR_SELECTION;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_COLLAPSE;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_COPY;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CUT;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_DISMISS;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_EXPAND;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_FOCUS;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_LONG_CLICK;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_NEXT_AT_MOVEMENT_GRANULARITY;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_NEXT_HTML_ELEMENT;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_PASTE;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_PREVIOUS_HTML_ELEMENT;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_FORWARD;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_SELECT;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_SELECTION;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT;

public class MySurfaceView extends SurfaceView implements View.OnKeyListener{
    // Messing around with SurfaceView drawing
    private SurfaceHolder holder;
    private Bitmap icon;
    private Point position = new Point(0,0);
    private int frame = 17;
    private int incrementX = 5;
    private int incrementY = 5;

    private Timer timer;
    private TimerTask task;

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        icon = BitmapFactory.decodeResource(getResources(), R.drawable.lb_ic_play);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startTimer();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stopTimer();
            }
        });

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("Surface", "dispatchKeyEvent("+KeyEvent.keyCodeToString(event.getKeyCode())+"_"+event.getAction()+")");
        boolean handled = handleKeyEvent(event);
        if (!handled) {
            return super.dispatchKeyEvent(event);
        }
        return handled;
    }

    private boolean handleKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                incrementY = Math.abs(incrementY);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                incrementY = -Math.abs(incrementY);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                incrementX = -Math.abs(incrementX);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                incrementX = Math.abs(incrementX);
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (timer != null) {
                        stopTimer();
                    } else {
                        startTimer();
                    }
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.d("Surface", "onKey("+KeyEvent.keyCodeToString(event.getKeyCode())+"_"+event.getAction()+")");
        return handleKeyEvent(event);
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        Log.d("Surface", "performAccessibilityAction("+getActionSymbolicName(action)+", "+arguments+")");
        return super.performAccessibilityAction(action, arguments);
    }

    private static String getActionSymbolicName(int action) {
        switch (action) {
            case ACTION_FOCUS:
                return "ACTION_FOCUS";
            case ACTION_CLEAR_FOCUS:
                return "ACTION_CLEAR_FOCUS";
            case ACTION_SELECT:
                return "ACTION_SELECT";
            case ACTION_CLEAR_SELECTION:
                return "ACTION_CLEAR_SELECTION";
            case ACTION_CLICK:
                return "ACTION_CLICK";
            case ACTION_LONG_CLICK:
                return "ACTION_LONG_CLICK";
            case ACTION_ACCESSIBILITY_FOCUS:
                return "ACTION_ACCESSIBILITY_FOCUS";
            case ACTION_CLEAR_ACCESSIBILITY_FOCUS:
                return "ACTION_CLEAR_ACCESSIBILITY_FOCUS";
            case ACTION_NEXT_AT_MOVEMENT_GRANULARITY:
                return "ACTION_NEXT_AT_MOVEMENT_GRANULARITY";
            case ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY:
                return "ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY";
            case ACTION_NEXT_HTML_ELEMENT:
                return "ACTION_NEXT_HTML_ELEMENT";
            case ACTION_PREVIOUS_HTML_ELEMENT:
                return "ACTION_PREVIOUS_HTML_ELEMENT";
            case ACTION_SCROLL_FORWARD:
                return "ACTION_SCROLL_FORWARD";
            case ACTION_SCROLL_BACKWARD:
                return "ACTION_SCROLL_BACKWARD";
            case ACTION_CUT:
                return "ACTION_CUT";
            case ACTION_COPY:
                return "ACTION_COPY";
            case ACTION_PASTE:
                return "ACTION_PASTE";
            case ACTION_SET_SELECTION:
                return "ACTION_SET_SELECTION";
            case ACTION_EXPAND:
                return "ACTION_EXPAND";
            case ACTION_COLLAPSE:
                return "ACTION_COLLAPSE";
            case ACTION_DISMISS:
                return "ACTION_DISMISS";
            case ACTION_SET_TEXT:
                return "ACTION_SET_TEXT";
//            case R.id.accessibilityActionShowOnScreen:
//                return "ACTION_SHOW_ON_SCREEN";
//            case R.id.accessibilityActionScrollToPosition:
//                return "ACTION_SCROLL_TO_POSITION";
//            case R.id.accessibilityActionScrollUp:
//                return "ACTION_SCROLL_UP";
//            case R.id.accessibilityActionScrollLeft:
//                return "ACTION_SCROLL_LEFT";
//            case R.id.accessibilityActionScrollDown:
//                return "ACTION_SCROLL_DOWN";
//            case R.id.accessibilityActionScrollRight:
//                return "ACTION_SCROLL_RIGHT";
//            case R.id.accessibilityActionSetProgress:
//                return "ACTION_SET_PROGRESS";
//            case R.id.accessibilityActionContextClick:
//                return "ACTION_CONTEXT_CLICK";
            default:
                return "ACTION_UNKNOWN";
        }
    }

    private void startTimer() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                draw();
            }
        };
        timer.schedule(task, 10, frame);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void draw() {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            if ((position.x+=incrementX) > getWidth() || position.x < 0) {
                incrementX = -incrementX;
            }
            if ((position.y+=incrementY) > getHeight() || position.y < 0) {
                incrementY = -incrementY;
            }
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(icon, position.x, position.y, null);
            holder.unlockCanvasAndPost(canvas);
        }
    }
}