package com.app.agrify;

import android.view.MotionEvent;

public interface DialogListener {
    void onDialogDismissed();

    // Handle touch events to detect gestures
    boolean onTouchEvent(MotionEvent event);
}
