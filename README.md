# Palette App

An Android application built with Kotlin and Jetpack Compose that extracts 5 dominant colors from a selected image.

## Features

- **Image Selection**: Use the system Photo Picker to select an image.
- **Color Extraction**: Extracts 5 dominant colors using the `androidx.palette` library.
- **Display**: Shows the selected image and a list of the 5 dominant colors.
- **Copy**: Copy the HEX code of any color to the clipboard.
- **State Preservation**: Survives screen rotation.

## Requirements

- JDK 17
- Android SDK 34

## Build Instructions

1.  Clone the repository.
2.  Open the project in Android Studio or use the command line.
3.  To build the debug APK:
    ```bash
    ./gradlew :app:assembleDebug
    ```
4.  To run unit tests:
    ```bash
    ./gradlew :app:testDebug
    ```

## CI/CD

The project includes a GitHub Actions workflow that builds the debug APK on every push and pull request. The APK artifact is uploaded and available for download in the Actions tab.

## Architecture

- **UI**: Jetpack Compose
- **Logic**: `HexUtils.kt` handles color extraction and HEX conversion.
- **Tests**: Unit tests for color logic using Robolectric.
