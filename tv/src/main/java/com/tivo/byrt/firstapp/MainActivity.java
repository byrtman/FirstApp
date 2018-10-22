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
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.load.model.ImageVideoWrapper;

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
    private AccessibilityManager mAccessibilityManager;
    private AccessibilityManager.AccessibilityStateChangeListener mAccessibilityListener;
    private ViewGroup mLayout;
    private boolean mTalkBackEnabled;

    private Button mTalkbackButton;
    private Button mWebviewButton;

    // VirtualNavigation keys when Talk back is turned on
    private static VirtualDpadKey mCenterVirtualDpadKey;
    private static VirtualDpadKey mLeftVirtualDpadKey;
    private static VirtualDpadKey mRightVirtualDpadKey;
    private static VirtualDpadKey mUpVirtualDpadKey;
    private static VirtualDpadKey mDownVirtualDpadKey;
    private static boolean mShouldStartVirtualKeyPresses;
    private Handler mVirtualNavigationFocusHandler;
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

        mVirtualNavigationFocusHandler = new Handler();

        mOnKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d("KEY", "  SurfaceView onKey("+KeyEvent.keyCodeToString(keyCode)+") received");
                    if (v != null) {
                        Log.d(TAG, v.toString());
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
                    disableVirtualNavigation();
                }
                else if (hasFocus) {
                    enableVirtualNavigation();
                }
            }

            private String getViewName(View v) {
                if (v == mSurfaceView) {
                    return "SURFACE_VIEW";
                } else if (v == mWebView) {
                    return "WEB_VIEW";
                } else if (v == mTalkbackButton) {
                    return "TALKBACK_BUTTON";
                } else if (v == mWebviewButton) {
                    return "WEBVIEW_BUTTON";
                } else {
                    return "UNKNOWN";
                }
            }
        };

        mSurfaceView.setFocusable(true);
        mSurfaceView.requestFocus();
        mSurfaceView.setOnKeyListener(mOnKeyListener);
        mSurfaceView.setOnFocusChangeListener(mOnFocusListener);
        mWebviewButton.setOnFocusChangeListener(mOnFocusListener);
        mTalkbackButton.setOnFocusChangeListener(mOnFocusListener);

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
                    enableVirtualNavigation();
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
            enableVirtualNavigation();
        }

    }

    private void outputFocusedViewParent() {
        for(int i = 0; i < mLayout.getChildCount(); i++) {
            View v = mLayout.getChildAt(i);
            if (v.hasFocus()) {
                Log.i(TAG,"MainLayout child view has focus : "+v.getId());
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
//            disableTalkBack();
        }
        else {
            Log.d(TAG, "Turning ON TalkBack...");
//            enableTalkBack();
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

    //On AndroidTV when Talkback is enabled keyvents are not sent to the application,
    //rather navigation happens based on neighbors of the currently focused widget.
    //Inorder to work around this issue...(feature?) we create a VirtualDpadNavigator
    //that simulates the KeyEvents based on the focus changes on its VirtualDpadKeys.
    private void enableVirtualNavigation() {
        //remove any previous VirtualNavigation
        disableVirtualNavigation();
        mLeftVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.LEFT, this);
        mRightVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.RIGHT, this);
        mUpVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.UP, this);
        mDownVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.DOWN, this);
        mCenterVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.CENTER, this);

        //Setup neighbors
        mCenterVirtualDpadKey.setNextFocusLeftId(mLeftVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusRightId(mRightVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusUpId(mUpVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusDownId(mDownVirtualDpadKey.getId());

        Log.i(TAG,"Virtual Navigation enabled");
        for(int i = 0; i < mLayout.getChildCount(); i++) {
            View v = mLayout.getChildAt(i);
            Log.i(TAG,"SurfaceLayout child at: "+i+" view: "+v);
        }
        //Set focus on center helepr to begin with
        resetVirtualNavigationFocus();
    }

    private void disableVirtualNavigation() {
        mShouldStartVirtualKeyPresses = false;
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

    private void resetVirtualNavigationFocus() {
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

    private enum VirtualDpadKeyType {
        CENTER,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    private class VirtualDpadKey extends android.support.v7.widget.AppCompatButton {

        private static final boolean DEBUG = true;
        private final VirtualDpadKeyType mVirtualDpadKeyType;
        public VirtualDpadKey(VirtualDpadKeyType VirtualDpadKeyType, Context context) {
            super(context);
            mVirtualDpadKeyType = VirtualDpadKeyType;
            setWidth(100);
            setHeight(100);
            switch(mVirtualDpadKeyType) {
                case UP:
                    setX(120);
                    setY(10);
                    break;
                case DOWN:
                    setX(120);
                    setY(230);
                    break;
                case LEFT:
                    setX(10);
                    setY(120);
                    break;
                case RIGHT:
                    setX(230);
                    setY(120);
                    break;
                case CENTER:
                    setX(120);
                    setY(120);
                    break;
            }
            setFocusable(true);
            setId(mVirtualDpadKeyType.ordinal() + 1);
            if(!DEBUG) {
                setBackground(null);
            }
            setAccessibilityDelegate(new View.AccessibilityDelegate() {
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
//                    Log.d(TAG, "ANodeInfo: " + info);
                    //Blanked to prevent talkback from announcing class/type and description
                    info.setClassName("");
                    info.setContentDescription("");
                }
            });
            setOnFocusChangeListener(new OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus) {
                        switch(mVirtualDpadKeyType) {
                            case UP:
                                if (!mShouldStartVirtualKeyPresses) {
                                    return;
                                }
                                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
                                resetVirtualNavigationFocus();
                                break;
                            case DOWN:
                                if (!mShouldStartVirtualKeyPresses) {
                                    return;
                                }
                                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
                                resetVirtualNavigationFocus();
                                break;
                            case LEFT:
                                if (!mShouldStartVirtualKeyPresses) {
                                    return;
                                }
                                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
                                resetVirtualNavigationFocus();
                                break;
                            case RIGHT:
                                if (!mShouldStartVirtualKeyPresses) {
                                    return;
                                }
                                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
                                resetVirtualNavigationFocus();
                                break;
                            case CENTER:
                                if (!mShouldStartVirtualKeyPresses) {
                                    mShouldStartVirtualKeyPresses = true;
                                }
                                break;
                        }
                    }
                }
            });
            setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_SEARCH) {
                        //Set back key handled or else Android will pause the app. Let SurfaceView decide
                        //if app must be paused.
                        //Voice search key is handled here so that Tivo voice search opens and not google search
                        //when Talkback feature is enabled
                        return true;
                    }
                    return false;
                }
            });
            setOnClickListener(new OnClickListener() {

                @Override
                public void onClick (View v) {
                    simulateKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER);
                }
            });
            //Adding this below the mSurfaceView
            mLayout.addView(this, mVirtualDpadKeyType.ordinal());
        }
    }

    private void simulateKeyEvent(int keyCode) {
        Log.d("KEY", "simulateKeyEvent("+KeyEvent.keyCodeToString(keyCode)+")");
        KeyEvent actionDown = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        actionDown.setSource(InputDevice.SOURCE_KEYBOARD);
        mOnKeyListener.onKey(null, keyCode, actionDown);
        KeyEvent actionUp = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        actionUp.setSource(InputDevice.SOURCE_KEYBOARD);
        mOnKeyListener.onKey(null, keyCode, actionUp);
    }
}
