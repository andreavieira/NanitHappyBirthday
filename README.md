# NanitHappyBirthday

Android implementation of Nanit's Happy Birthday assignment using Kotlin and Jetpack Compose.

## Status

All five stages are implemented. Annotated stage tags identify the accepted deliverable checkpoints; see the project plan for requirements and validation.

## Build and run

Open this directory in Android Studio, select JDK 17 as the Gradle JDK, sync, and run on an API 28+ device or emulator. Compilation requires Android SDK 35 and Build Tools 36.0.0.

```sh
./gradlew formatKotlin
./gradlew checkKotlinFormatting assembleDebug testDebugUnitTest lintDebug
```

Formatting uses ktfmt's Kotlin style with four-space indentation and a 100-character line width. `checkKotlinFormatting` also runs as part of `check` and reports formatting differences without changing files.

## Project guidance

See [ProjectPlan.md](docs/ProjectPlan.md) for requirements, architecture, design measurements and adaptations, persistence/error behavior, and validation criteria. [AssetManifest.json](docs/AssetManifest.json) maps supplied originals to Android resources.

Supplied Nanit artwork is proprietary and restricted to this assignment.

## Device checks

With an emulator running and a compatible JDK selected through `JAVA_HOME`:

```sh
python3 scripts/RunEmulatorTests.py --serial emulator-5554
```

This uses replacement installs and direct instrumentation to preserve stored app data. Avoid `connectedDebugAndroidTest` on an emulator containing review data because its cleanup may uninstall the app. Profile tests restore previous values or use isolated storage.

For manual review, edit name/date, select a gallery picture, force-stop and reopen, then check camera capture and cancellation. The app uses the system photo picker with document-picker fallback and external camera capture; broad gallery/camera permissions are not requested.
