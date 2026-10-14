# NanitHappyBirthday

Android implementation of Nanit's Happy Birthday assignment using Kotlin and Jetpack Compose.

## Status

Stage 1 details UI is implemented: name, birthday picker, picture placeholder and required-field validation. Durable persistence, photo selection and birthday-screen navigation follow in stages 2-3.

## Build and run

Open this directory in Android Studio, select JDK 17 as the Gradle JDK, sync, and run on an API 28+ device or emulator. Android SDK 35 is required for compilation.

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Debug assembly, three form-validation unit tests and lint pass. The details screen was installed and launched on an emulator without a crash. Full interactive/device review remains pending.

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

At this stage, Show birthday screen confirms that details are ready; the birthday destination is implemented in stage 3. SavedStateHandle restores the form after configuration changes/system recreation, but durable relaunch persistence belongs to stage 2.
