# Gemini Palette

This is an Android application built with Kotlin and Jetpack Compose that allows users to pick an image and extract 5 dominant colors from it.

## Features

- **Image Selection**: Pick an image from the gallery using the system Photo Picker.
- **Color Extraction**: Extracts 5 dominant colors using the Android Palette API.
- **Color Display**: Displays the extracted colors as HEX codes with preview chips.
- **Copy to Clipboard**: Tap a color chip to copy the HEX code to the clipboard.
- **Fallback**: Automatically generates complementary colors if extracted colors are fewer than 5.
- **Dark/Light Mode Support**: Follows system theme.
- **Orientation Support**: Preserves state across screen rotations.

## Build and Run

### Prerequisites

- JDK 17
- Android SDK (API 34)

### Commands

1.  **Build Debug APK**:
    ```bash
    ./gradlew :app:assembleDebug
    ```
    The APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

2.  **Run Unit Tests**:
    ```bash
    ./gradlew :app:testDebug
    ```

## CI/CD

The project includes a GitHub Actions workflow (`.github/workflows/build.yml`) that runs on every push and pull request to:
1.  Set up the environment (JDK 17).
2.  Build the debug APK.
3.  Run unit tests.
4.  Upload the debug APK as an artifact.

## Tech Stack

- **Language**: Kotlin 2.0.0
- **UI Toolkit**: Jetpack Compose
- **Color Extraction**: androidx.palette
- **Build System**: Gradle 8.7
- **Testing**: JUnit, Robolectric

## Architecture

- **HexUtils**: Handles extraction and processing of colors.
- **MainActivity**: Manages UI state and composition.
