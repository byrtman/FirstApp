package com.tivo.byrt.firstapp;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;


class VirtualDpadKey extends Button {

    private static final boolean DEBUG = true;
    private final VirtualDpadKeyType mVirtualDpadKeyType;
    public VirtualDpadKey(VirtualDpadKeyType VirtualDpadKeyType, Context context) {
        super(context);

        final MainActivity activity = (MainActivity) context;

        mVirtualDpadKeyType = VirtualDpadKeyType;
        int buttonSize = 100;
        int x = 60;
        int y = 30;
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

        // When using AppCompatButton
//            setFrame(x, y, x+buttonSize,y+buttonSize);

        // When using Button
        setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize));
        setX(x);
        setY(y);

        setFocusable(true);
        setTag(mVirtualDpadKeyType.name());
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
                            activity.simulateKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
                            activity.resetVirtualNavigationFocus();
                            break;
                        case DOWN:
                            activity.simulateKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
                            activity.resetVirtualNavigationFocus();
                            break;
                        case LEFT:
                            activity.simulateKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT);
                            activity.resetVirtualNavigationFocus();
                            break;
                        case RIGHT:
                            activity.simulateKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT);
                            activity.resetVirtualNavigationFocus();
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
                activity.simulateKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER);
            }
        });

//        setAccessibilityDelegate(new AccessibilityDelegate() {
//            @Override
//            public boolean performAccessibilityAction(View host, int action, Bundle args) {
//                Log.d("VPad", "performAccessibilityAction("+action+")");
//                return true;
//            }
//        });

        //Adding this below the mSurfaceView
//        MainLayout layout = (MainLayout) findViewById(R.layout.activity_main);
//        layout.addView(this, mVirtualDpadKeyType.ordinal());
    }
}