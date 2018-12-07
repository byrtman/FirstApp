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
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.List;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_MULTIPLE;
import static android.view.KeyEvent.ACTION_UP;

/*
 * MainActivity class that loads {@link MainLayout}.
 */
public class MainActivity extends Activity {

    private static final String TAG = "BYRT";
    private WebView mWebView;
    private MySurfaceView mSurfaceView;
    private AccessibilityManager mAccessibilityManager;
    private AccessibilityManager.AccessibilityStateChangeListener mAccessibilityListener;
    private ViewGroup mLayout;

    // VirtualNavigation keys when Talk back is turned on
    private static VirtualDpadKey mCenterVirtualDpadKey;
    private static VirtualDpadKey mLeftVirtualDpadKey;
    private static VirtualDpadKey mRightVirtualDpadKey;
    private static VirtualDpadKey mUpVirtualDpadKey;
    private static VirtualDpadKey mDownVirtualDpadKey;

    private Handler mVirtualNavigationFocusHandler;
    private View.OnKeyListener mOnKeyListener;
    private View.OnFocusChangeListener mOnFocusListener;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = (ViewGroup) findViewById(R.id.mainLayout);
        mSurfaceView = (MySurfaceView) findViewById(R.id.surfaceView);

        mVirtualNavigationFocusHandler = new Handler();

        mOnKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d("KEY", "  SurfaceView onKey("+KeyEvent.keyCodeToString(keyCode)+") received");
                    if (v != null) {
                        Log.d(TAG, v.toString());
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        if (mCenterVirtualDpadKey != null) {
//                            disableVirtualNavigation();
                        } else {
//                            enableVirtualNavigation();
                        }
                        return true;
                    }
                }
                return mSurfaceView.onKey(v, keyCode, event);
            }
        };

        mOnFocusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("FOCUS", "onFocusChange() "+getViewName(v)+" now has focus: " + hasFocus);
                if (v != mSurfaceView && hasFocus) {
                    disableVirtualNavigation();
                }
                else if (hasFocus) { //&& mCenterVirtualDpadKey == null) {
//                    enableVirtualNavigation();
                }
            }

            private String getViewName(View v) {
                if (v == mSurfaceView) {
                    return "SURFACE_VIEW";
                } else if (v == mWebView) {
                    return "WEB_VIEW";
                } else {
                    return "UNKNOWN";
                }
            }
        };

        mSurfaceView.setFocusable(true);
        mSurfaceView.requestFocus();
        mSurfaceView.setOnKeyListener(mOnKeyListener);
        mSurfaceView.setOnFocusChangeListener(mOnFocusListener);


        /**
         * This code block to detect TalkBack enablement changes is GOOD
         */
        mAccessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        mAccessibilityListener = new AccessibilityManager.AccessibilityStateChangeListener() {
            @Override
            public void onAccessibilityStateChanged(boolean enabled) {
                Log.d(TAG, "onAccessibilityStateChanged("+enabled+")");
                Toast.makeText( getApplicationContext(), "Ouch!", Toast.LENGTH_SHORT).show();
                if(enabled) {
//                    enableVirtualNavigation();
                }
                else {
                    disableVirtualNavigation();
                }
            }
        };
        mAccessibilityManager.addAccessibilityStateChangeListener(mAccessibilityListener);

        /**
         * This code block to detect initial state of TalkBack enablement is GOOD
         */
        Log.d(TAG, "onCreate() is Accessibility enabled? : " + mAccessibilityManager.isEnabled() );
        if (mAccessibilityManager.isEnabled())
        {
//            enableVirtualNavigation();
        }

    }

    private void outputFocusedViewParent() {
        for(int i = 0; i < mLayout.getChildCount(); i++) {
            View v = mLayout.getChildAt(i);
            if (v.hasFocus()) {
                Log.i(TAG,"MainActivity: mLayout child view has focus : "+getViewTag(v));
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
        mAccessibilityManager.removeAccessibilityStateChangeListener(mAccessibilityListener);
        if (mWebView != null) {
            mWebView.removeAllViews();
            mLayout.removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
//        Log.d(TAG, "MainActivity: dispatchGenericMotionEvent("+event+")");
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.d("KEY", "MainActivity: dispatchKeyEvent("+outputKeyEvent(event)+")");
            outputFocusedViewParent();
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

//            Intent intent = new Intent("accessibilityOptionsEvent");
//            intent.putExtra("state", "enable");
//            sendBroadcast(intent);
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

//            Intent intent = new Intent("accessibilityOptionsEvent");
//            intent.putExtra("state", "disable");
//            sendBroadcast(intent);
        }
        catch(Exception e){
            Log.e(TAG, "Failed to disable accessibility: " + e);
        }
    }

    //On AndroidTV when Talkback is enabled keyvents are not sent to the application,
    //rather navigation happens based on neighbors of the currently focused widget.
    //Inorder to work around this issue...(feature?) we create a VirtualDpadNavigator
    //that simulates the KeyEvents based on the focus changes on its VirtualDpadKeys.
    private void enableVirtualNavigation() {
        //remove any previous VirtualNavigation
        disableVirtualNavigation();
        mLeftVirtualDpadKey = addNewDpadKey(VirtualDpadKeyType.LEFT, this);
        mRightVirtualDpadKey = addNewDpadKey(VirtualDpadKeyType.RIGHT, this);
        mUpVirtualDpadKey = addNewDpadKey(VirtualDpadKeyType.UP, this);
        mDownVirtualDpadKey = addNewDpadKey(VirtualDpadKeyType.DOWN, this);
        mCenterVirtualDpadKey = addNewDpadKey(VirtualDpadKeyType.CENTER, this);

        //Setup neighbors
        mCenterVirtualDpadKey.setNextFocusLeftId(mLeftVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusRightId(mRightVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusUpId(mUpVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusDownId(mDownVirtualDpadKey.getId());

        Log.i(TAG,"Virtual Navigation enabled");
        logLayoutViews();
        //Set focus on center helepr to begin with
        resetVirtualNavigationFocus();
    }

    private void logLayoutViews() {
        for(int i = 0; i < mLayout.getChildCount(); i++) {
            View v = mLayout.getChildAt(i);
            Log.i(TAG," Layout child at: "+i+" view: "+getViewTag(v)+(v.hasFocus()?" <- *FOCUS*":""));
        }
    }

    public static String getViewTag(View v) {
        if (v == null) {
            return "NULL";
        }
        return v.getTag() != null ? (String) v.getTag() : v.getClass().getName();
    }

    private VirtualDpadKey addNewDpadKey(VirtualDpadKeyType type, Context context) {
        VirtualDpadKey key = new VirtualDpadKey(type, context);
        mLayout.addView(key, type.ordinal());
        return key;
    }

    private void disableVirtualNavigation() {
        if(mCenterVirtualDpadKey != null) {
            mLayout.removeView(mCenterVirtualDpadKey);
            mCenterVirtualDpadKey = null;
        }
        if(mLeftVirtualDpadKey != null) {
            mLayout.removeView(mLeftVirtualDpadKey);
            mLeftVirtualDpadKey = null;
        }
        if(mRightVirtualDpadKey != null) {
            mLayout.removeView(mRightVirtualDpadKey);
            mRightVirtualDpadKey = null;
        }
        if(mUpVirtualDpadKey != null) {
            mLayout.removeView(mUpVirtualDpadKey);
            mUpVirtualDpadKey = null;
        }
        if(mDownVirtualDpadKey != null) {
            mLayout.removeView(mDownVirtualDpadKey);
            mDownVirtualDpadKey = null;
        }
    }

    public void resetVirtualNavigationFocus() {
        //Must delay requesting focus or else Android doesn't always assign focus, 100 ms is magic!
        mVirtualNavigationFocusHandler.postDelayed(new Runnable() {
                                                       @Override
                                                       public void run() {
                                                           if(mCenterVirtualDpadKey != null) {
                                                               mCenterVirtualDpadKey.requestFocus();
                                                           }
                                                       }
                                                   }
                , 100);
    }




    public void simulateKeyEvent(int keyCode) {
        Log.d("KEY", "simulateKeyEvent("+KeyEvent.keyCodeToString(keyCode)+")");
        KeyEvent actionDown = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        actionDown.setSource(InputDevice.SOURCE_KEYBOARD);
        mOnKeyListener.onKey(null, keyCode, actionDown);
        KeyEvent actionUp = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        actionUp.setSource(InputDevice.SOURCE_KEYBOARD);
        mOnKeyListener.onKey(null, keyCode, actionUp);
    }


}