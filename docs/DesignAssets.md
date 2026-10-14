# Design assets

Reference: https://www.figma.com/design/YQObKTFziOt9xIR5ySXqIf/Dev-Home-assignment?node-id=1-201&p=f

26 supplied originals are preserved under docs/design-assets/. AssetManifest.json maps sources and SHA-256 checksums to Android resources. PNG backgrounds live in drawable-nodpi; compatible SVGs are converted to VectorDrawable XML in drawable.

## Theme mapping

| Theme | Background | Placeholder fill | Border/accent | Illustration source |
|---|---|---|---|---|
| Fox | #C5E8DF | #A9DCCF | #6FC5AF | Fox/android_foxxxxhdpi (1).png |
| Elephant | #FEEFCB | #FEE7B7 | #FEBE21 | Elephant/BG_Android_Elephant.png |
| Pelican | #DAF1F6 | #B9E5EF | #8BD3E4 | Pelican/BG_Android_Pelican.png |

Use filled placeholders without an outer photo ring. Their internal face outlines are part of the artwork. Draw the outer ring separately in Compose for both placeholder and selected photo. Camera badges remain independent overlays.

Elephant/Group 49.svg is the complete camera badge. Elephant/bg.svg is the earlier circle-only export retained for source preservation; do not use it as the complete camera button. The Pelican placeholder retains the misleading source name Default_place_holder_green.svg; identify it by its theme folder.

Preserve PNG transparency, canvas bounds and aspect ratio. Number assets 0-12 have different canvas sizes; avoid stretching them into equal-sized boxes. See AssetInspection.md for conversion checks and FigmaMeasurements.md for reference geometry.

Nanit artwork is proprietary and restricted to this assignment.
