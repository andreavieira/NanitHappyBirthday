# Birthday screen design

Behavior follows Requirements.md. Measured values are in FigmaMeasurements.md; decisions below fill gaps in the visual specification.

## Components

BirthdayScreen contains a solid theme background, illustration, AgeHeading, BabyImage, Nanit logo and independent close/camera/share controls. A reusable BirthdayCard renders content without interactive controls for sharing. Name and age text remain dynamic.

Select a theme once on screen entry and retain it during recomposition and photo updates. Theme definitions map colors and resources from DesignAssets.md.

## Typography and controls

Figma reports BentonSans medium, title 21 design px and age label 18 design px. Use FontFamily.SansSerif with medium headings unless a licensed font is supplied; font substitution is permitted. Use sp and support font scaling.

- Close: top-left within system insets; proposed 24 dp glyph, 16 dp edge margin and at least 48 dp touch target. Dark text color #394562.
- Share the news: horizontally centered below the baby image and logo, over the illustration; use theme-compatible rounded styling and readable contrast. Exact spacing is tuned during visual review.
- Camera: center its badge on the circle border at 45 degrees; separate visual size from accessible touch target.

## Baby image

Show the filled theme placeholder when no photo exists; otherwise replace it with the selected photo, automatically center-cropped and clipped to a circle. Draw the same separate theme border in both states. The reference ring reports a 7 px border and 78% ring opacity; never apply that opacity to the photo.

## Background and responsive layout

Scale the illustration uniformly, center horizontally and anchor to the bottom while covering the content width. Preserve transparent margins and clip overflow at content bounds. The original PNGs are 1083 x 1776. Figma image layers report 459 x 752 with left -49 in a 360-wide frame; this does not establish the original image-fill crop. Compare visible landmarks before applying any additional crop.

Center photo and logo horizontally. Center the age section between the top system inset and photo circle. Use 360 x 800 as the reference baseline, not physical device coordinates. Respect actual system insets; do not render status/nav mockups. Adapt spacing or scrolling on constrained screens so controls do not obscure essential content.

## Visual checks

Compare all three themes, selected-photo state, long names and multi-digit ages. Preserve the recorded 8 px Pelican photo-offset difference until reference comparison resolves it. Close/share dimensions are implementation choices, not measured Figma values. Full-screen reference exports and final on-device comparison remain outstanding.
