#!/bin/bash

# Swarm Mobile Build Instructions

echo "=== Swarm Mobile Android Build Script ==="
echo ""

# Step 1: Build the library
echo "Step 1: Building swarmlib (.aar)..."
echo "./gradlew :swarmlib:assembleRelease"
echo ""
echo "This will create: swarmlib/build/outputs/aar/swarmlib-release.aar"
echo ""

# Step 2: Build the app
echo "Step 2: Building the Android app..."
echo "./gradlew :app:assembleDebug"
echo ""
echo "This will create: app/build/outputs/apk/debug/app-debug.apk"
echo ""

# Step 3: Install (optional)
echo "Step 3 (Optional): Install on connected device..."
echo "./gradlew :app:installDebug"
echo ""

echo "=== How the .aar is embedded ==="
echo ""
echo "The app module depends on the swarmlib module via:"
echo "  dependencies {"
echo "      implementation project(':swarmlib')"
echo "  }"
echo ""
echo "When built, Gradle automatically:"
echo "1. Compiles swarmlib into a .aar file"
echo "2. Includes the .aar in the app's dependencies"
echo "3. Packages everything into the final APK"
echo ""

echo "=== Alternative: Using a pre-built .aar ==="
echo ""
echo "To use a pre-built .aar instead of the module:"
echo "1. Copy swarmlib-release.aar to app/libs/"
echo "2. In app/build.gradle, replace:"
echo "     implementation project(':swarmlib')"
echo "   with:"
echo "     implementation files('libs/swarmlib-release.aar')"
echo ""

echo "Build script created. Review the steps above to build the project."
