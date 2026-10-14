# Design assets

Reference: https://www.figma.com/design/YQObKTFziOt9xIR5ySXqIf/Dev-Home-assignment?node-id=1-201&p=f

27 supplied originals are preserved under docs/design-assets/. AssetManifest.json maps sources and SHA-256 checksums to Android resources. PNG backgrounds live in drawable-nodpi; compatible SVGs are converted to VectorDrawable XML in drawable.

## Theme mapping

| Theme | Background | Placeholder fill | Border/accent | Illustration source |
|---|---|---|---|---|
| Fox | #C5E8DF | #A9DCCF | #6FC5AF | Fox/android_foxxxxhdpi (1).png |
| Elephant | #FEEFCB | #FEE7B7 | #FEBE21 | Elephant/BG_Android_Elephant.png |
| Pelican | #DAF1F6 | #B9E5EF | #8BD3E4 | Pelican/BG_Android_Pelican.png |

Use filled placeholders without an outer photo ring. Their internal face outlines are part of the artwork. Draw the outer ring separately in Compose for both placeholder and selected photo. Camera badges remain independent overlays.

Elephant/Group 49.svg is the complete camera badge. The legacy circle-only Elephant badge resource is retained; its original bg.svg is absent from the current source folder and replacement ZIP. Do not use it as the complete camera button. The Pelican placeholder retains the misleading source name Default_place_holder_green.svg; identify it by its theme folder.

Preserve PNG transparency, canvas bounds and aspect ratio. Number assets 0-12 have different canvas sizes; avoid stretching them into equal-sized boxes. See AssetInspection.md for conversion checks and FigmaMeasurements.md for reference geometry.

Nanit artwork is proprietary and restricted to this assignment.

## Shared controls

The refreshed ZIP adds `Shared/Union.svg` (back arrow, 18 × 15, #394562) and `Shared/ic_share.svg` (white share arrow, 24 × 24). All 25 overlapping files match the existing originals byte-for-byte. Both additions are preserved unchanged and converted to `birthday_back.xml` and `birthday_share.xml`.

The back export contains the inner artwork rather than its original 24 × 24 Figma component. Render its 18 × 15 canvas proportionally inside a 24 × 24 icon slot and a touch target of at least 48 dp. The share export uses a 24 × 24 canvas rather than the previously inspected transformed group bounds. Preserve its exported aspect ratio and validate button spacing during stage 5. The pill background and label remain native Compose UI, using the recorded iOS styling.
