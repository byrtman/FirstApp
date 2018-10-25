package com.tivo.byrt.firstapp;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * TODO: document your custom view class.
 */
public class TalkBackHandlerView extends RelativeLayout {

    private static final String TAG = "BYRT";

    // VirtualNavigation keys when Talk back is turned on
    private static VirtualDpadKey mCenterVirtualDpadKey;
    private static VirtualDpadKey mLeftVirtualDpadKey;
    private static VirtualDpadKey mRightVirtualDpadKey;
    private static VirtualDpadKey mUpVirtualDpadKey;
    private static VirtualDpadKey mDownVirtualDpadKey;

    private ViewGroup mLayout;
    private Handler mVirtualNavigationFocusHandler;
    private View.OnKeyListener mOnKeyListener;
    private AccessibilityManager mAccessibilityManager;
    private AccessibilityManager.AccessibilityStateChangeListener mAccessibilityListener;

    public TalkBackHandlerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLayout = this;
        mVirtualNavigationFocusHandler = new Handler();

        mOnKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d("KEY", "  SurfaceView onKey("+KeyEvent.keyCodeToString(keyCode)+") received");
                    if (v != null) {
                        Log.d(TAG, v.toString());
                        Toast.makeText( getContext(), KeyEvent.keyCodeToString(keyCode), Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        };

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        /* no op */
    }

    @Override
    protected void onAttachedToWindow () {
        super.onAttachedToWindow();

        /**
         * This code block to detect TalkBack enablement changes is GOOD
         */
        mAccessibilityManager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        mAccessibilityListener = new AccessibilityManager.AccessibilityStateChangeListener() {
            @Override
            public void onAccessibilityStateChanged(boolean enabled) {
                Log.d(TAG, "onAccessibilityStateChanged("+enabled+")");
                Toast.makeText( getContext(), "Ouch!", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDetachedFromWindow() {
        mAccessibilityManager.removeAccessibilityStateChangeListener(mAccessibilityListener);
        super.onDetachedFromWindow();
    }

    private enum VirtualDpadKeyType {
        CENTER,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    private class VirtualDpadKey extends AppCompatButton {

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
            setTag(mVirtualDpadKeyType.name());
            setId(mVirtualDpadKeyType.ordinal() + 1);
            if(!DEBUG) {
                setBackground(null);
            }
            setAccessibilityDelegate(new View.AccessibilityDelegate() {
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
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
                                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
                                resetVirtualNavigationFocus();
                                break;
                            case DOWN:
                                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
                                resetVirtualNavigationFocus();
                                break;
                            case LEFT:
                                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
                                resetVirtualNavigationFocus();
                                break;
                            case RIGHT:
                                simulateKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
                                resetVirtualNavigationFocus();
                                break;
                            case CENTER:
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

    //On AndroidTV when Talkback is enabled keyvents are not sent to the application,
    //rather navigation happens based on neighbors of the currently focused widget.
    //Inorder to work around this issue...(feature?) we create a VirtualDpadNavigator
    //that simulates the KeyEvents based on the focus changes on its VirtualDpadKeys.
    public void enableVirtualNavigation() {
        //remove any previous VirtualNavigation
        disableVirtualNavigation();
        mCenterVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.CENTER, getContext());
        mLeftVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.LEFT, getContext());
        mRightVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.RIGHT, getContext());
        mUpVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.UP, getContext());
        mDownVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.DOWN, getContext());

        //Setup neighbors
        mCenterVirtualDpadKey.setNextFocusLeftId(mLeftVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusRightId(mRightVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusUpId(mUpVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusDownId(mDownVirtualDpadKey.getId());

        Log.i(TAG,"Virtual Navigation enabled");
        for(int i = 0; i < mLayout.getChildCount(); i++) {
            View v = mLayout.getChildAt(i);
            Log.i(TAG,"SurfaceLayout child at: "+i+" view: "+MainActivity.getViewName(v));
        }
        //Set focus on center helepr to begin with
        resetVirtualNavigationFocus();
    }

    public void disableVirtualNavigation() {
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

