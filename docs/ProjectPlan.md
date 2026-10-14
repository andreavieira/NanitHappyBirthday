# Implementation plan

Current status: stage 1 accepted after build, three validation tests, lint and emulator review. The stage-1 tag marks this checkpoint. Stage 2 is accepted and merged; stage-2 points to its final merge commit. Stage 3 is accepted and tagged; Stage 4 is implemented locally for review and Stage 5 remains pending. Follow the stages in order and validate each before creating its tag.

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
- Input policy: reject future birthdays, validate trimmed names and preserve data on cancelled photo selection.

## Implementation order

Stage 3 uses separate reviewable changes: navigation first, then theme/asset mapping, age calculation and number artwork, and final card layout with responsive previews. The first navigation checkpoint adds the birthday destination, close/Back handling, recreation-safe destination data, and nonblocking save-failure feedback. Its emulator navigation test passes, and Android Studio preview rendering was confirmed during review. Capture UI behavior with scripts/RecordNavigationDemo.py; recordings go to ignored outputs/ and are attached separately as review evidence.

Introduce details UI/state first, persistence and platform photo contracts second, then birthday navigation/components. Introduce sharing last using a reusable card renderer. Keep a shared profile repository as the source of truth. Stage 2 saves name/date on continuation and serializes photo imports through a ViewModel channel; DataStore transactions update only the relevant fields. Save failure reports a snackbar without blocking the birthday destination.

## Checks

Commit readiness requires meeting the current stage's acceptance criteria, a passing debug build, all unit tests and lint, relevant instrumentation checks, successful rendering of every Compose preview in Android Studio, and emulator verification of changed flows. Build success does not establish preview correctness. Report incomplete verification explicitly and resolve failures before presenting the work as ready to commit; exceptions require explicit approval.

Use pure date tests with a controlled current date for newborn, month-end, leap-day and first-birthday cases. Use injected theme selection for deterministic tests. Run debug assembly, focused tests and lint as relevant, then inspect device behavior and screenshots. Full-screen reference exports and final background crop comparisons remain outstanding; current assets and recorded measurements support initial implementation.

## Version control

Create an initial foundation commit, then focused feature commits. Add annotated stage-1 through stage-5 tags only when their acceptance criteria pass. Keep machine-specific files and temporary outputs excluded. Repository publication and visibility are configured separately from implementation.

## Review workflow

Propose commit title/body before creating commits. After approval, push the feature branch and open a draft pull request for review in Files changed. Merge only after approval. A stage tag marks the latest approved stage checkpoint; update it only when approved stage changes are merged. Do not tag an unreviewed feature branch as complete.

## Deliverable checkpoints

- `stage-1`: final accepted details-form checkpoint (`3361865`).
- `stage-2`: final accepted persistence checkpoint, including all stage 2 refinements (`3a4ed08`).
- `stage-3`: final accepted birthday presentation checkpoint (`3f5e8ac`), including themes, age artwork, annotated layout and edge-to-edge rendering.
- `stage-4` and `stage-5`: created only after their respective requirements and verification are complete.

Before final submission, verify each annotated stage tag exists on origin and resolves to the final accepted commit for that stage. Intermediate PR merges do not mark an assignment stage complete.

## Current theme checkpoint

The three theme/asset mappings and random selection on each birthday entry are implemented locally. Background illustrations retain their aspect ratio, bottom anchor, and recorded side overflow. Photo and placeholder share the theme border. Per-theme previews and emulator captures are included. Age artwork and heading/photo/logo layout are now implemented; final acceptance is tracked below.

## Header checkpoint

The birthday header renders dynamic uppercase name text, supplied number artwork and pluralized month/year labels. Age uses completed calendar anniversaries, clamped to the last available day for month-end and leap-day birthdays; newborns show zero months. Age boundary tests cover these decisions. Photo/logo positioning and responsive layouts are implemented for final review.

## Stage 3 review checkpoint

Includes dynamic completed calendar age, supplied number/swirl artwork, long-name handling, all three themes, photo/logo layout following annotated spacing, and edge-to-edge background with safely inset controls. Build, age-boundary unit tests, lint, emulator navigation/recreation and theme rendering checks are run before review. PR #6 is accepted and merged; stage-3 points to its final merge commit after preview confirmation.

## Stage 4 local implementation

BirthdayRoute reuses the gallery/camera launchers and shared profile ViewModel. The selected photo updates both destinations and persists through the existing repository. The themed 36 dp badge has a 48 dp touch target centered at 45 degrees on the 7 dp ring centerline. Photo imports disable repeated selection until finished; cancellation preserves the photo and theme. Picker/import errors use dismissible snackbar feedback. Acceptance checks cover real system gallery selection, camera capture/cancellation, invalid-photo feedback, persistent copied files, shared photo state, theme preservation and rendering previews.

## Stage visual evidence gate

Before publishing a stage tag, verify the final checkpoint PR contains screenshots of the delivered UI and, when navigation changes, a short video or screenshot sequence showing the flow. Media must be embedded attachments or linked PR-comment attachments that reviewers can open; relevant earlier PR evidence may be linked. Keep recordings brief and prefer synthetic/placeholder images. A local-only output path does not satisfy this gate. The pull request template records this check alongside validation and acceptance.
