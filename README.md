# NanitHappyBirthday

Android implementation of Nanit's Happy Birthday assignment using Kotlin and Jetpack Compose.

## Status

Stages 1–4 are accepted and tagged. Stage 5 sharing is implemented and under review.

## Build and run

Open this directory in Android Studio, select JDK 17 as the Gradle JDK, sync, and run on an API 28+ device or emulator. Compilation requires Android SDK 35 and Build Tools 36.0.0.

```sh
./gradlew formatKotlin
./gradlew checkKotlinFormatting assembleDebug testDebugUnitTest lintDebug
```

Formatting uses ktfmt's Kotlin style with four-space indentation and a 100-character line width. `checkKotlinFormatting` also runs as part of `check` and reports formatting differences without changing files.

## Implementation

Single activity and app module. Compose screens use ViewModel/StateFlow and lifecycle-aware state collection. A profile repository isolates persistence; camera, gallery and sharing remain at platform boundaries.

DataStore restores the profile after relaunch. Name and birthday save automatically as they change; photo changes persist immediately. Show birthday screen flushes the current details before continuing. A name/date save failure shows a snackbar and permits continuation with the in-memory draft. Photo failures or cancellation preserve the previous image. Images must be readable and smaller than 20 MB.

The birthday screen selects a theme on each entry, displays completed months below one year and completed years afterward, and exports a shareable card without controls or system bars.

## Documentation

- docs/Requirements.md: assignment acceptance checklist.
- docs/ProjectPlan.md: implementation stages and validation.
- docs/Architecture.md: component responsibilities and toolchain.
- docs/DesignImplementation.md: layout and interaction decisions.
- docs/FigmaMeasurements.md: reference measurements.
- docs/DesignAssets.md and docs/AssetManifest.json: asset usage and source mapping.

## Assignment stages

1. Details screen.
2. Editing, photos and persistence.
3. Birthday screen, age calculation and themes.
4. Photo replacement from the birthday screen.
5. Shareable birthday image.

Use focused commits and stage tags after acceptance and validation. Supplied Nanit artwork is proprietary and restricted to this assignment.

## Device checks

With an emulator running and a compatible JDK selected through `JAVA_HOME`:

```sh
python3 scripts/RunEmulatorTests.py --serial emulator-5554
```

This uses replacement installs and direct instrumentation to preserve stored app data. Avoid `connectedDebugAndroidTest` on an emulator containing review data because its cleanup may uninstall the app. Profile tests restore previous values or use isolated storage.

For manual review, edit name/date, select a gallery picture, force-stop and reopen, then check camera capture and cancellation. The app uses the system photo picker with document-picker fallback and external camera capture; broad gallery/camera permissions are not requested.

## Review recordings

On macOS, record separate persistence, theme, photo, sharing and date-boundary demos:

```sh
python3 scripts/RecordReviewDemos.py --photo /path/to/review-photo.jpeg
```

The script builds a separate demo APK and preserves the normal app's profile. Generated media stays in ignored `outputs/ReviewDemos`; review it before publishing and use a photo approved for the intended audience. Personal photos are not repository assets.
