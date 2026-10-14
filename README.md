# NanitHappyBirthday

Android implementation of Nanit's Happy Birthday assignment using Kotlin and Jetpack Compose.

## Status

Stage 1 is accepted. Stage 2 adds persistent name/birthday, gallery and camera selection, and durable private photo storage; review is pending. Birthday-screen navigation follows in stage 3.

## Build and run

Open this directory in Android Studio, select JDK 17 as the Gradle JDK, sync, and run on an API 28+ device or emulator. Android SDK 35 is required for compilation.

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Stage 1 is marked by the stage-1 tag. Stage 2 validation includes debug assembly, 13 unit tests, lint, an isolated on-device DataStore/photo-storage test, and emulator gallery/camera selection and cancellation checks.

## Implementation

Single activity and app module. Compose screens use ViewModel/StateFlow and lifecycle-aware state collection. Persistence is isolated behind a profile repository; camera, gallery and sharing remain at platform boundaries. Dependencies are added as each feature requires them.

## Documentation

- docs/Requirements.md: assignment acceptance checklist.
- docs/ProjectPlan.md: implementation stages and validation.
- docs/Architecture.md: component responsibilities and toolchain.
- docs/DesignImplementation.md: layout and interaction decisions.
- docs/FigmaMeasurements.md: reference measurements and open visual questions.
- docs/DesignAssets.md and docs/AssetManifest.json: asset usage and source mapping.

## Assignment stages

1. Details screen.
2. Editing, photos and persistence.
3. Birthday screen, age calculation and themes.
4. Photo replacement from the birthday screen.
5. Shareable birthday image.

Use focused commits and stage tags after validation. Supplied Nanit artwork is proprietary and restricted to this assignment.

At this stage, Show birthday screen confirms that details are ready; the birthday destination is implemented in stage 3. DataStore restores the form after relaunch. Name and birthday save when Show birthday screen is tapped. A save failure shows a snackbar and permits continuation with the in-memory draft. Initial load failures offer Retry. Photo imports retain the existing picture on failure or cancellation; images over 20 MB are rejected.

## Device checks

With an emulator/device connected, run `./gradlew connectedDebugAndroidTest` for the isolated storage recreation test. For manual review, edit name/date, select a gallery picture, force-stop and reopen the app, then check camera capture and cancellation. The app uses the system photo picker (with document-picker fallback) and external camera capture; broad gallery/camera permissions are not requested. Personal test images belong on the device only and are not repository assets.
