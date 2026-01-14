# Gesture Trails Feature - Test Plan

## Test Environment Setup

1. Build and install the app:
   ```bash
   ./gradlew :app:assembleDefaultDebug
   adb install -r app/build/outputs/apk/default/debug/app-default-debug.apk
   ```

2. Grant necessary permissions:
   - Display over other apps (for floating controls)
   - Screen recording permission
   - Storage permission (for saving recordings)

## Test Cases

### TC1: Feature Discovery and Enablement

**Steps**:
1. Open FadCam app
2. Switch to "FadRec" mode
3. Scroll down in settings to find "Show Gesture Trails" card
4. Verify card has Material icon "gesture" and toggle switch

**Expected**: 
- Card appears below "Floating Controls" card
- Toggle is OFF by default
- Color picker buttons are hidden

**Status**: [ ] Pass [ ] Fail

---

### TC2: Enable Gesture Trails

**Steps**:
1. Toggle "Show Gesture Trails" switch to ON
2. Observe UI changes

**Expected**:
- Color picker buttons appear with fade-in animation
- "Tap Dot Color" button shows preview circle (pinkish color)
- "Trail Color" button shows preview circle (cyan color)
- Setting is saved to SharedPreferences

**Status**: [ ] Pass [ ] Fail

---

### TC3: Customize Tap Dot Color

**Steps**:
1. Enable gesture trails (if not already enabled)
2. Tap "Tap Dot Color" button
3. Color picker dialog appears
4. Select a color (e.g., red)
5. Return to settings

**Expected**:
- Color picker dialog opens as overlay
- 24 colors displayed in 4-column grid
- Selected color is broadcast via intent
- Preview circle updates to selected color
- Setting persists after app restart

**Status**: [ ] Pass [ ] Fail

---

### TC4: Customize Trail Color

**Steps**:
1. Enable gesture trails
2. Tap "Trail Color" button
3. Color picker dialog appears
4. Select a different color (e.g., green)
5. Return to settings

**Expected**:
- Color picker dialog opens
- Selected color updates preview circle
- Trail color is different from dot color
- Setting persists after app restart

**Status**: [ ] Pass [ ] Fail

---

### TC5: Tap Gesture Visualization

**Steps**:
1. Enable gesture trails
2. Start floating controls
3. Start screen recording
4. Tap screen multiple times in different locations
5. Observe visual feedback

**Expected**:
- Small circular dots appear at tap locations (~18dp radius)
- Dots use selected tap dot color
- Dots fade out after ~600ms
- Multiple taps can be visible simultaneously during fade
- No performance lag or stutter

**Status**: [ ] Pass [ ] Fail

---

### TC6: Swipe Gesture Visualization

**Steps**:
1. Enable gesture trails
2. Start screen recording
3. Swipe across screen horizontally
4. Swipe vertically
5. Swipe diagonally
6. Perform quick vs slow swipes

**Expected**:
- Smooth trail follows finger path
- Trail uses selected trail color
- Trail has consistent ~12dp stroke width
- Trail is smooth (no jagged lines)
- Trail fades out after ~600ms
- Works for all swipe directions and speeds

**Status**: [ ] Pass [ ] Fail

---

### TC7: Multi-Touch Gesture Visualization

**Steps**:
1. Enable gesture trails
2. Start screen recording
3. Perform pinch gesture (two fingers)
4. Perform spread gesture
5. Perform two-finger scroll

**Expected**:
- Both finger paths show trails
- Trails use selected trail color
- Both trails are smooth and synchronized
- Trails fade independently after 600ms
- Multi-touch detection is accurate

**Status**: [ ] Pass [ ] Fail

---

### TC8: Gesture Recording in Video

**Steps**:
1. Enable gesture trails
2. Start screen recording
3. Perform taps and swipes
4. Stop recording
5. Play back recorded video

**Expected**:
- All tap dots visible in recording
- All swipe trails visible in recording
- Gestures appear on top of screen content
- Colors match selected colors
- Fade animations are captured smoothly
- No visual artifacts or glitches

**Status**: [ ] Pass [ ] Fail

---

### TC9: Disable Gesture Trails

**Steps**:
1. Enable gesture trails and set custom colors
2. Start screen recording
3. Perform gestures - verify they appear
4. Stop recording
5. Toggle "Show Gesture Trails" OFF
6. Start new recording
7. Perform gestures

**Expected**:
- Color picker buttons disappear when disabled
- No gesture visualization appears during second recording
- Custom colors are preserved (visible if re-enabled)
- Window becomes non-touchable for annotation view

**Status**: [ ] Pass [ ] Fail

---

### TC10: Orientation Change

**Steps**:
1. Enable gesture trails
2. Start screen recording in portrait
3. Perform taps and swipes
4. Rotate device to landscape
5. Perform more taps and swipes
6. Rotate back to portrait

