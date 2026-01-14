package com.fadcam.fadrec.ui.overlay;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders gesture trails (taps and swipes) as visual overlays on screen.
 * Handles drawing animated dots for taps and trails for swipes that fade out.
 */
public class GestureTrailRenderer {
    private static final String TAG = "GestureTrailRenderer";
    
    // Visual constants
    private static final float TAP_DOT_RADIUS_DP = 18f;
    private static final float TRAIL_STROKE_WIDTH_DP = 12f;
    private static final long FADE_DURATION_MS = 600;
    private static final long TAP_ANIMATION_DURATION_MS = 300;
    
    private final View parentView;
    private final Paint tapDotPaint;
    private final Paint trailPaint;
    private final Path trailPath;
    private final Handler mainHandler;
    
    // Configuration
    private boolean isEnabled = false;
    private int tapDotColor = 0xFFFF69B4; // Default pink
    private int trailColor = 0xFF00FFFF; // Default cyan
    private float density = 1.0f;
    
    // Active trails
    private final List<TapTrail> activeTapTrails = new ArrayList<>();
    private final List<SwipeTrail> activeSwipeTrails = new ArrayList<>();
    
    /**
     * Represents a tap trail with position and animation state.
     */
    private static class TapTrail {
        final PointF position;
        final long startTime;
        ValueAnimator animator;
        
        TapTrail(float x, float y) {
            this.position = new PointF(x, y);
            this.startTime = System.currentTimeMillis();
        }
        
        float getAlpha(long currentTime) {
            long elapsed = currentTime - startTime;
            return Math.max(0f, 1f - (float) elapsed / FADE_DURATION_MS);
        }
        
        boolean isExpired(long currentTime) {
            return getAlpha(currentTime) <= 0f;
        }
        
        void animate(ValueAnimator animator) {
            this.animator = animator;
        }
        
        void cancelAnimation() {
            if (animator != null) {
                animator.cancel();
            }
        }
    }
    
    /**
     * Represents a swipe trail with path and animation state.
     */
    private static class SwipeTrail {
        final Path path;
        final long startTime;
        final List<PointF> points;
        
        SwipeTrail() {
            this.path = new Path();
            this.points = new ArrayList<>();
            this.startTime = System.currentTimeMillis();
        }
        
        void addPoint(float x, float y) {
            PointF point = new PointF(x, y);
            if (points.isEmpty()) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
            points.add(point);
        }
        
        float getAlpha(long currentTime) {
            long elapsed = currentTime - startTime;
            return Math.max(0f, 1f - (float) elapsed / FADE_DURATION_MS);
        }
        
        boolean isExpired(long currentTime) {
            return getAlpha(currentTime) <= 0f;
        }
    }
    
    public GestureTrailRenderer(View parentView) {
        this.parentView = parentView;
        this.tapDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.trailPath = new Path();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Configure paints
        tapDotPaint.setStyle(Paint.Style.FILL);
        trailPaint.setStyle(Paint.Style.STROKE);
        trailPaint.setStrokeCap(Paint.Cap.ROUND);
        trailPaint.setStrokeJoin(Paint.Join.ROUND);
        
        // Get density from context
        if (parentView != null && parentView.getContext() != null) {
            density = parentView.getContext().getResources().getDisplayMetrics().density;
        }
        
        // Set initial stroke width
        trailPaint.setStrokeWidth(TRAIL_STROKE_WIDTH_DP * density);
    }
    
    /**
     * Enable or disable gesture trail rendering.
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (!enabled) {
            clearAllTrails();
        }
        Log.d(TAG, "Gesture trails " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Check if gesture trail rendering is enabled.
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Set the color for tap dots.
     */
    public void setTapDotColor(int color) {
        this.tapDotColor = color;
        Log.d(TAG, "Tap dot color set to: #" + Integer.toHexString(color));
    }
    
    /**
     * Set the color for swipe trails.
     */
    public void setTrailColor(int color) {
        this.trailColor = color;
        Log.d(TAG, "Trail color set to: #" + Integer.toHexString(color));
    }
    
