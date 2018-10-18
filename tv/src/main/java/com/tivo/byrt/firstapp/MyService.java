package com.tivo.byrt.firstapp;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class MyService extends AccessibilityService {

    private static final String TAG = "BYRT";

    @Override
    public void onCreate() {
        Log.i(TAG, "MyService: onCreate() : \n" + this.getApplicationInfo());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "MyService: onAccessibilityEvent("+event+")");

    }
    @Override
    protected void onServiceConnected()
    {
        Log.i(TAG, "MyService: onServiceConnected()");
        super.onServiceConnected();
    }

    @Override
    public void onInterrupt() {
        /* Ignore */
    }
}