**Expected**:
- Gestures work correctly in both orientations
- Dot size remains consistent (dp-based)
- Trail width remains consistent
- No crashes or visual glitches on rotation
- Settings persist across orientation changes

**Status**: [ ] Pass [ ] Fail

---

### TC11: Integration with Annotations

**Steps**:
1. Enable gesture trails
2. Enable floating controls with annotation tools
3. Start screen recording
4. Enable annotation mode
5. Draw some annotations with pen tool
6. Disable annotation mode
7. Perform tap/swipe gestures on screen

**Expected**:
- Annotation drawing doesn't trigger gesture trails
- Gestures work when annotation is disabled
- Both annotations and gestures can coexist in recording
- No interference between the two systems
- Window touchability flags update correctly

**Status**: [ ] Pass [ ] Fail

---

### TC12: Performance Impact

**Steps**:
1. Enable gesture trails
2. Start screen recording (monitor FPS)
3. Perform rapid gestures (e.g., fast continuous swipe)
4. Check recording quality and device temperature

**Expected**:
- No noticeable FPS drop
- Recording remains smooth (30fps target)
- Device doesn't overheat
- Memory usage stays stable
- No ANR or "isn't responding" dialogs

**Status**: [ ] Pass [ ] Fail

---

### TC13: Edge Cases

#### TC13a: Rapid Tap Spam
**Steps**: Tap screen 50+ times rapidly
**Expected**: All taps render, old ones fade, no crash, no memory leak

#### TC13b: Very Long Swipe
**Steps**: Swipe from top-left to bottom-right corner slowly
**Expected**: Complete trail renders smoothly, fades correctly

#### TC13c: Multi-Touch with 5+ Fingers
**Steps**: Place 5 fingers on screen simultaneously
**Expected**: System handles gracefully (may only track 2, no crash)

#### TC13d: Switch Colors While Recording
**Steps**: Change gesture colors mid-recording
**Expected**: New colors apply immediately to new gestures

**Status**: [ ] Pass [ ] Fail

---

### TC14: Settings Persistence

**Steps**:
1. Enable gesture trails
2. Set custom dot color (e.g., yellow)
3. Set custom trail color (e.g., purple)
4. Force-close app
5. Reopen app and navigate to FadRec settings

**Expected**:
- "Show Gesture Trails" toggle is still ON
- Dot color preview shows yellow
- Trail color preview shows purple
- All settings load from SharedPreferences correctly

**Status**: [ ] Pass [ ] Fail

---

### TC15: Color Picker Cancel

**Steps**:
1. Enable gesture trails
2. Note current dot color
3. Tap "Tap Dot Color"
4. Tap "Cancel" in color picker
5. Check dot color preview

**Expected**:
- Color picker dismisses
- Dot color remains unchanged
- No color is saved to preferences
- App doesn't crash

**Status**: [ ] Pass [ ] Fail

---

## Regression Tests

### RT1: Existing Screen Recording
**Steps**: Disable gesture trails, record screen normally
**Expected**: Standard screen recording works as before

### RT2: Floating Controls
**Steps**: Test floating controls with gesture trails disabled
**Expected**: Floating controls work normally, no impact

### RT3: Annotation Tools
**Steps**: Use annotation tools with gesture trails disabled
**Expected**: Annotations work as before, no regressions

---

## Performance Metrics

- [ ] Gesture detection latency: < 16ms (1 frame)
- [ ] Tap to visual appearance: < 50ms
- [ ] Memory overhead: < 10MB
- [ ] CPU usage increase: < 5%
- [ ] Recording FPS maintained: 30fps stable

---

## Known Limitations

1. **Gesture trails only visible during AnnotationService**:
   - Requires floating controls to be enabled
   - Does not work without annotation overlay

2. **Max fade time fixed at 600ms**:
   - User cannot customize fade duration
   - Acceptable trade-off for simplicity

3. **Color picker has limited palette**:
   - 24 predefined colors only
   - No custom HEX/RGB input
   - Sufficient for most use cases

---

## Sign-off

- [ ] All test cases passed
- [ ] No critical bugs found
- [ ] Performance acceptable
- [ ] Feature ready for production

**Tested by**: _______________
**Date**: _______________
**Build version**: _______________

---

## Debugging Tips

If gestures don't appear:
1. Check `adb logcat | grep -E "GestureTrail|AnnotationView"`
2. Verify floating controls are enabled
3. Verify overlay permission granted
4. Check SharedPreferences: `adb shell run-as com.fadcam cat /data/data/com.fadcam/shared_prefs/app_prefs.xml | grep gesture`

If colors don't update:
1. Check broadcast receiver registration
2. Verify ColorPickerDialogActivity sends broadcast
3. Check logcat for `"Gesture settings changed"`
