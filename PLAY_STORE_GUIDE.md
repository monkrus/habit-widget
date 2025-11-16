# Google Play Store Submission Guide

Complete guide to submitting Habit Streak Widget to Google Play Store.

## Prerequisites

### 1. Google Play Developer Account
- Cost: $25 (one-time fee)
- Sign up: https://play.google.com/console/signup
- You'll need:
  - Google account
  - Credit/debit card for $25 fee
  - Developer identity verification (takes 1-2 days)

### 2. Generate App Signing Key

Run this command on your machine:

```bash
keytool -genkey -v -keystore habit-streak-release.jks \
  -alias habit-streak -keyalg RSA -keysize 2048 -validity 10000
```

**IMPORTANT:**
- Save the keystore file and passwords securely
- Never commit the keystore to git
- If you lose this, you can never update your app

You'll be asked:
- Keystore password (choose a strong one)
- Your name and organization details
- Key password (can be same as keystore password)

### 3. Configure App Signing

Create `app/keystore.properties`:

```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=habit-streak
storeFile=/path/to/habit-streak-release.jks
```

Update `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... rest of release config
        }
    }
}
```

### 4. Build Signed App Bundle

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## Play Store Listing Content

### App Title (30 chars max)
```
Habit Streak Widget
```

### Short Description (80 chars max)
```
Track daily habits with beautiful home screen widgets. Build streaks effortlessly.
```

### Full Description (4000 chars max)

```
Transform your daily habits with the power of home screen widgets. Habit Streak makes building habits 10x easier by putting your goals right on your home screen.

✨ WHY HABIT STREAK?

Most habit trackers require multiple steps to mark a habit complete. With Habit Streak Widget, it's just:
1. Glance at your home screen
2. Tap once
3. Done!

This friction-free approach is the difference between 90% adherence and 30% adherence.

🔥 KEY FEATURES

• Beautiful Home Screen Widget - Your habits are always visible
• One-Tap Completion - Mark habits complete instantly
• Streak Tracking - Use Jerry Seinfeld's "Don't Break the Chain" method
• Emoji Icons - Personalize each habit with fun emojis
• Clean, Ad-Free Interface - Focus on what matters
• Offline First - No account required, your data stays private

📊 FREE VERSION

• Track up to 3 daily habits
• Full widget functionality
• Streak tracking
• No ads, ever

⭐ PRO VERSION ($1.99 one-time)

• Unlimited habits
• Custom themes & colors
• Advanced statistics
• Cloud backup & sync
• Priority support

🎯 PERFECT FOR

• Building morning/evening routines
• Fitness goals (exercise, water intake)
• Reading, meditation, journaling
• Learning new skills
• Breaking bad habits
• Any daily commitment

💡 THE SCIENCE

Based on Jerry Seinfeld's productivity secret: "Don't Break the Chain." When you see your streak growing day by day on your home screen, you'll feel motivated to keep it going.

🔒 PRIVACY FIRST

• All data stored locally on your device
• No account required
• No tracking or analytics
• You own your data

📱 BEAUTIFUL DESIGN

• Material Design 3
• Dark mode support
• Customizable widget layouts
• Multiple size options

Download now and start building the habits that will change your life. Your future self will thank you!

---

Questions or feedback? Email us at support@habitstreak.app
```

### App Category
```
Productivity
```

### Tags (5 max)
```
habits, productivity, widget, streak, goals
```

### Content Rating
```
Everyone
```

### Privacy Policy

You'll need to host a privacy policy. Here's a simple one:

**Privacy Policy URL:** `https://yourdomain.com/privacy` (create this)

**Sample Privacy Policy:**

```markdown
# Privacy Policy for Habit Streak Widget

Last updated: [Date]

## Data Collection
Habit Streak Widget does NOT collect, transmit, or share any personal data. All habit data is stored locally on your device.

## Data Storage
- All data is stored using Android's encrypted DataStore
- Data never leaves your device
- No analytics or tracking

## Third-Party Services
The free version uses no third-party services. The Pro version may use Google Play Billing for purchase verification only.

## Children's Privacy
We do not knowingly collect data from children under 13.

## Changes
We may update this policy. Check this page periodically.

## Contact
For questions: support@habitstreak.app
```

## Screenshots Required

You need **at least 2 screenshots** (recommend 4-6):

