#!/bin/bash
# Build script for TRMS Driver App

echo "=== TRMS Driver App Build ==="
echo "Starting build at $(date)"

cd /home/c/AndroidStudioProjects/TRMSDriverApp

echo ""
echo "Step 1: Clean project..."
./gradlew clean --no-daemon

echo ""
echo "Step 2: Generate resources..."
./gradlew :app:generateDebugResValues

echo ""
echo "Step 3: Build debug APK..."
./gradlew assembleDebug --no-daemon --stacktrace

if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "✅ BUILD SUCCESSFUL!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    ls -lh app/build/outputs/apk/debug/app-debug.apk
else
    echo ""
    echo "❌ BUILD FAILED - APK not found"
    echo "Check errors above"
    exit 1
fi