    /**
     * Add a tap trail at the specified coordinates.
     */
    public void addTap(float x, float y) {
        if (!isEnabled) return;
        
        TapTrail tapTrail = new TapTrail(x, y);
        activeTapTrails.add(tapTrail);
        
        // Animate the tap dot with a pulse effect
        ValueAnimator animator = ValueAnimator.ofFloat(0.5f, 1.2f, 1.0f);
        animator.setDuration(TAP_ANIMATION_DURATION_MS);
        animator.addUpdateListener(animation -> {
            parentView.invalidate(); // Trigger redraw
        });
        tapTrail.animate(animator);
        animator.start();
        
        Log.d(TAG, "Tap added at (" + x + ", " + y + ")");
        
        // Schedule cleanup
        scheduleCleanup();
    }
    
    /**
     * Start a new swipe trail at the specified coordinates.
     */
    public void startSwipe(float x, float y) {
        if (!isEnabled) return;
        
        SwipeTrail swipeTrail = new SwipeTrail();
        swipeTrail.addPoint(x, y);
        activeSwipeTrails.add(swipeTrail);
        
        Log.d(TAG, "Swipe started at (" + x + ", " + y + ")");
    }
    
    /**
     * Add a point to the current swipe trail.
     */
    public void addSwipePoint(float x, float y) {
        if (!isEnabled || activeSwipeTrails.isEmpty()) return;
        
        SwipeTrail currentSwipe = activeSwipeTrails.get(activeSwipeTrails.size() - 1);
        currentSwipe.addPoint(x, y);
        
        // Invalidate parent view to trigger redraw
        mainHandler.post(() -> parentView.invalidate());
    }
    
    /**
     * End the current swipe trail.
     */
    public void endSwipe(float x, float y) {
        if (!isEnabled || activeSwipeTrails.isEmpty()) return;
        
        SwipeTrail currentSwipe = activeSwipeTrails.get(activeSwipeTrails.size() - 1);
        currentSwipe.addPoint(x, y);
        
        Log.d(TAG, "Swipe ended at (" + x + ", " + y + ")");
        
        scheduleCleanup();
    }
    
    /**
     * Clear all active trails immediately.
     */
    public void clearAllTrails() {
        // Cancel all animations
        for (TapTrail tapTrail : activeTapTrails) {
            tapTrail.cancelAnimation();
        }
        
        activeTapTrails.clear();
        activeSwipeTrails.clear();
        
        // Invalidate parent view
        if (parentView != null) {
            parentView.invalidate();
        }
        
        Log.d(TAG, "All gesture trails cleared");
    }
    
    /**
     * Draw all active gesture trails on the canvas.
     */
    public void draw(Canvas canvas) {
        if (!isEnabled) return;
        
        long currentTime = System.currentTimeMillis();
        
        // Remove expired trails
        removeExpiredTrails(currentTime);
        
        // Draw tap dots
        for (TapTrail tapTrail : activeTapTrails) {
            float alpha = tapTrail.getAlpha(currentTime);
            if (alpha > 0f) {
                tapDotPaint.setColor(tapDotColor);
                tapDotPaint.setAlpha((int) (alpha * 255));
                
                canvas.drawCircle(
                    tapTrail.position.x, 
                    tapTrail.position.y, 
                    TAP_DOT_RADIUS_DP * density, 
                    tapDotPaint
                );
            }
        }
        
        // Draw swipe trails
        for (SwipeTrail swipeTrail : activeSwipeTrails) {
            float alpha = swipeTrail.getAlpha(currentTime);
            if (alpha > 0f && !swipeTrail.points.isEmpty()) {
                trailPaint.setColor(trailColor);
                trailPaint.setAlpha((int) (alpha * 255));
                
                canvas.drawPath(swipeTrail.path, trailPaint);
            }
        }
    }
    
    /**
     * Remove expired trails from the active lists.
     */
    private void removeExpiredTrails(long currentTime) {
        // Remove expired tap trails
        activeTapTrails.removeIf(tapTrail -> tapTrail.isExpired(currentTime));
        
        // Remove expired swipe trails
        activeSwipeTrails.removeIf(swipeTrail -> swipeTrail.isExpired(currentTime));
    }
    
    /**
     * Schedule cleanup of expired trails.
     */
    private void scheduleCleanup() {
        mainHandler.post(() -> {
            if (parentView != null) {
                parentView.invalidate();
            }
        });
        
        // Schedule final cleanup after fade duration
        mainHandler.postDelayed(() -> {
            long currentTime = System.currentTimeMillis();
            removeExpiredTrails(currentTime);
            if (parentView != null) {
                parentView.invalidate();
            }
        }, FADE_DURATION_MS + 50);
    }
    
    /**
     * Get the current density scaling factor.
     */
    public float getDensity() {
        return density;
    }
}