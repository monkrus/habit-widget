#!/bin/bash

# Habit Streak Widget - Quick Build & Test Script

set -e

echo "========================================="
echo "Habit Streak Widget - Build & Test"
echo "========================================="
echo ""

PROJECT_DIR="/home/user/habit-tracker-widget"
cd "$PROJECT_DIR"

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ Error: gradlew not found!"
    exit 1
fi

echo "📦 Building debug APK..."
echo ""

# Build debug APK
./gradlew assembleDebug --console=plain

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "📱 APK Location:"
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    echo "   $PROJECT_DIR/$APK_PATH"
    echo ""

    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "📊 APK Size: $APK_SIZE"
        echo ""
    fi

    echo "🔧 Next Steps:"
    echo ""
    echo "Option 1 - Install via ADB (if phone connected):"
    echo "   adb install $APK_PATH"
    echo ""
    echo "Option 2 - Copy to phone:"
    echo "   Transfer the APK file above to your Android phone"
    echo "   Tap it to install (may need to enable 'Install from unknown sources')"
    echo ""
    echo "Option 3 - Test in emulator:"
    echo "   1. Install Android Studio"
    echo "   2. Create an emulator (Pixel 5, API 34)"
    echo "   3. Run: adb install $APK_PATH"
    echo ""
else
    echo "❌ Build failed!"
    echo ""
    echo "Common issues:"
    echo "1. Java not installed (need JDK 17)"
    echo "2. Android SDK not found"
    echo "3. Network issues downloading dependencies"
    echo ""
    echo "Solution: Use Android Studio instead"
    exit 1
fi
