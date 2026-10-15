# NanitHappyBirthday project plan

## Requirements and completion

Build the five stages in order. An annotated `stage-1` through `stage-5` tag marks each final accepted checkpoint; intermediate commits and PRs do not mark completion.

| Stage | Required behavior | Acceptance |
|---|---|---|
| 1: Details | App title, name, birthday, picture and Show birthday screen action. | Fresh profiles are empty; continuation requires a nonblank name and a birthday. |
| 2: Persistence | Edit name/date; choose a gallery image or take a photo; persist all three across launches. | Details save without opening the card; selected photos remain readable after relaunch; cancellation/failure preserves the prior photo. |
| 3: Birthday | Supplied artwork, a random theme on each entry, correct months/years, a two-line long name and top-left return control. | Check all three themes, calendar boundaries, long names, Back/recreation and constrained layouts. |
| 4: Photo update | A themed camera badge opens gallery/camera selection and replaces the placeholder. | Both destinations reflect the persisted photo; updating it preserves the current theme. |
| 5: Sharing | Share the news opens Android Sharesheet with an image excluding back, camera and Share controls. | PNG contains the current card, including content below a short viewport, without system bars; recipients receive temporary read access. |

All five stages are implemented. Annotated stage tags identify accepted deliverable checkpoints. Verify the final accepted source is on `main` and each tag exists remotely before submission.

The following are project decisions where the assignment does not specify an edge case:

- Show completed months below one year, then completed years; newborns show 0 months. Calendar anniversaries clamp to the month's last day, including February 29 in non-leap years.
- Reject future birthdays at the picker and repository write boundary. Persist incomplete details drafts; require valid name/date for continuation.
- Center-crop photos automatically. Use a filled baby placeholder when no photo exists, with the same separate theme border in either state.
- Choose the theme independently on each card entry; consecutive repeats are allowed. Preserve the choice through recomposition, recreation and photo changes.
- Save name/date edits automatically and flush before continuation. Save failure reports a snackbar and permits viewing the in-memory card. Photo failure retains the previous photo; load failure offers Retry.

## Architecture and responsibilities

Keep one activity and app module. Use constructor injection and immutable UI state; add dependencies or abstractions only for a concrete need.

| Component | Responsibility |
|---|---|
| `MainActivity` / `NanitApp` | Host Compose, edge-to-edge rendering, the two-screen destination, stable birthday snapshot/theme and app-level feedback. |
| `DetailsRoute` / `BirthdayRoute` | Collect state with lifecycle awareness and coordinate platform actions, dialogs and feedback. |
| `DetailsScreen` / `BirthdayScreen` | Render explicit state and callbacks as meaningful UI sections. Keep platform operations out of rendering functions. |
| `DetailsViewModel` | Load the profile, expose StateFlow, automatically save details, serialize photo operations and flush before continuation. Shared across both destinations. |
| `ProfileRepository` / `DataStoreProfileStore` | Validate writes and persist metadata atomically while preserving unrelated fields. |
| `PrivateProfilePhotos` | Copy and validate images in private storage on the IO dispatcher; enforce the 20 MiB limit and clean replaced/failed files. |
| `PhotoSelectionFlow` | Reuse gallery/camera selection on both routes. Keep launchers registered while the source dialog is hidden so external results can return. |
| `CameraCaptureFiles` / `rememberCameraPhotoCapture` | Own temporary camera files and FileProvider URIs; retain pending capture state across recreation and clean up after import/cancellation. |
| `BirthdayShareImageRenderer` / `BirthdayShareAction` | Record an invisible full-height copy of the shared artwork, wait for image readiness, capture/crop the bitmap and open sharing. Interactive controls stay outside the recorded tree. |
| `BirthdaySharing` | Write unique PNGs into a dedicated cache, remove exports older than a day on the next share, and grant temporary read access through FileProvider. |

Name/date writes use a conflated channel so rapid edits coalesce while disk writes are busy. A mutex orders autosaves with the continuation flush; each write reads the latest draft after acquiring the lock. Autosaving does not disable typing. Photo operations use a separate sequential queue and update only photo metadata. Completed writes survive relaunch; a process termination before a write completes cannot guarantee persistence.

Keep platform Contexts and activity-result launchers outside ViewModels. File helpers retain only application Context. Share preparation runs in the route's composition scope, prevents duplicate taps and releases captured bitmaps after use. The app uses system image selection and external camera capture without broad gallery/camera permissions.

## Design reference and implementation

