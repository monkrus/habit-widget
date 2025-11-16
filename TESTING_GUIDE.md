# Testing Guide - Habit Streak Widget

Complete guide to test your app before launching.

## Quick Summary

| Method | Time | Requirements | Best For |
|--------|------|--------------|----------|
| **Android Studio** | 30 min setup | Desktop/laptop | Full development |
| **Real Device** | 5 min | Android phone + USB | Quick testing |
| **Build Only** | 2 min | This machine | Verify compilation |

---

## Method 1: Android Studio (Recommended)

### A. Install Android Studio

**Download:** https://developer.android.com/studio

**Or via command line:**

```bash
# Ubuntu/Debian
sudo snap install android-studio --classic

# macOS
brew install --cask android-studio

# Windows
# Download installer from website
```

### B. Open Project

1. Launch Android Studio
2. **File** → **Open**
3. Navigate to `/home/user/habit-tracker-widget`
4. Click **OK**

### C. Wait for Gradle Sync

- First time: 3-5 minutes (downloads dependencies)
- Status bar shows "Gradle sync" progress
- When done, you'll see "Gradle sync finished" ✓

### D. Create Virtual Device (Emulator)

1. Click **Device Manager** icon (phone icon in toolbar)
2. Click **Create Device**
3. Select **Pixel 5** → **Next**
4. Download **API 34** (Android 14) → **Next**
5. Name it "Test Phone" → **Finish**
6. Wait for download (~800 MB)

### E. Run the App

1. Click green **▶️ Run** button
2. Select "Test Phone" emulator
3. Wait ~30 seconds for emulator to boot
4. App launches automatically!

### F. Test Features

**In the app:**
- [ ] Tap **+** button
- [ ] Choose emoji (💧)
- [ ] Enter "Drink Water"
- [ ] Tap **Save**
- [ ] Habit appears in list
- [ ] Tap habit card → marks complete (✓)
- [ ] Add 2 more habits
- [ ] Try to add 4th → see Pro upgrade prompt

**Test the widget:**
- [ ] Long-press emulator home screen
- [ ] Tap **Widgets** at bottom
- [ ] Find **Habit Streak**
- [ ] Long-press widget → drag to home screen
- [ ] See your 3 habits on widget
- [ ] Tap habit name on widget → marks complete
- [ ] Check main app → status synced

**Test streaks:**
- [ ] In Android Studio toolbar: **Extended controls** (•••)
- [ ] Click **Settings** → **Date & time**
- [ ] Advance date by 1 day
- [ ] Open app → tap habit
- [ ] Streak increases to 🔥 2
- [ ] Advance another day without completing
- [ ] Streak resets to 0

---

## Method 2: Test on Real Android Phone

### Prerequisites

- Android phone (8.0 or higher)
- USB cable
- 5 minutes

### A. Enable Developer Mode

**On your phone:**

1. **Settings** → **About Phone**
2. Tap **Build Number** 7 times
3. "You are now a developer!" message appears
4. Go back to **Settings** → **System**
5. Tap **Developer Options**
6. Enable **USB Debugging**

### B. Build the APK

**On this machine:**

```bash
cd /home/user/habit-tracker-widget

# Build debug APK (unsigned, for testing)
./gradlew assembleDebug
```

Wait 2-3 minutes. Output:
```
BUILD SUCCESSFUL in 2m 34s
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

### C. Install on Phone

**Option A: USB Cable + ADB**

```bash
# Install ADB if needed
sudo apt-get install android-tools-adb

# Connect phone via USB
# Phone will ask "Allow USB debugging?" → Tap OK

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Output: Success
```

**Option B: Transfer File**

```bash
# Copy APK to your computer/phone
# Email it to yourself, or:
cp app/build/outputs/apk/debug/app-debug.apk ~/Desktop/

# On phone:
# 1. Download/copy the APK file
# 2. Tap it in file manager
# 3. May ask "Install from unknown sources?" → Allow
# 4. Tap Install
```

### D. Test Everything

Same checklist as Android Studio method above.

---

## Method 3: Just Verify It Compiles

Don't want to run it yet? Just check if the code is valid:

```bash
cd /home/user/habit-tracker-widget

