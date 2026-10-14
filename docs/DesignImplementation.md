# Birthday screen design

Behavior follows Requirements.md. Measured values are in FigmaMeasurements.md; decisions below fill gaps in the visual specification.

## Components

BirthdayScreen contains a solid theme background, illustration, AgeHeading, BabyImage, Nanit logo and independent close/camera/share controls. A reusable BirthdayCard renders content without interactive controls for sharing. Name and age text remain dynamic.

Select a theme once on screen entry and retain it during recomposition and photo updates. Theme definitions map colors and resources from DesignAssets.md.

## Typography and controls

Figma reports BentonSans medium, title 21 design px and age label 18 design px. Use FontFamily.SansSerif with medium headings unless a licensed font is supplied; font substitution is permitted. Use sp and support font scaling.

- Back/close: use the supplied iOS left-arrow artwork (18 × 15 export inside a 24 × 24 icon slot), #394562, at top-left within system insets; proposed 16 dp edge margin and at least 48 dp touch target. Exact Android placement is an implementation decision.
- Share the news: use the inspected iOS coral #EF7B7B pill (179 × 42 reference, radius 21), white 16 px text and exported share-arrow artwork. Center below the baby image and logo over the illustration. Adapt to an Android touch target of at least 48 dp and tune placement during visual review.
- Camera: center its badge on the circle border at 45 degrees; separate visual size from accessible touch target.

## Baby image

Show the filled theme placeholder when no photo exists; otherwise replace it with the selected photo, automatically center-cropped and clipped to a circle. Draw the same separate theme border in both states. The reference ring reports a 7 px border and 78% ring opacity; never apply that opacity to the photo.

## Background and responsive layout

Scale the illustration uniformly, center horizontally and anchor to the bottom while covering the content width. Preserve transparent margins and clip overflow at content bounds. The original PNGs are 1083 x 1776. Figma image layers report 459 x 752 with left -49 in a 360-wide frame; this does not establish the original image-fill crop. Compare visible landmarks before applying any additional crop.

Center photo and logo horizontally. Center the age section between the top system inset and photo circle. Use 360 x 800 as the reference baseline, not physical device coordinates. Respect actual system insets; do not render status/nav mockups. Adapt spacing or scrolling on constrained screens so controls do not obscure essential content.

## Visual checks

Compare all three themes, selected-photo state, long names and multi-digit ages. Preserve the recorded 8 px Pelican photo-offset difference until reference comparison resolves it. Close/share dimensions are implementation choices, not measured Figma values. Full-screen reference exports and final on-device comparison remain outstanding.
