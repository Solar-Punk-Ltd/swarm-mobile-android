# swarm-mobile-android
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

## Get Swarm bee-lite-java binaries
Follow the instructions at [bee-lite-java](https://github.com/Solar-Punk-Ltd/bee-lite-java) repository to get the binary.
Copy the .aar file and the .jar into the `swarmlib/libs/` directory.
At build.gradle these files already referenced but double check `implementation(name: 'mobile', ext: 'aar')` - rename if you wish.
More help in this topic here in a [POC](https://github.com/Solar-Punk-Ltd/GoOnAndroidPoc)

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
- Start/Stop controls for the Swarm node in [ultra-light or light mode](https://docs.ethswarm.org/docs/bee/installation/getting-started#choosing-a-node-type)
- Real-time status updates
- Download functionality using Swarm hash
- List / Buy Stamps for upload
- Simple file upload to Swarm with upload history

## Usage - ultra-light mode
1. Build the app using Gradle
2. Install the APK on an Android device (API 21+)
3. Launch the "Swarm Mobile" app
4. Select "Ultra-Light Mode"
5. Tap "Start Node" to start the Swarm node
6. Enter swarm hash to download

## Usage - light mode
1. Build the app using Gradle
2. Install the APK on an Android device (API 21+)
3. Launch the "Swarm Mobile" app
4. Select "Light Mode"
5. Fill required RPC Endpoint (e.g. https://xdai.fairdatasociety.org) 
6. Tap "Start Node" to start the Swarm node
7. After Wallet Address appears need to fund the wallet. Recommended to use this [app](https://fund.ethswarm.org/)
8. Recommended minimal 0.1xDai for the node to be able to deploy a [Chequebook](https://docs.ethswarm.org/docs/bee/installation/quick-start#get-your-nodes-address) and a fex xBZZ for postage batches.
9. Restart App
10. After the node shows "Running" status and has connected peers then on the Upload tab Stamps should be bought for upload with the "Create" button.
11. Select Stamp from the list
12. Browse a file to upload and tap "Upload" button
13. After upload completes the file hash will be shown in the upload history list

## Possible improvements
- Add in app lint to the fund app with prefilled address and amount
- Hash quick share
- Upload complete folder
- Upload multiple selected files at once
- Stamp estimated cost calculator before buy
- Stamp estimated validity calculator before buy
- Stamp remaining validity display
- Availability of the uploaded content in the history list (based on the used stamp)
- Stamp [Topup and Dilude](https://docs.ethswarm.org/docs/desktop/postage-stamps/#dilute-a-batch) functionality  

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