### Screenshot Ideas:
1. **Widget on home screen** - Show the widget with 3 habits and streaks
2. **Main app screen** - Habit list with completed/incomplete indicators
3. **Add habit screen** - Emoji picker and name input
4. **Pro features screen** - Upgrade modal
5. **Habit detail** - Streak stats and calendar view (if implemented)
6. **Empty state** - First-time user experience

### Screenshot Specs:
- **JPEG or PNG** (no alpha)
- **16:9 aspect ratio**
- **Minimum:** 320px
- **Maximum:** 3840px
- **Recommended:** 1080x1920 (phone) or 1920x1080 (landscape)

### How to Capture:
1. Run app on emulator (Pixel 5, Android 13+)
2. Use Android Studio's screenshot tool
3. Or use device screenshot and upload

## Feature Graphic (Required)

- **Size:** 1024 x 500px
- **Format:** PNG or JPEG (no alpha)
- **Content:** App name + visual showing widget on phone

You can create this with:
- Canva (free templates)
- Figma
- Photoshop/GIMP

## App Icon

- **Size:** 512 x 512px
- **Format:** PNG (32-bit, with alpha)
- **High-res version of your app's launcher icon**

## Video (Optional but Recommended)

- Upload to YouTube (unlisted)
- 30-60 second demo
- Show: adding habit → seeing it on widget → tapping to complete → streak increment
- Add to store listing

## Store Listing Checklist

Before submitting:

- [ ] App title (30 chars)
- [ ] Short description (80 chars)
- [ ] Full description (up to 4000 chars)
- [ ] App icon (512x512)
- [ ] Feature graphic (1024x500)
- [ ] At least 2 screenshots
- [ ] Privacy policy URL
- [ ] Content rating questionnaire completed
- [ ] App category selected
- [ ] Signed AAB uploaded
- [ ] Pricing set ($0.00 for base app)
- [ ] In-app products configured (Pro upgrade $1.99)

## In-App Purchase Setup

1. **Go to:** Play Console → Your App → Monetize → Products
2. **Create product:**
   - Product ID: `pro_version_upgrade`
   - Name: "Pro Version"
   - Description: "Unlock unlimited habits, custom themes, advanced stats, and cloud backup"
   - Price: $1.99
3. **Activate** the product

## Submission Steps

1. **Go to Play Console** → Your app
2. **Production** → Create new release
3. **Upload AAB** (app-release.aab)
4. **Release name:** "1.0.0 - Initial Release"
5. **Release notes:**
   ```
   Initial release of Habit Streak Widget!

   Features:
   • Beautiful home screen widget
   • Track up to 3 habits (free) or unlimited (Pro)
   • Streak tracking
   • One-tap habit completion
   • 30+ emoji icons
   • Ad-free experience
   ```
6. **Review** → Save → Submit for review

## Review Timeline

- **Initial review:** 2-7 days (usually 2-3)
- **Updates:** 1-2 days

## After Approval

### 1. Launch Checklist
- [ ] Test install from Play Store on real device
- [ ] Post on r/AndroidApps
- [ ] Submit to ProductHunt
- [ ] Share on Twitter/X
- [ ] Email to Android Police, Android Authority

### 2. Monitor
- Check reviews daily (respond to all)
- Monitor crash reports in Play Console
- Track installs and conversion rates

### 3. ASO (App Store Optimization)
- Update description based on what keywords work
- A/B test screenshots
- Monitor competitor apps

## Common Rejection Reasons

1. **Missing privacy policy** - Must have one
2. **Misleading screenshots** - Must show actual app
3. **Permissions not explained** - Add to description
4. **Broken functionality** - Test thoroughly before submitting

## Troubleshooting

### "App not optimized for tablets"
- Add tablet screenshots (optional but recommended)

### "Version code conflict"
- Increment `versionCode` in `build.gradle.kts`

### "Signing key mismatch"
- You're using a different keystore than initial upload
- Cannot be fixed - must match original key

## Next Steps After Launch

1. **Week 1:** Soft launch, gather feedback
2. **Week 2:** Fix any critical bugs (update to 1.0.1)
3. **Week 3:** Marketing push
4. **Month 2:** Add requested features (update to 1.1.0)

## Resources

- [Play Console Help](https://support.google.com/googleplay/android-developer)
- [Android App Bundle Guide](https://developer.android.com/guide/app-bundle)
- [Play Store Best Practices](https://developer.android.com/distribute/best-practices)

---

**You're ready to launch!** 🚀

Expected time to complete submission: **30-60 minutes** (after developer account is verified)
