# Implementation plan

Current status: stage 1 UI and validation implemented; build, three validation tests and lint pass. Emulator launch/initial rendering checked. Interactive acceptance review remains pending before the stage-1 tag. Follow the stages in order and validate each before creating its tag.

| Stage | Scope | Acceptance and validation |
|---|---|---|
| 1: Details | Title, name, birthday, picture and Show birthday screen button; minimal styling | Required fields visible; button requires nonblank name and a birthday; verify empty, partial and valid form states |
| 2: Persistence | Edit name/date; gallery and camera; durable profile repository | Values survive relaunch; photo files remain readable; cancellation/failure preserves prior photo; test repository and ViewModel behavior |
| 3: Birthday | Accurate age, random theme on entry, design assets, two-line name and close/back behavior | Test age boundaries; theme remains stable during recomposition; compare all themes and photo states against Figma; verify small screens and larger fonts |
| 4: Photo update | Camera overlay reuses gallery/camera workflow | Both screens receive updates; photo persists; theme stays unchanged; verify cancellation, errors and accessibility |
| 5: Sharing | Render birthday card and open Android Sharesheet | Export contains current content without close/camera/share controls or system bars; verify output image and temporary URI access |

## Decisions

- Completed months below one year, then completed years; newborns show 0 months old.
- Automatic center crop for selected photos; a separate theme-colored border surrounds placeholder and photo alike.
- Consistent Android sans-serif typography; custom top-left close and centered Share the news controls.
- One theme choice per birthday-screen entry; consecutive entries may choose the same theme.
- Proposed input policy: reject future birthdays, validate trimmed names and preserve data on cancelled photo selection.

## Implementation order

Introduce details UI/state first, persistence and platform photo contracts second, then birthday navigation/components. Introduce sharing last using a reusable card renderer. Keep a shared profile repository as the source of truth. Choose a serialized save strategy during stage 2 to prevent stale writes.

## Checks

Use pure date tests with a controlled current date for newborn, month-end, leap-day and first-birthday cases. Use injected theme selection for deterministic tests. Run debug assembly, focused tests and lint as relevant, then inspect device behavior and screenshots. Full-screen reference exports and final background crop comparisons remain outstanding; current assets and recorded measurements support initial implementation.

## Version control

Create an initial foundation commit, then focused feature commits. Add annotated stage-1 through stage-5 tags only when their acceptance criteria pass. Keep machine-specific files and temporary outputs excluded. Repository publication and visibility are configured separately from implementation.
