# 1Alarm - Wear OS Alarm Clock

A simple alarm clock app for Samsung Galaxy Watch (Wear OS).

## Features

- Create, edit, and delete alarms
- Full-screen dismiss/snooze UI
- Bezel control for setting time
- Repeat days (weekdays, weekends, custom)
- Vibration-only alarms
- Complication showing next alarm countdown
- App-wide snooze duration setting

## Sideload Permissions

After installing via ADB (sideloading), you must grant these permissions manually:

```bash
adb shell pm grant com.alarm.onealarm android.permission.POST_NOTIFICATIONS
adb shell pm grant com.alarm.onealarm android.permission.SYSTEM_ALERT_WINDOW
```

**Note:** `POST_NOTIFICATIONS` will also be requested via an in-app dialog on first launch. `SYSTEM_ALERT_WINDOW` has no settings UI on Wear OS, so ADB is the only way to grant it for sideloaded apps. Apps installed from the Play Store receive `SYSTEM_ALERT_WINDOW` automatically.

## Building

Open the project in Android Studio and build/deploy to your Wear OS device.
