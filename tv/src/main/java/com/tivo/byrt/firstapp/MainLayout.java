/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RelativeLayout;

public class MainLayout extends RelativeLayout {
    private static final String TAG = "BYRT";

    public MainLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        Log.d(TAG, "constructing MainLayout : isAccessibilityImportant? " + isImportantForAccessibility());

    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {

        Log.i(TAG, "onPopulateAccessibilityEvent() called! " + AccessibilityEvent.eventTypeToString(event.getEventType()) );
        super.onPopulateAccessibilityEvent(event);
    }

    @Override
    public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        logEvent("requestSendAccessibilityEvent",child,event);
        return super.requestSendAccessibilityEvent(child, event);
    }

    @Override
    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        logEvent("onRequestSendAccessibilityEvent",child,event);
        return super.onRequestSendAccessibilityEvent(child, event);
    }


    private void logEvent(String methodName, View child, AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
//                Log.d(TAG, methodName + "(" + child.getClass().getName() + "," + AccessibilityEvent.eventTypeToString(event.getEventType()) + ")");
            default:
        }
    }

}
