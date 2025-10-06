cmake_minimum_required(VERSION 3.18.1)

# Set the path to the OpenCV Android SDK
set(OpenCV_DIR C:/Users/abhis/Downloads/OpenCV-android-sdk/sdk/native/jni)
find_package(OpenCV 4.12 REQUIRED java)

# Add the native library
add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)

# Include OpenCV headers
include_directories(${OpenCV_INCLUDE_DIRS})

# Link OpenCV libraries
target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${OpenCV_LIBS})