# Check Kotlin syntax
./gradlew compileDebugKotlin

# Or build everything (without installing)
./gradlew assembleDebug
```

If it says `BUILD SUCCESSFUL` → Your code is valid! ✅

---

## Method 4: Code Review (No Build Required)

Want to just review the code first? Check these key files:

### Core Logic
```bash
# Habit data model with streak calculation
cat app/src/main/java/com/habitstreak/app/data/Habit.kt

# Widget provider
cat app/src/main/java/com/habitstreak/app/widget/HabitWidgetReceiver.kt

# Main screen
cat app/src/main/java/com/habitstreak/app/ui/screens/HabitListScreen.kt
```

### UI Preview (If you have Android Studio)

Open any screen file (e.g., `HabitListScreen.kt`) and:
1. Click **Split** view (top right)
2. See live preview of UI
3. No need to run the app!

---

## Troubleshooting

### "JAVA_HOME not set"

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
./gradlew assembleDebug
```

### "SDK location not found"

Create `local.properties`:

```bash
echo "sdk.dir=/home/$USER/Android/Sdk" > local.properties
```

Or install Android SDK:

```bash
# Ubuntu
sudo apt-get install android-sdk

# Then update local.properties with actual path
```

### "Build failed"

Most common fix:

```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug
```

### "Emulator won't start"

```bash
# Enable virtualization in BIOS/UEFI
# Or use a real device instead
```

### "Widget doesn't appear"

- Run the app at least once (registers the widget)
- Restart your phone/emulator
- Check widget size is at least 4x2 cells

---

## Quick Start (TL;DR)

**Fastest way to test:**

```bash
# 1. Build APK
cd /home/user/habit-tracker-widget
./gradlew assembleDebug

# 2. Enable USB debugging on phone
# (Settings → About → Tap Build Number 7x)

# 3. Install
adb install app/build/outputs/apk/debug/app-debug.apk

# 4. Done! Open app on phone
```

**Total time:** 5 minutes

---

## What to Test

### Critical Features ✅

- [ ] App launches without crash
- [ ] Can add a habit
- [ ] Can edit a habit
- [ ] Can delete a habit
- [ ] Widget appears on home screen
- [ ] Tapping widget marks habit complete
- [ ] Streak increments correctly
- [ ] Free version limits to 3 habits
- [ ] 4th habit shows Pro upgrade screen
- [ ] Pro upgrade works (simulated for now)

### Nice to Test 🎯

- [ ] Dark mode works
- [ ] App survives device rotation
- [ ] Widget updates after reboot
- [ ] Multiple widgets can be added
- [ ] Emoji picker scrolls smoothly
- [ ] Long habit names truncate properly

### Edge Cases 🐛

- [ ] Delete habit that's on widget → widget updates
- [ ] Add habit, immediately add widget → appears
- [ ] Complete habit, change date, complete again → streak increments
- [ ] Skip a day → streak resets to 0
- [ ] Complete today, try to complete again → stays at ✓

---

## After Testing

Once everything works:

1. **Take screenshots** (for Play Store)
2. **Note any bugs** → fix them
3. **Build release version** (see PLAY_STORE_GUIDE.md)
4. **Submit to Play Store**

---

## Need Help?

**Common Questions:**

Q: "Do I need Android Studio?"
A: No, but it's the easiest way. You can also build APK and install on phone directly.

Q: "Can I test without a phone?"
A: Yes, use Android Studio's emulator.

Q: "How long does building take?"
A: First time: 5-10 minutes (downloads dependencies). After that: 30-60 seconds.

Q: "What Android versions does it support?"
A: Android 8.0 (API 26) and up. That's 95% of devices.

Q: "Can I test on iOS?"
A: No, this is Android only. iOS version would need SwiftUI + WidgetKit.

---

**Ready to test?** Pick a method above and let's see your app in action! 🚀
