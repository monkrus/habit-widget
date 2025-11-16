# Quick Build Guide

Step-by-step instructions to build and run Habit Streak Widget.

## Prerequisites

- **Android Studio:** Hedgehog (2023.1.1) or newer
- **JDK:** 17 (comes with Android Studio)
- **Android SDK:** API 34 (Android 14)

## Setup (5 minutes)

### 1. Clone the Repository

```bash
git clone https://github.com/monkrus/habit-widget.git
cd habit-widget
```

### 2. Open in Android Studio

1. Launch Android Studio
2. **File** → **Open**
3. Select the project folder
4. Click **OK**

### 3. Wait for Gradle Sync

Android Studio will automatically:
- Download dependencies
- Sync Gradle files
- Index the project

This takes 2-5 minutes on first run.

## Running the App (2 minutes)

### Option A: Using an Emulator

1. **Create emulator** (if needed):
   - Click **Device Manager** (phone icon in toolbar)
   - Click **Create Device**
   - Select **Pixel 5** → **Next**
   - Select **API 34 (UpsideDownCake)** → **Next**
   - Click **Finish**

2. **Run app:**
   - Click green **Run** button (▶️)
   - Or press `Shift + F10`
   - Select your emulator
   - Wait for app to launch

### Option B: Using a Physical Device

1. **Enable Developer Options** on your Android device:
   - Go to **Settings** → **About Phone**
   - Tap **Build Number** 7 times
   - Go back to **Settings** → **Developer Options**
   - Enable **USB Debugging**

2. **Connect device:**
   - Plug in via USB
   - Accept "Allow USB debugging?" prompt

3. **Run app:**
   - Click green **Run** button
   - Select your device
   - App will install and launch

## Testing the Widget

### Adding Widget to Home Screen

1. **Run the app** first to register the widget
2. **Long-press** on home screen
3. Tap **Widgets**
4. Find **Habit Streak**
5. Drag **Habit Streak Widget** to home screen
6. Widget will show "Add habits in the app"

### Creating Your First Habit

1. Open the app
2. Tap **+** button (bottom right)
3. Choose an emoji (e.g., 💧)
4. Enter habit name (e.g., "Drink Water")
5. Tap **Save**
6. Check your widget - habit should appear!

### Testing Streak Logic

1. Tap habit in the widget → ✓ appears (marked complete)
2. Wait until midnight (or change device time)
3. Next day, tap habit again → streak increments to 🔥 2
4. Skip a day → streak resets to 0

## Building Release APK

### For Testing (Debug Build)

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### For Production (Signed Release)

See [PLAY_STORE_GUIDE.md](PLAY_STORE_GUIDE.md) for signing setup, then:

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### For Play Store (App Bundle)

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## Project Structure Overview

```
habit-tracker-widget/
├── app/
│   ├── src/main/
│   │   ├── java/com/habitstreak/app/
│   │   │   ├── data/           # Data models & repository
│   │   │   ├── widget/         # Widget provider
│   │   │   ├── ui/             # Compose UI screens
│   │   │   └── MainActivity.kt
│   │   ├── res/                # Resources (layouts, strings)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts        # App-level Gradle config
├── build.gradle.kts            # Project-level Gradle config
├── settings.gradle.kts
└── README.md
```

## Common Issues

### "SDK not found"

**Solution:**
1. Open **SDK Manager** (Tools → SDK Manager)
2. Install **Android 14.0 (API 34)**
3. Sync Gradle again

### "Gradle sync failed"

**Solution:**
1. Click **Try Again**
2. Or: **File** → **Invalidate Caches** → **Invalidate and Restart**

### "App doesn't run"

**Solution:**
- Check device/emulator is running Android 8.0+ (API 26+)
- Try **Build** → **Clean Project** → **Rebuild Project**

### Widget doesn't appear

**Solution:**
- Run the app at least once to register the widget
- Restart your device/emulator
- Check widget size is at least 4x2 grid cells

## Development Tips

### Enable Live Preview (Jetpack Compose)

In any Compose file:
1. Click **Split** view (top right)
2. See live preview of UI as you code
3. Add `@Preview` annotation to composables

### Debug Widget Updates

Check Logcat for widget-related logs:
1. Open **Logcat** (bottom toolbar)
2. Filter: `HabitWidgetReceiver`
3. See when widget updates

### Hot Reload

While app is running:
- **Ctrl+S** (or Cmd+S) saves changes
- Click **Apply Changes** (lightning bolt icon)
- UI updates without full rebuild (for Compose changes)

## Testing Checklist

Before submitting to Play Store:

- [ ] App launches without crashes
- [ ] Can create a habit
- [ ] Can edit a habit
- [ ] Can delete a habit
- [ ] Widget shows habits correctly
- [ ] Tapping widget toggles completion
- [ ] Streak increments correctly over multiple days
- [ ] Free version limits to 3 habits
- [ ] Pro upgrade works
- [ ] Widget updates after boot
- [ ] App works on Android 8.0+ devices

## Next Steps

1. **Test thoroughly** on multiple devices/Android versions
2. **Create screenshots** for Play Store listing
3. **Follow** [PLAY_STORE_GUIDE.md](PLAY_STORE_GUIDE.md) for submission
4. **Launch** and start marketing!

---

**Build time:** ~5 minutes
**First run:** ~2 minutes
**Total:** 7 minutes from clone to running app! 🚀

Questions? Open an issue on GitHub!
