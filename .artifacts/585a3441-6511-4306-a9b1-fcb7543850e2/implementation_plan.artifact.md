# Implementation Plan - Live Material Clock (Dark Theme)

Create a live updating clock app with a toggle between Analog and Digital displays, strictly in Dark Theme using Material 3.

## User Review Required

> [!IMPORTANT]
> The app will be forced into **Dark Theme** as requested, ignoring the system setting.
> The **Analog Clock** will be custom-drawn using Compose `Canvas`.

## Proposed Changes

### [MainActivity](file:///C:/Users/infor/AndroidStudioProjects/Fitness/app/src/main/java/com/example/fitness/MainActivity.kt)

#### [MODIFY] [MainActivity.kt](file:///C:/Users/infor/AndroidStudioProjects/Fitness/app/src/main/java/com/example/fitness/MainActivity.kt)
- Update `setContent` to use `FitnessTheme(darkTheme = true)`.
- Implement `ClockScreen` which manages:
    - `currentTime` state (updates every second).
    - `isAnalog` state (toggled by the user).
- Create `DigitalClock` component:
    - Large, stylized text showing `HH:mm:ss`.
- Create `AnalogClock` component:
    - Uses `Canvas` to draw:
        - Clock face (circle and hour markers).
        - Hour, Minute, and Second hands with distinct colors/thickness.
- Add a `Switch` or `FilterChip` in a `TopAppBar` or bottom bar to toggle modes.

## Verification Plan

### Manual Verification
- Deploy to device/emulator.
- Verify the clock updates every second.
- Verify the toggle correctly switches between Digital and Analog views.
- Verify the app remains in Dark Theme regardless of system settings.
- Check that the Analog hands move correctly (Seconds move every tick, Minutes/Hours move proportionally).
