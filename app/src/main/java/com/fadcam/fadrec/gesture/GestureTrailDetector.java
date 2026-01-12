package com.fadcam.fadrec.gesture;

import android.view.MotionEvent;

/**
 * Detects gestures during screen recording to visualize them as trails.
 * Handles taps, swipes, pinches, and multi-touch scrolls.
 */
public class GestureTrailDetector {
    
    public interface OnGestureListener {
        void onTap(float x, float y);
        void onSwipeStarted(float x, float y);
        void onSwipeMoved(float x, float y);
        void onSwipeEnded(float x, float y);
        void onMultiTouchStarted(float x1, float y1, float x2, float y2);
        void onMultiTouchMoved(float x1, float y1, float x2, float y2);
        void onMultiTouchEnded();
    }
    
    private final OnGestureListener listener;
    private final float density;
    
    private static final float TAP_TIMEOUT = 200; // ms
    private static final float TAP_THRESHOLD_DP = 12f;
    private static final float SWIPE_THRESHOLD_DP = 40f;
    
    private float startX, startY;
    private long startTime;
    private boolean isSwipeActive = false;
    private boolean isMultiTouchActive = false;
    private boolean movedSignificantDistance = false;
    
    public GestureTrailDetector(float density, OnGestureListener listener) {
        this.density = density;
        this.listener = listener;
    }
    
    /**
     * Process a touch event for gesture detection.
     * @param event The MotionEvent to process
     */
    public void onTouchEvent(MotionEvent event) {
        if (listener == null) return;
        
        int action = event.getActionMasked();
        int pointerCount = event.getPointerCount();
        
        float tapThreshold = TAP_THRESHOLD_DP * density;
        
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                startTime = System.currentTimeMillis();
                isSwipeActive = false;
                isMultiTouchActive = false;
                movedSignificantDistance = false;
                break;
                
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerCount == 2) {
                    isMultiTouchActive = true;
                    isSwipeActive = false;
                    listener.onMultiTouchStarted(
                        event.getX(0), event.getY(0),
                        event.getX(1), event.getY(1)
                    );
                }
                break;
                
            case MotionEvent.ACTION_MOVE:
                float currentX = event.getX();
                float currentY = event.getY();
                float dx = currentX - startX;
                float dy = currentY - startY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distance > tapThreshold) {
                    movedSignificantDistance = true;
                }
                
                if (isMultiTouchActive && pointerCount >= 2) {
                    listener.onMultiTouchMoved(
                        event.getX(0), event.getY(0),
                        event.getX(1), event.getY(1)
                    );
                } else if (!isMultiTouchActive) {
                    if (!isSwipeActive && distance > tapThreshold) {
                        isSwipeActive = true;
                        listener.onSwipeStarted(startX, startY);
                    }
                    if (isSwipeActive) {
                        listener.onSwipeMoved(currentX, currentY);
                    }
                }
                break;
                
            case MotionEvent.ACTION_POINTER_UP:
                if (pointerCount <= 2) {
                    isMultiTouchActive = false;
                    listener.onMultiTouchEnded();
                }
                break;
                
            case MotionEvent.ACTION_UP:
                long duration = System.currentTimeMillis() - startTime;
                if (!movedSignificantDistance && duration < TAP_TIMEOUT) {
                    listener.onTap(event.getX(), event.getY());
                } else if (isSwipeActive) {
                    listener.onSwipeEnded(event.getX(), event.getY());
                }
                isSwipeActive = false;
                isMultiTouchActive = false;
                break;
                
            case MotionEvent.ACTION_CANCEL:
                if (isSwipeActive) {
                    listener.onSwipeEnded(event.getX(), event.getY());
                }
                if (isMultiTouchActive) {
                    listener.onMultiTouchEnded();
                }
                isSwipeActive = false;
                isMultiTouchActive = false;
                break;
        }
    }
}
