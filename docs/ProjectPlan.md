# Implementation plan

Current status: stage 1 accepted after build, three validation tests, lint and emulator review. The stage-1 tag marks this checkpoint. Stage 2 is accepted and merged; stage-2 points to its final merge commit. Stage 3 navigation is implemented; the remaining stage 3 subtasks and stages 4-5 are pending. Follow the stages in order and validate each before creating its tag.

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
- `stage-3`: not created yet. Navigation and component refactoring are intermediate work; themes, random selection, age artwork, final design, and acceptance verification must be complete first.
- `stage-4` and `stage-5`: created only after their respective requirements and verification are complete.

Before final submission, verify each annotated stage tag exists on origin and resolves to the final accepted commit for that stage. Intermediate PR merges do not mark an assignment stage complete.

## Current theme checkpoint

The three theme/asset mappings and random selection on each birthday entry are implemented locally. Background illustrations retain their aspect ratio, bottom anchor, and recorded side overflow. Photo and placeholder share the theme border. Per-theme previews and emulator captures are included. Age artwork, final heading/photo/logo placement, and full responsive acceptance remain separate stage 3 work.
