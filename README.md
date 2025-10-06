# Real-Time Edge Detection Viewer

An Android application that captures camera frames, processes them using OpenCV (C++), renders the output with OpenGL ES, and provides a web viewer for visualization.

## Features

- 📸 Real-time camera feed capture using CameraX
- 🔍 Edge detection using OpenCV (C++)
- 🎨 OpenGL ES 2.0+ rendering
- 🌐 TypeScript web viewer for processed frames
- 🔄 JNI bridge for native code integration

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── cpp/
│   │   │   ├── CMakeLists.txt
│   │   │   └── native-lib.cpp
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── edgevision/
│   │   │               ├── MainActivity.kt
│   │   │               ├── renderer/
│   │   │               │   └── GLRenderer.kt
│   │   │               └── view/
│   │   │                   └── CameraView.kt
│   │   └── res/
│   │       └── ...
│   └── ...
web/
├── src/
│   ├── index.html
│   └── viewer.ts
├── package.json
└── tsconfig.json
```

## Setup Instructions

### Prerequisites

- Android Studio Arctic Fox or later
- Android NDK
- OpenCV for Android SDK
- Node.js and npm (for web viewer)

### Building the Project

1. Clone the repository
2. Open the project in Android Studio
3. Sync project with Gradle files
4. Build and run on an Android device/emulator

### Setting up the Web Viewer

1. Navigate to the `web` directory
2. Run `npm install`
3. Run `npm start` to start the development server

## Architecture Overview

1. **Camera Capture**: Uses CameraX API to capture camera frames
2. **Image Processing**: OpenCV (C++) for edge detection
3. **Rendering**: OpenGL ES 2.0+ for efficient rendering
4. **Web Interface**: TypeScript-based web viewer for visualization

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
