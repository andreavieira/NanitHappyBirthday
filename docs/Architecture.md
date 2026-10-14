# Architecture

Start with one activity and one app module. Add feature/details and feature/birthday packages as their stages begin, data/profile for persistence, and ui/theme for design tokens. Composables render immutable state and send actions to screen ViewModels; ViewModels coordinate a profile repository. Platform launchers stay in the UI/platform boundary.

Installed foundation: Kotlin 2.1.20, AGP 8.9.2, Gradle 8.11.1, Java 17 bytecode, compile/target SDK 35, minimum SDK 28, Compose BOM 2025.04.01, Material3 1.4.0, Activity Compose 1.10.1, Lifecycle 2.8.7, Coroutines 1.10.2, JUnit4. Versions are pinned for a reproducible build.

Add Preferences DataStore in stage 2 for a small profile; copy selected images into app-private storage for durable access. Use Activity Result photo picker and TakePicture with FileProvider. Add Coil only when displaying photo URIs. For two screens, decide navigation during stage 3. Use manual constructor injection; Hilt, Room, Retrofit, and multi-module infrastructure are unnecessary at present. Date arithmetic uses java.time with an injectable clock and boundary tests. Select the visual variant on screen entry, not recomposition. Sharing should render a separate card composable without controls.

Reference: https://developer.android.com/topic/architecture/recommendations

## Shared baby image component

Use one reusable circular baby image component for both states: a filled, borderless theme placeholder when no photo exists, or the selected photo when available. Clip the displayed content to a circle and draw the theme-colored outer border separately in Compose in both states. Keep border geometry independent of the image source. The camera action is a separate overlay. See DesignAssets.md for asset selection and reference colors; avoid baking the photo border into placeholder exports.

## Details screen boundary

MainActivity hosts the app theme and DetailsRoute. DetailsRoute collects ViewModel state, wires callbacks and hosts UI/platform interactions. DetailsScreen accepts immutable state and callbacks without depending on the ViewModel or an Activity. The platform date picker is created by a private UI helper; its Context stays outside the ViewModel.

The date picker's maximum date is a UI constraint. Stage 2 will also validate profile writes at the repository boundary, using a controlled current date for tests, so restored values or other callers cannot bypass the rule.
