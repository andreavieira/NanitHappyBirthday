# Birthday screen design

Behavior follows Requirements.md. Measured values are in FigmaMeasurements.md; decisions below fill gaps in the visual specification.

## Components

BirthdayScreen contains a solid theme background, illustration, AgeHeading, BabyImage, Nanit logo and independent close/camera/share controls. BirthdayArtwork renders content without interactive controls for sharing. Name and age text remain dynamic.

Select a theme once on screen entry and retain it during recomposition and photo updates. Theme definitions map colors and resources from DesignAssets.md.

## Typography and controls

Figma reports BentonSans medium, title 21 design px and age label 18 design px. Use FontFamily.SansSerif with medium headings unless a licensed font is supplied; font substitution is permitted. Use sp and support font scaling.

- Back/close: use the supplied iOS left-arrow artwork (18 × 15 export inside a 24 × 24 icon slot), #394562, at top-left within system insets; 24 dp icon slot starts 16 dp from the safe content edges, inside a 48 dp touch target starting at 4 dp. The 18 × 15 export sits at x=3/y=5 in its 24 × 24 slot to preserve measured Figma padding. The app-level Scaffold supplies system-bar insets. Android placement is an implementation decision.
- Share the news: use the inspected iOS coral #EF7B7B pill (179 × 42 reference, radius 21), white text translated to Android 16 sp, regular weight (400), zero letter spacing, a 22 sp line height and exported share-arrow artwork. The supplied iOS text layer is 116 × 22 design px with a 16 px BentonSans font; Android font substitution remains permitted, and text width is content-driven. Center below the baby image and logo over the illustration. Adapt to an Android touch target of at least 48 dp and tune placement during visual review.
- Camera: center its badge on the circle border at 45 degrees; separate visual size from accessible touch target.

## Baby image

Show the filled theme placeholder when no photo exists; otherwise replace it with the selected photo, automatically center-cropped and clipped to a circle. Draw the same separate theme border in both states. The reference ring reports a 7 px border and 78% ring opacity; never apply that opacity to the photo.

## Background and responsive layout

Scale the illustration uniformly, center horizontally and anchor to the bottom while covering the content width. Preserve transparent margins and clip overflow at content bounds. The original PNGs are 1083 x 1776. Figma image layers report 459 x 752 with left -49 in a 360-wide frame; this does not establish the original image-fill crop. Compare visible landmarks before applying any additional crop.

Center photo and logo horizontally. Center the age section between the top system inset and photo circle. Use 360 x 800 as the reference baseline, not physical device coordinates. Respect actual system insets; do not render status/nav mockups. Adapt spacing or scrolling on constrained screens so controls do not obscure essential content.

## Visual checks

Compare all three themes, selected-photo state, long names and multi-digit ages. Preserve the recorded 8 dp Pelican photo offset; the share-button adjustment does not move the photo, logo or heading. Close/share dimensions are implementation choices, not measured Figma values. Full-screen reference exports and final on-device comparison remain outstanding.

## Photo/logo layout implementation

Preserve the Stage 3 photo/header/logo layout: at the 360 × 800 baseline with 24/48 system insets, photo top is 242 dp within safe content (266 dp in the reference), or 234 dp for Pelican. On taller screens it follows the vertical center; constrained layouts retain header clearance and scroll. The photo stays 200 dp, shrinking to preserve minimum 50 dp side clearance. The centered logo is 58.68 × 19.63 dp, 15 dp below the photo. Only Share is bottom-anchored: 53 dp above the safe bottom, with at least 53 dp below the logo. That gap expands on taller screens without moving the photo/header/logo. The button retains at least a 48 dp touch target, and its measured height is included in layout.

## Annotated spacing rules

The supplied annotated Pelican reference takes precedence over inferred spacing: center the complete age section vertically between the status inset and photo, with at least 20 dp above and 15 dp below. Use 13 dp between title and number, 14 dp between number and age label, and 22 dp from number to each swirl. Preserve the photo’s minimum 50 dp side clearance, horizontal centering of photo/logo, 15 dp photo-to-logo gap, and bottom/side anchored aspect-preserving illustration. These are minimum clearances rather than a fixed age-label-to-photo gap. The camera badge is centered at 45 degrees on the photo border centerline.

## System insets

Enable Android edge-to-edge on all supported versions. Render the birthday illustration across the entire window; apply Scaffold safeDrawing padding only to birthday content and controls. The safe region accounts for system bars and display cutouts. Keep platform navigation-bar protection for three-button navigation. The back control uses a 48 dp target at 4 dp from the safe top/start edges, centering the original 24 dp asset slot at 16 dp; the exported glyph retains its internal 3/5 dp offset.

## Sharing implementation

The share control sits at least 53 dp below the logo and 53 dp above the safe bottom edge, with a minimum 48 dp touch target. During export preparation it retains its coral fill while disabled. The exported PNG includes the current theme, heading, age, photo and logo without controls or system bars. On constrained screens, content scrolls and the export retains the full unscrolled card.
