# Architecture

Start with one activity and one app module. Add feature/details and feature/birthday packages as their stages begin, data/profile for persistence, and ui/theme for design tokens. Composables render immutable state and send actions to screen ViewModels; ViewModels coordinate a profile repository. Platform launchers stay in the UI/platform boundary.

Installed foundation: Kotlin 2.1.20, AGP 8.9.2, Gradle 8.11.1, Java 17 bytecode, compile/target SDK 35, minimum SDK 28, Compose BOM 2025.04.01, Material3 1.4.0, Activity Compose 1.10.1, Lifecycle 2.8.7, Coroutines 1.10.2, JUnit4. Versions are pinned for a reproducible build.

Stage 2 uses Preferences DataStore 1.1.7 for profile fields and filenames, with copied images in app-private storage. Use Activity Result photo picker and TakePicture with FileProvider. Coil Compose 2.7.0 displays the private image with a circular center crop. For two screens, decide navigation during stage 3. Use manual constructor injection; Hilt, Room, Retrofit, and multi-module infrastructure are unnecessary at present. Date arithmetic uses java.time with an injectable clock and boundary tests. Select the visual variant on screen entry, not recomposition. Sharing should render a separate card composable without controls.

Reference: https://developer.android.com/topic/architecture/recommendations

## Shared baby image component

Use one reusable circular baby image component for both states: a filled, borderless theme placeholder when no photo exists, or the selected photo when available. Clip the displayed content to a circle and draw the theme-colored outer border separately in Compose in both states. Keep border geometry independent of the image source. The camera action is a separate overlay. See DesignAssets.md for asset selection and reference colors; avoid baking the photo border into placeholder exports.

## Details screen boundary

MainActivity hosts the app theme and DetailsRoute. DetailsRoute collects ViewModel state, wires callbacks and hosts UI/platform interactions. DetailsScreen accepts immutable state and callbacks without depending on the ViewModel or an Activity. The platform date picker is created by a private UI helper; its Context stays outside the ViewModel.

The date picker's maximum date is a UI constraint. The repository also validates profile writes against an injected Clock, so other callers cannot bypass the future-date rule.

## Profile persistence

NanitApplication owns one repository/DataStore instance. ProfileStore abstracts transactional metadata updates; PrivateProfilePhotos isolates ContentResolver/file operations on Dispatchers.IO. The ViewModel loads the persisted profile and holds name/date edits in memory until Show birthday screen is tapped. Photo imports run through a channel and persist immediately. DataStore updates preserve unrelated fields. Continuation requires a nonblank name and birthday and waits for active photo import or saving to finish.

New photos are copied to unique private files and checked for image dimensions and a 20 MB size limit before their filenames are stored. Metadata write failure removes the uncommitted copy; successful replacement removes the previous file. FileProvider exposes only the camera cache directory. The shared camera helper retains the pending capture URI across recreation and cleans it after completion/cancellation. Successful copies do not depend on ongoing gallery URI permission.

Name/date save failure returns a result for snackbar feedback and does not block continuation with the in-memory draft. Stage 3 must navigate regardless of save success. Initial load failures offer Retry. Photo/camera errors appear in a compact card with a close control and clear when Choose picture is tapped again; failed imports retain the old photo. Unsubmitted edits are lost if the process terminates.

## Shared photo selection UI

`ui/photo` contains the gallery launcher, camera capture launcher, and source-choice dialog for reuse by the details and birthday screens. These helpers expose callbacks without depending on a feature ViewModel or repository. The caller decides how to import and persist the selected image, and invokes the camera completion callback after import so temporary capture files can be removed safely. `platform/photo` owns FileProvider URI creation and file operations. The birthday-specific date picker stays in `feature/details` until another feature needs it.

## Birthday theme selection

BirthdayTheme maps each variant to its measured colors and supplied illustration, placeholder, and camera resources. NanitApp chooses a theme only when handling entry to the birthday destination and stores its enum name with rememberSaveable alongside the displayed profile snapshot. Recomposition and activity recreation preserve that selection; closing and entering again performs another independent choice, which may repeat the previous variant. BirthdayScreen receives its theme explicitly and separates illustration, heading, photo, and back-control rendering. The app container uses the same theme background around system insets.

## Code documentation

Keep concise purpose/responsibility KDoc on classes, interfaces, enums and named objects. Function comments, including composable KDoc, should explain non-obvious behavior, constraints or intent; omit descriptions that repeat clear names or implementation.

## Emulator test workflow

Use `python3 scripts/RunEmulatorTests.py` with the project's Gradle JDK configured in `JAVA_HOME`. It builds both APKs, installs them with `adb install -r`, and invokes the instrumentation runner directly, leaving the app and its private storage installed. Use `--serial` when multiple devices are connected and `--test-class` to limit the run. Avoid Gradle connected-test cleanup on an emulator containing review data. Navigation tests restore the existing profile; persistence tests use isolated storage. This workflow preserves data during updates; it does not restore data already removed by an uninstall.
