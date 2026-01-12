# swarm-mobile-android-native
Native Android Swarm Node

## Overview
This is a native Android application for running a Swarm node on Android devices. The project consists of:
- **swarmlib**: An Android library module that provides Swarm node functionality (built as .aar)
- **app**: An Android application that embeds the swarmlib .aar and provides a user interface

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
│           └── SwarmNode.java  # Core Swarm functionality
├── build.gradle                # Root build configuration
├── settings.gradle             # Project settings
└── gradle/                     # Gradle wrapper

```

## Requirements
- Android SDK (API level 24 or higher)
- Java 8 or higher
- Gradle 8.0 or higher

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
1. Install the APK on an Android device (API 24+)
2. Launch the "Swarm Mobile" app
3. Tap "Start Node" to start the Swarm node
4. Enter a peer ID and tap "Connect Peer" to establish connections
5. View connected peers in the peers list
6. Tap "Stop Node" when done

## Development
The app is written in Java and uses:
- AndroidX libraries
- Material Components for Android
- Gradle build system

## License
This project is part of the Swarm ecosystem.
