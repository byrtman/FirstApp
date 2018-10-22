package com.tivo.byrt.firstapp;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class MyService extends AccessibilityService {

    private static final String TAG = "MyService";

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate() : " + this.getApplicationInfo().packageName);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "onAccessibilityEvent("+eventInfo(event)+")");

    }

    private String eventInfo(AccessibilityEvent event) {
        String out = AccessibilityEvent.eventTypeToString(event.getEventType())+"\n  "+event.getPackageName();

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            out += "\n  "+event.getSource().getClassName()+"\n  "+event.getText();
        }
        return out;
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.i(TAG, "onKeyEvent("+KeyEvent.keyCodeToString(event.getKeyCode())+")");
        return super.onKeyEvent(event);
    }

    @Override
    protected void onServiceConnected()
    {
        Log.i(TAG, "onServiceConnected()");
        super.onServiceConnected();
    }

    @Override
    public void onInterrupt() {
        /* Ignore */
    }
}
