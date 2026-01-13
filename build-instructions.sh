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

echo "=== Customizing the Swarm Implementation ==="
echo ""
echo "The swarmlib module provides a starter implementation."
echo "You can:"
echo "1. Replace the implementation in swarmlib/src/main/java/com/swarm/lib/SwarmNode.java"
echo "2. Or wrap your own .aar by importing it into the swarmlib module"
echo ""

echo "=== Requirements ==="
echo ""
echo "- Java 11 or higher (compatible with JDK 17-21)"
echo "- Gradle 8.13+"
echo "- Android SDK (API 21+)"
echo ""

echo "Build script completed. Review the steps above to build the project."

