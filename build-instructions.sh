#!/bin/bash

# Swarm Mobile Build Instructions

echo "=== Swarm Mobile Android Build Script ==="
echo ""

# Step 1: Add .aar file
echo "Step 1: Add your Swarm library .aar file"
echo "Place your .aar file in the app/libs/ directory"
echo "Example: cp your-swarm-library.aar app/libs/"
echo ""
echo "The .aar must provide the com.swarm.lib.SwarmNode class"
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
echo "The app module loads .aar files from app/libs/ via:"
echo "  dependencies {"
echo "      implementation fileTree(dir: 'libs', include: ['*.aar'])"
echo "  }"
echo ""
echo "When built, Gradle automatically:"
echo "1. Includes all .aar files from app/libs/"
echo "2. Merges the .aar into the app's dependencies"
echo "3. Packages everything into the final APK"
echo ""

echo "=== Requirements ==="
echo ""
echo "- Java 11 or higher (compatible with JDK 17)"
echo "- Gradle 8.5+"
echo "- Android SDK (API 21+)"
echo "- Your Swarm library .aar file in app/libs/"
echo ""

echo "Build script completed. Review the steps above to build the project."