[Figma reference](https://www.figma.com/design/YQObKTFziOt9xIR5ySXqIf/Dev-Home-assignment?node-id=1-201&p=f). Android reference frames are 360 × 800 design pixels, not physical device pixels. Treat the following as reference measurements interpreted in dp/sp at that baseline, then apply responsive rules and actual system insets.

| Theme | Background | Placeholder fill | Border/accent |
|---|---|---|---|
| Fox | `#C5E8DF` | `#A9DCCF` | `#6FC5AF` |
| Elephant | `#FEEFCB` | `#FEE7B7` | `#FEBE21` |
| Pelican | `#DAF1F6` | `#B9E5EF` | `#8BD3E4` |

`BirthdayTheme` maps colors and resources; `NanitColors` holds the palette. `NanitDimens` holds shared sizes; `BirthdayDimens` and `DetailsDimens` hold feature measurements. Preserve original asset geometry and transparency. `AssetManifest.json` maps original checksums to runtime resources; supplied originals remain under `docs/design-assets/`. PNG backgrounds use drawable-nodpi, and compatible SVGs use VectorDrawable XML. Nanit's artwork is assignment-only.

### Measured birthday artwork and spacing

- Photo: 200 × 200, centered horizontally; reference top 266 for Fox/Elephant and 258 for Pelican. Ring width 7 and opacity 78%; apply opacity to the ring, not the photo. Shrink the circle when necessary to retain at least 50 dp side clearance.
- Logo: 58.68 × 19.63, centered 15 below the photo.
- Heading: name 21 with weight 500 and letter spacing -0.42; age label 18 with weight 500 and letter spacing -0.36; foreground `#394562`. Center the complete age section between the safe top and photo, with minimum top/bottom clearance. Keep 13 between title/number, 14 between number/age label and 22 between number/swirl.
- Number artwork: about 88 high; preserve each digit's proportions. Use supplied compound numbers through 12, composing digits for larger values. Swirls are 50.2 × 43.53 in the inspected screen.
- Camera: 36 visual badge centered at 45 degrees on the ring centerline, with a separate 48 dp touch target.
- Background: original PNG canvas 1083 × 1776; inspected Figma layer 459 × 752 at left -49 inside a 360-wide frame. Preserve aspect ratio, center horizontally, anchor to the bottom and clip side overflow. Layer geometry does not prove the original image-fill crop.

The annotation-based Android layout centers photo/logo horizontally and preserves Pelican's 8 dp offset. At the reference 24/48 system insets, photo top is 242 within safe content for Fox/Elephant and 234 for Pelican. Taller screens follow the vertical-center offset; constrained screens scroll. The heading uses a 23 sp line height and up to two lines with ellipsis. Font-family substitution is permitted; the app uses Android sans-serif instead of BentonSans.

### Android control adaptations

The supplied back/share controls came from the iOS design; their Android placements are implementation decisions rather than measured Android coordinates.

- Back: preserve the 18 × 15 glyph at offset 3/5 inside a 24 × 24 icon slot. Its 48 dp target starts 4 dp from safe top/start edges.
- Share: coral `#EF7B7B`, white 16 sp regular label, 22 sp line height, zero letter spacing, radius 21 and minimum width 179 dp. Keep a minimum 48 dp target; place it 53 dp above the safe bottom and at least 53 dp below the logo. Preserve photo/header/logo positions when adjusting Share. Retain the coral fill during preparation.
- Details action: match Share's text size and safe-bottom spacing, while keeping the full-width details styling. On constrained heights or with the keyboard open, follow the scrollable form with at least 20 dp separation.
- Background extends behind system bars; apply safeDrawing insets to foreground content and controls. Keep system navigation protection for three-button navigation. Do not draw Figma's mock status/navigation bars.

The final supplied colors are intentional. White on coral has limited text contrast; improving it would require a design decision. Do not silently restyle it during cleanup.

## Validation and review

Build/run commands are in the README. Unit tests cover calendar boundaries, theme selection, repository updates, UI availability and ViewModel persistence/failures. Emulator tests cover navigation/recreation, unsubmitted details persistence, gallery/camera flows, storage recreation, theme rendering and sharing.

Use `scripts/RunEmulatorTests.py` for replacement installs and instrumentation; do not uninstall or clear an emulator containing review data. Tests that touch the profile restore it in finally; storage tests use isolated files. Keep generated media, personal photos, credentials and local configuration outside Git.

Before accepting changes, run formatting checks, the debug build, unit tests, lint and relevant emulator tests. Inspect changed Compose previews and layouts; build success alone does not verify rendering. For visible changes, attach concise screenshots; record only when motion or a flow materially helps review. Report incomplete checks explicitly.

Remaining visual verification: inspect every birthday/details preview, including overlay controls, long names and small-screen/large-text states; compare matched-size runtime screenshots with full-frame Figma references, especially background landmarks/cropping. Exact crop equivalence has not been established from individual asset exports alone.

Use focused PRs. Several related commits may belong in one PR. Prefer publishing independent completion PRs sequentially against main; if stacking, retarget the next PR after its predecessor merges. Create stage tags only after acceptance, merge and required verification.
