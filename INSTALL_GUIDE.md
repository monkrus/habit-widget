# 📱 Install Habit Streak Widget on Your Phone

## Quick Start Guide

### **Option 1: Build on Your Machine** (Recommended)

#### Prerequisites:
- Android Studio installed
- Physical Android device or emulator
- USB debugging enabled on your phone

#### Steps:

1. **Clone the repository** (if you haven't already):
```bash
git clone https://github.com/monkrus/habit-widget.git
cd habit-widget
git checkout claude/quick-project-ideas-019ZMgnyxFmJhgdf6waV9arf
```

2. **Open in Android Studio**:
   - File → Open → Select the `habit-widget` folder
   - Wait for Gradle sync (2-3 minutes)

3. **Connect your phone**:
   - Enable Developer Options on your phone:
     - Settings → About Phone → Tap "Build Number" 7 times
   - Enable USB Debugging:
     - Settings → Developer Options → USB Debugging → ON
   - Connect phone via USB cable
   - Tap "Allow" when prompted on phone

4. **Build and Install**:
   - Click the green "Run" button (▶️) in Android Studio
   - Select your device from the list
   - App will build and install automatically (~2-3 minutes first time)

---

### **Option 2: Build APK via Command Line**

If you prefer command line:

```bash
# Navigate to project
cd habit-widget

# Build debug APK
./gradlew assembleDebug

# APK location
# app/build/outputs/apk/debug/app-debug.apk
```

Then transfer the APK to your phone:
- Email it to yourself
- Upload to Google Drive
- Use ADB: `adb install app/build/outputs/apk/debug/app-debug.apk`

---

## 🔧 First-Time Setup on Phone

### After installing:

1. **Add the Widget**:
   - Long-press on your home screen
   - Tap "Widgets"
   - Find "Habit Streak Widget"
   - Drag to your home screen

2. **Create Your First Habit**:
   - Tap the widget (it will say "Add habits in the app")
   - Opens the app
   - Tap the "+" button
   - Choose an emoji (🎯 is default)
   - Name your habit (e.g., "Drink Water")
   - Tap "Save"

3. **Test the Widget**:
   - Go back to home screen
   - Your habit should appear on the widget
   - Tap the habit on the widget
   - Should show a checkmark ✓
   - Streak counter should show "🔥 1"

4. **Set Up Notifications** (Android 13+):
   - You'll be prompted to allow notifications
   - Tap "Allow"
   - Default reminder: 8:00 PM daily

---

## 🧪 Testing Checklist

Once installed, test these features:

### NEW! Phase 1 Premium Features:
- [ ] **Haptic Feedback**: Feel vibration when completing habit (should feel premium!)
- [ ] **Habit Suggestions**: Open app with no habits → See 10 popular suggestions
- [ ] **One-tap Add**: Tap "Add" on a suggestion (e.g., "💧 Drink water")
- [ ] **Achievements**: Complete habit for 7 days → Unlock "Week Warrior 🔥"
- [ ] **View Achievements**: Settings → Progress → Achievements (see 12 total)
- [ ] **Milestone Vibration**: Get to 7-day streak → Feel stronger vibration pattern

### Basic Functionality:
- [ ] Add a habit
- [ ] Widget updates immediately
- [ ] Tap habit on widget (marks complete)
- [ ] Tap again (marks incomplete)
- [ ] Streak counter increases
- [ ] Edit habit (change name/emoji)
- [ ] Delete habit (with confirmation dialog)

### Free vs Pro:
- [ ] Add 3 habits (should work)
- [ ] Try to add 4th habit (should see "Upgrade to Pro")
- [ ] Tap "Upgrade to Pro"
- [ ] **DO NOT purchase yet** (unless you want to test billing)

### Notifications:
- [ ] Grant notification permission
- [ ] Wait until 8:00 PM (or change time in code)
- [ ] Should receive "Don't break the chain!" notification

### Edge Cases:
- [ ] Restart phone (widget should persist)
- [ ] Clear app data (habits should reset)
- [ ] Multiple habits on widget (max 5 shown)

---

## 🐛 Common Issues

### "App not installed" error:
- Uninstall any previous version first
- Enable "Install from unknown sources" in Settings

### Widget doesn't update:
- Remove widget and re-add it
- Force close app and reopen
- Restart phone

### Notifications not working:
- Settings → Apps → Habit Streak → Notifications → Allow
- Check Do Not Disturb is off
- Verify notification permission granted

### Can't connect phone to Android Studio:
- Revoke USB debugging authorizations
- Disconnect and reconnect cable
- Try different USB cable/port

---

## 🎯 What to Test

Focus on these key flows:

1. **New User Experience**:
   - Install app
   - Add widget to home screen
   - Create first habit
   - Complete it from widget
   - Check streak increases

2. **Multi-Day Streak**:
   - Complete habit today
   - Tomorrow, complete again
   - Verify streak = 2
   - Skip a day
   - Verify streak resets to 0

3. **Free Limit**:
   - Add 3 habits (should work)
   - Try 4th (should prompt Pro upgrade)
   - Don't actually purchase (unless testing)

4. **Persistence**:
   - Add habits
   - Close app
   - Reboot phone
   - Verify habits still there
   - Verify widget still works

---

## 📊 Expected Behavior

### Streaks:
- ✅ Complete today → Streak = 1
- ✅ Complete today + yesterday → Streak = 2
- ✅ Skip today, completed yesterday → Streak = 1 (maintained)
- ✅ Skip 2+ days → Streak = 0 (broken)

### Widget:
- ✅ Shows up to 5 habits
- ✅ Tap to toggle completion
- ✅ Shows "🔥 X" when incomplete
- ✅ Shows "✓" when complete today

### Notifications:
- ✅ Daily reminder at 8 PM if habits incomplete
- ✅ Milestone celebration at 7, 30, 100, 365 days
- ✅ Notification opens app when tapped

---

## 🔥 Pro Tips

1. **Change reminder time**:
   - Currently hardcoded to 8 PM
   - Settings screen coming in v1.1

2. **Test milestone notifications**:
   - Manually edit habit data to have 7-day streak
   - Complete habit → Should trigger celebration

3. **Reset everything**:
   - Settings → Apps → Habit Streak → Storage → Clear Data

4. **Check for crashes**:
   - Android Studio → Logcat → Filter by "com.habitstreak.app"

---

## 📱 Device Requirements

- **Android 8.0+** (API 26+)
- **Target: Android 14** (API 34)
- **~15 MB storage**
- **Minimal RAM** (DataStore is lightweight)

---

## 🚀 Next Steps After Testing

If everything works:
1. Create a GitHub issue with any bugs found
2. List features you'd want added
3. Test on different Android versions if possible
4. Share feedback on UX/design

If you find bugs:
1. Note the exact steps to reproduce
2. Check Android version
3. Check device model
4. Report in GitHub issues

---

## 📞 Need Help?

If you encounter issues building:
1. Check you're on the correct branch: `claude/quick-project-ideas-019ZMgnyxFmJhgdf6waV9arf`
2. Sync Gradle files in Android Studio
3. Clean build: `./gradlew clean`
4. Rebuild: `./gradlew assembleDebug`

---

**Happy testing! 🎉**

Let me know what you think of the app once you try it!
