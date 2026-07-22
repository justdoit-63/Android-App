# Walkthrough - Live Material Clock (Dark Theme)

I have transformed the app into a live Material 3 clock with both Analog and Digital modes, forced to Dark Theme.

## Changes Made

### [MainActivity](file:///C:/Users/infor/AndroidStudioProjects/Fitness/app/src/main/java/com/example/fitness/MainActivity.kt)

- **Live Ticking**: Implemented using `LaunchedEffect` and `Calendar` to update the state every second.
- **Dark Theme**: Forced `FitnessTheme(darkTheme = true)` in the `setContent` block.
- **Analog Clock**:
    - Custom UI built with `Canvas`.
    - Smooth movement for hour hands (accounts for minutes).
    - Material 3 Primary color used for the second hand and center pin.
- **Digital Clock**:
    - Large, bold font using Material 3 `displayLarge` typography.
- **Toggle Mode**:
    - Added a `Switch` to swap between Digital and Analog views.
    - Used `Crossfade` for a smooth transition animation between modes.

## Verification Results

### Live Updates
- The clock state updates every 1000ms.
- Both analog hands and digital text reflect the current system time accurately.

### Dark Theme
- App consistently uses `DarkColorScheme` regardless of system settings.

### User Interface
- Verified that the Analog clock hands (Hour, Minute, Second) are correctly positioned based on trigonometric calculations.
- Verified the toggle switch works as expected.
