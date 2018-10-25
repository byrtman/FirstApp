/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.tivo.byrt.firstapp;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_MULTIPLE;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_BACK;

/*
 * MainActivity class that loads {@link MainLayout}.
 */
public class MainActivity extends Activity {

    private static final String TAG = "BYRT";
    private WebView mWebView;
    private SurfaceView mSurfaceView;
    private ViewGroup mLayout;

    private Button mTalkbackButton;
    private Button mWebviewButton;
    private TalkBackHandlerView mTalkBackHandler;

    private View.OnKeyListener mOnKeyListener;
    private View.OnFocusChangeListener mOnFocusListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = findViewById(R.id.mainLayout);
        mSurfaceView = findViewById(R.id.surfaceView);
        mTalkbackButton = findViewById(R.id.buttonTalkBack);
        mWebviewButton = findViewById(R.id.buttonWeb);
        mTalkBackHandler = findViewById(R.id.vPad);


        mOnKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d("KEY", "  SurfaceView onKey("+KeyEvent.keyCodeToString(keyCode)+") received");
                    if (v != null) {
                        Log.d(TAG, v.toString());
                        Toast.makeText( getApplicationContext(), KeyEvent.keyCodeToString(keyCode), Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        };

        mOnFocusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("FOCUS", "onFocusChange() "+getViewName(v)+" now has focus: " + hasFocus);
                if (v != mSurfaceView && hasFocus) {
                    mTalkBackHandler.disableVirtualNavigation();
                }
                else if (hasFocus) {
                    mTalkBackHandler.enableVirtualNavigation();
                }
            }
        };

        mLayout.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                Log.w(TAG, "onChildViewAdded("+parent+", "+child+")" );
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                Log.w(TAG, "onChildViewRemoved("+parent+", "+child+")" );

            }
        });

        mSurfaceView.setOnKeyListener(mOnKeyListener);
        mSurfaceView.setOnFocusChangeListener(mOnFocusListener);
        mWebviewButton.setOnFocusChangeListener(mOnFocusListener);
        mTalkbackButton.setOnFocusChangeListener(mOnFocusListener);
    }

    public static String getViewName(View v) {
        return v.getTag() != null ? (String) v.getTag() : v.getClass().getName();
    }

    private void outputFocusedViewParent() {
        for(int i = 0; i < mLayout.getChildCount(); i++) {
            View v = mLayout.getChildAt(i);
            if (v.hasFocus()) {
                Log.i(TAG,"MainLayout child view has focus : "+getViewName(v));
            }
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        if (mWebView != null) {
            mWebView.removeAllViews();
            mLayout.removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.d("KEY", "MainActivity: dispatchKeyEvent("+outputKeyEvent(event)+")");
            outputFocusedViewParent();
        }
        if (event.getKeyCode() == KEYCODE_BACK) {
            mTalkbackButton.requestFocus();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    private String outputKeyEvent(KeyEvent event) {
        return KeyEvent.keyCodeToString(event.getKeyCode()) + " : " +
                actionToString(event.getAction()) + " : " +
                event.getDevice().getName() + " : " +
                event.getSource();
    }

    public static String actionToString(int action) {
        switch (action) {
            case ACTION_DOWN:
                return "ACTION_DOWN";
            case ACTION_UP:
                return "ACTION_UP";
            case ACTION_MULTIPLE:
                return "ACTION_MULTIPLE";
            default:
                return Integer.toString(action);
        }
    }

    public void toggleTalkBackState(View view) {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        Toast.makeText( getApplicationContext(), "Ouch!", Toast.LENGTH_SHORT).show();
        if (am.isEnabled()) {
            Log.d(TAG, "Turning OFF TalkBack...");
            disableTalkBack();
        }
        else {
            Log.d(TAG, "Turning ON TalkBack...");
            enableTalkBack();
        }

    }

    public void toggleWebView(View view) {
        Log.d(TAG, "toggleWebView");
        if (mWebView == null) {
            mWebView = new WebView(this);
            mWebView.setLayoutParams(new ViewGroup.LayoutParams( mSurfaceView.getWidth(), mSurfaceView.getHeight()));
            mWebView.setOnKeyListener(mOnKeyListener);
            mWebView.setOnFocusChangeListener(mOnFocusListener);
            mWebView.loadUrl("http://developer.android.com");
            mLayout.addView(mWebView);
        }
        else {
            mWebView.removeAllViews();
            mLayout.removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
    }

    private void enableTalkBack() {
        try {
            AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
            List<AccessibilityServiceInfo> services = am.getInstalledAccessibilityServiceList();

            if (services.isEmpty()) {
                Log.e(TAG, "No accessibility service available to enable.");
                return;
            }

            AccessibilityServiceInfo service = services.get(0);

            boolean enableTouchExploration = (service.flags
                    & AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE) != 0;
            // Try to find a service supporting explore by touch.
            if (!enableTouchExploration) {
                final int serviceCount = services.size();
                for (int i = 1; i < serviceCount; i++) {
                    AccessibilityServiceInfo candidate = services.get(i);
                    if ((candidate.flags & AccessibilityServiceInfo
                            .FLAG_REQUEST_TOUCH_EXPLORATION_MODE) != 0) {
                        enableTouchExploration = true;
                        service = candidate;
                        break;
                    }
                }
            }

            ServiceInfo serviceInfo = service.getResolveInfo().serviceInfo;
            ComponentName componentName = new ComponentName(serviceInfo.packageName, serviceInfo.name);
            String enabledServiceString = componentName.flattenToString();
            ContentResolver resolver = getContentResolver();

            Settings.Secure.putString(resolver, "enabled_accessibility_services", enabledServiceString);
            Settings.Secure.putString(resolver,
                    "touch_exploration_granted_accessibility_services",
                    enabledServiceString);
            if (enableTouchExploration) {
                Settings.Secure.putInt(resolver, "touch_exploration_enabled", 1);
            }
            Settings.Secure.putInt(resolver, "accessibility_script_injection", 1);
            Settings.Secure.putInt(resolver, "accessibility_enabled", 1);

            Intent intent = new Intent("accessibilityOptionsEvent");
            intent.putExtra("state", "enable");
            sendBroadcast(intent);
        }
        catch(Exception e) {
            Log.e(TAG, "Failed to enable accessibility: " + e);
        }
    }

    private void disableTalkBack() {
        try {
            ContentResolver resolver = getContentResolver();
            Settings.Secure.putString(resolver, "enabled_accessibility_services", "");
            Settings.Secure.putString(resolver, "touch_exploration_granted_accessibility_services", "");
            Settings.Secure.putInt(resolver, "touch_exploration_enabled", 0);
            Settings.Secure.putInt(resolver, "accessibility_script_injection", 0);
            Settings.Secure.putInt(resolver, "accessibility_enabled", 0);

            Intent intent = new Intent("accessibilityOptionsEvent");
            intent.putExtra("state", "disable");
            sendBroadcast(intent);
        }
        catch(Exception e){
            Log.e(TAG, "Failed to disable accessibility: " + e);
        }
    }


}
