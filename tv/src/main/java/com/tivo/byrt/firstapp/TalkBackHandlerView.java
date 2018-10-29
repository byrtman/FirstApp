package com.tivo.byrt.firstapp;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

/**
 * TODO: document your custom view class.
 */
public class TalkBackHandlerView extends ViewGroup {

    private static final String TAG = "BYRT";

    // VirtualNavigation keys when Talk back is turned on
    private VirtualDpadKey mCenterVirtualDpadKey;
    private VirtualDpadKey mLeftVirtualDpadKey;
    private VirtualDpadKey mRightVirtualDpadKey;
    private VirtualDpadKey mUpVirtualDpadKey;
    private VirtualDpadKey mDownVirtualDpadKey;

    private ViewGroup mLayout;
    private Handler mVirtualNavigationFocusHandler;
    private AccessibilityManager mAccessibilityManager;
    private AccessibilityManager.AccessibilityStateChangeListener mAccessibilityListener;

    public TalkBackHandlerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTag("VPAD Handler View");

        mLayout = this;
        mLayout.setLayoutParams(new ViewGroup.LayoutParams(20, 20));

        mVirtualNavigationFocusHandler = new Handler();
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        enableVirtualNavigation();
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
            }
        };
        mAccessibilityManager.addAccessibilityStateChangeListener(mAccessibilityListener);

        Log.d(TAG, "onCreate() is Accessibility enabled? : " + mAccessibilityManager.isEnabled() );
    }

    @Override
    protected void onDetachedFromWindow() {
        mAccessibilityManager.removeAccessibilityStateChangeListener(mAccessibilityListener);
        disableVirtualNavigation();
        super.onDetachedFromWindow();
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        Log.d(TAG, "requestChildFocus("+MainActivity.getViewName(child)+", "+MainActivity.getViewName(focused)+")");
        super.requestChildFocus(child, focused);
    }

    private enum VirtualDpadKeyType {
        UP,
        LEFT,
        CENTER,
        RIGHT,
        DOWN
    }

    private class VirtualDpadKey extends AppCompatButton {

        private static final boolean DEBUG = true;
        private final VirtualDpadKeyType mVirtualDpadKeyType;
        public VirtualDpadKey(VirtualDpadKeyType VirtualDpadKeyType, Context context) {
            super(context);
            mVirtualDpadKeyType = VirtualDpadKeyType;
            int buttonSize = 5;
            int x = 10;
            int y = 10;
            switch(mVirtualDpadKeyType) {
                case UP:
                    x += buttonSize;
                    y += 0;
                    break;
                case DOWN:
                    x += buttonSize;
                    y += buttonSize*2;
                    break;
                case LEFT:
                    x += 0;
                    y += buttonSize;
                    break;
                case RIGHT:
                    x += buttonSize*2;
                    y += buttonSize;
                    break;
                case CENTER:
                    x += buttonSize;
                    y += buttonSize;
                    break;
            }
            setFrame(x, y, x+buttonSize,y+buttonSize);
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
                    info.setClassName("Vpad");
                    info.setContentDescription("Fuck this guy!");
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
                    if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_SEARCH) {
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
        mUpVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.UP, getContext());
        mLeftVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.LEFT, getContext());
        mCenterVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.CENTER, getContext());
        mRightVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.RIGHT, getContext());
        mDownVirtualDpadKey = new VirtualDpadKey(VirtualDpadKeyType.DOWN, getContext());

        //Setup neighbors
        mCenterVirtualDpadKey.setNextFocusLeftId(mLeftVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusRightId(mRightVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusUpId(mUpVirtualDpadKey.getId());
        mCenterVirtualDpadKey.setNextFocusDownId(mDownVirtualDpadKey.getId());

        Log.i(TAG,"Virtual Navigation enabled");
        for(int i = 0; i < mLayout.getChildCount(); i++) {
            View v = mLayout.getChildAt(i);
            Log.i(TAG,"VPAD child at: "+i+" view: "+MainActivity.getViewName(v));
        }
        //Set focus on center helper to begin with
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
        MainActivity.simulateKeyEvent(keyCode);
    }
}

