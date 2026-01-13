# swarm-mobile-android-native
Native Android Swarm Node

## Overview
This is a native Android application for running a Swarm node on Android devices. The project includes a starter Swarm library implementation that you can wrap your own .aar into.

## Project Structure
```
swarm-mobile-android-native/
├── app/                          # Android application module
│   ├── build.gradle             # App build configuration
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/swarm/mobile/
│       │   └── MainActivity.java # Main UI implementation
│       └── res/                 # Resources (layouts, strings, etc.)
├── swarmlib/                    # Swarm library module (builds to .aar)
│   ├── build.gradle            # Library build configuration
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/swarm/lib/
│           └── SwarmNode.java  # Starter Swarm implementation
├── build.gradle                # Root build configuration
├── settings.gradle             # Project settings
└── gradle/                     # Gradle wrapper
```

## Requirements
- Android SDK (API level 21 or higher)
- Java 11 or higher (compatible with JDK 17-21)
- Gradle 8.13 or higher

## Building the Project

### Build the Swarm Library (.aar)
```bash
./gradlew :swarmlib:assembleRelease
```
The .aar file will be generated at: `swarmlib/build/outputs/aar/swarmlib-release.aar`

### Build the Android App
```bash
./gradlew :app:assembleDebug
```
The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Build Everything
```bash
./gradlew build
```

## Features

### Swarm Library (swarmlib)
- `SwarmNode` class with lifecycle management
- Start/stop node functionality
- Peer connection management
- Event listener interface for status updates

### Android App
- Material Design UI with card-based layout
- Node information display (Node ID, Status)
- Start/Stop controls for the Swarm node
- Peer connection interface
- Connected peers list
- Real-time status updates

## UI Components
1. **Node Information Card**: Displays the unique node ID and current status
2. **Control Buttons**: Start and Stop buttons to manage node lifecycle
3. **Peer Connection**: Input field and button to connect to other peers
4. **Peers List**: Shows all currently connected peers

## Usage
1. Build the app using Gradle
2. Install the APK on an Android device (API 21+)
3. Launch the "Swarm Mobile" app
4. Tap "Start Node" to start the Swarm node
5. Enter a peer ID and tap "Connect Peer" to establish connections
6. View connected peers in the peers list
7. Tap "Stop Node" when done

## Customizing the Swarm Implementation
The `swarmlib` module provides a starter implementation. You can:
- Replace the implementation in `swarmlib/src/main/java/com/swarm/lib/SwarmNode.java`
- Or wrap your own .aar by importing it into the swarmlib module

## Development
The app is written in Java 11 and uses:
- AndroidX libraries
- Material Components for Android
- Gradle build system
- Minimum SDK: API 21 (Android 5.0)
- Target SDK: API 34 (Android 14)

## Gradle Version
This project uses Gradle 8.13, which is compatible with:
- Android Gradle Plugin 8.7.3
- Java 11-21
- Android Studio Ladybug (2024.2.1) and later

## License
This project is part of the Swarm ecosystem.
