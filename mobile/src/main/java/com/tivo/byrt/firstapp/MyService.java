package com.tivo.byrt.firstapp;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class MyService extends AccessibilityService {

    private static final String TAG = "BYRT";

    @Override
    public void onCreate() {
        Log.i(TAG, "MyAccessibilityService: onCreate() : " + this.getApplicationInfo().packageName);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "MyAccessibilityService: onAccessibilityEvent("+AccessibilityEvent.eventTypeToString(event.getEventType())+")");

    }


    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.i(TAG, "MyAccessibilityService: onKeyEvent("+KeyEvent.keyCodeToString(event.getKeyCode())+")");
        return super.onKeyEvent(event);
    }

    @Override
    protected void onServiceConnected()
    {
        Log.i(TAG, "MyAccessibilityService: onServiceConnected()");
        super.onServiceConnected();
    }

    @Override
    public void onInterrupt() {
        /* Ignore */
    }
}
