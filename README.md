# swarm-mobile-android-native
Native Android Swarm Node

## Overview
This is a native Android application for running a Swarm node on Android devices. 
The project includes a Swarm wrapper implementation that you can use in your own Android app to interact with Swarm network.

## Project Structure
```
swarm-mobile-android-native/
├── app/                            # Android application module
│   ├── build.gradle                # Application build configuration
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/swarm/mobile/  # Main app code
│       └── res/                    # Resources (layouts, strings, etc.)
├── swarmlib/                       # Swarm library module (builds to .aar)
│   ├── build.gradle                # Library build configuration
│   ├── libs                        # place bee-lite.aar file and jar here
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/swarm/lib/
│           └── SwarmNode.java  # Swarm wrapper implementation
├── build.gradle                # Root build configuration
├── settings.gradle             # Project settings
└── gradle/                     # Gradle wrapper
```

## Requirements
- Android SDK (API level 21 or higher)
- Java 17 or higher
- Gradle 8.13 or higher

## Building the Project

## Get Swarm bee-lite binaries
// TODO 

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

### Android App
- Material Design UI with card-based layout
- Node information display (Node ID, Status)
- Start/Stop controls for the Swarm node
- Real-time status updates

## UI Components
1. **Node Information Card**: Displays the unique node ID and current status
2. **Control Buttons**: Start and Stop buttons to manage node lifecycle
3. **Peer Connection**: Input field and button to connect to other peers
4. **Peers Count**: Shows all currently connected peers count

## Usage
1. Build the app using Gradle
2. Install the APK on an Android device (API 21+)
3. Launch the "Swarm Mobile" app
4. Tap "Start Node" to start the Swarm node
5. Enter a peer ID and tap "Connect Peer" to establish connections
6. View connected peers in the peers list
7. Tap "Stop Node" when done


## Development
The app is written in Java 17 and uses:
- AndroidX libraries
- Material Components for Android
- Gradle build system
- Minimum SDK: API 21 (Android 5.0)
- Target SDK: API 36 (Android 16)

## Gradle Version
This project uses Gradle 8.13, which is compatible with:
- Android Gradle Plugin 8.7.3
- Java 17-21
- Android Studio Ladybug (2024.2.1) and later

