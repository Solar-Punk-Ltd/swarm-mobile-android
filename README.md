# swarm-mobile-android-native
Native Android Swarm Node

## Overview
This is a native Android application for running a Swarm node on Android devices. The app uses an external Swarm library (.aar file) that you need to provide.

## Project Structure
```
swarm-mobile-android-native/
├── app/                          # Android application module
│   ├── build.gradle             # App build configuration
│   ├── libs/                    # Place your .aar file here
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/swarm/mobile/
│       │   └── MainActivity.java # Main UI implementation
│       └── res/                 # Resources (layouts, strings, etc.)
├── build.gradle                # Root build configuration
├── settings.gradle             # Project settings
└── gradle/                     # Gradle wrapper
```

## Requirements
- Android SDK (API level 21 or higher)
- Java 11 or higher (compatible with JDK 17)
- Gradle 8.5 or higher
- Swarm library .aar file (see below)

## Setting Up the .aar Library

1. Place your Swarm library `.aar` file in the `app/libs/` directory
2. The .aar file must provide the following class:
   - `com.swarm.lib.SwarmNode` with:
     - Constructor: `SwarmNode()`
     - Methods: `start()`, `stop()`, `isRunning()`, `getNodeId()`, `connectPeer(String)`, `disconnectPeer(String)`
     - Interface: `SwarmNodeListener` with callbacks: `onStatusChanged(String)`, `onPeerConnected(String)`, `onPeerDisconnected(String)`
     - Methods: `addListener(SwarmNodeListener)`, `removeListener(SwarmNodeListener)`

The app will automatically include all `.aar` files from the `app/libs/` directory.

## Building the Project

### Build the Android App
```bash
./gradlew :app:assembleDebug
```
The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release Version
```bash
./gradlew :app:assembleRelease
```

### Install on Connected Device
```bash
./gradlew :app:installDebug
```

## Features

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
1. Place your Swarm library .aar file in `app/libs/`
2. Build the app using Gradle
3. Install the APK on an Android device (API 21+)
4. Launch the "Swarm Mobile" app
5. Tap "Start Node" to start the Swarm node
6. Enter a peer ID and tap "Connect Peer" to establish connections
7. View connected peers in the peers list
8. Tap "Stop Node" when done

## Development
The app is written in Java 11 and uses:
- AndroidX libraries
- Material Components for Android
- Gradle build system
- Minimum SDK: API 21 (Android 5.0)
- Target SDK: API 34 (Android 14)

## Gradle Version
This project uses Gradle 8.5, which is compatible with:
- Android Gradle Plugin 8.1.0+
- Java 11-19
- Android Studio Hedgehog (2023.1.1) and later

## License
This project is part of the Swarm ecosystem.
