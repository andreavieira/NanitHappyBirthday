# Figma measurements for verification

Reference measurements from the Android page. Source: https://www.figma.com/design/YQObKTFziOt9xIR5ySXqIf/Dev-Home-assignment . Assets have been imported; these values are reference measurements. Final visual comparison is pending.

## Theme mapping

| Theme | Frame background | Illustration layer | Placeholder layer | Placeholder fill | Photo border |
|---|---|---|---|---|---|
| Fox | #C5E8DF | android_foxxxxhdpi (1) | Default_place_holder_green | #A9DCCF | #6FC5AF |
| Elephant | #FEEFCB | BG_Android_Elephant | Default_place_holder_yellow Copy | #FEE7B7 | #FEBE21 |
| Pelican | #DAF1F6 | BG_Android_Pelican | Default_place_holder_green (contains Lily_face and Fill 1) | #B9E5EF (Fill 1) | #8BD3E4 |

Beware: the blue Pelican placeholder is also named Default_place_holder_green. Identify by parent frame and visual appearance, not filename alone. Colors above are read from selected frame/layer Properties, not sampled from screenshots.

## Coordinate convention

All values below are Figma design pixels, as displayed in the viewer's Width, Height, Top, Left fields. The three inspected theme frames are 360 x 800. Direct child positions are frame-relative; nested Fox photo/camera children were also displayed as frame offsets. Do not treat these as physical device pixels. Match the reference baseline, then apply the explicit anchoring notes for other screen sizes and system insets.

## Shared observed bounds

| Element | Left | Top | Width | Height |
|---|---:|---:|---:|---:|
| Name/title text block | 54 | 44 | 252 | 58 |
| Sample number 1 (1 copy 2) | 157 | 116.26 | 50.44 | 87.95 |
| Age unit text (MONTH OLD!) | 123 | 219 | 114 | 25 |
| Left swirls | 85 | 141.22 | 50.2 | 43.53 |
| Right swirls | 230 | 141 | 50.2 | 43.53 |
| Nanit logo | 150.41 | 481 | 58.68 | 19.63 |

These bounds were individually observed in Fox, Elephant, and the first Pelican reference. The sample title content reads TODAY CRISTIANO RONALDO IS although its layer is named TODAY CHRISTIANA IS. Text content is dynamic, not an image asset.

Typography: title 21 px, weight 500, centered, letter spacing -0.42 px; age unit 18 px, weight 500, centered, letter spacing -0.36 px. Both display BentonSans and line height 100%; assignment permits ignoring font types. Text and swirls use #394562; swirl border is 1 px. Bounds for other digits/multi-digit ages have not been collected.

## Theme-specific photo and camera bounds

| Theme / element | Left | Top | Width | Height |
|---|---:|---:|---:|---:|
| Fox photo ring (Oval) | 80 | 266 | 200 | 200 |
| Fox placeholder | 81 | 266 | 199.17 | 199.17 |
| Fox photo group (Group 72) | 80 | 266 | 200.17 | 200 |
| Fox camera group (Group 49) | 242 | 283 | 36 | 36 |
| Elephant photo ring (Oval) | 80 | 266 | 200 | 200 |
| Elephant placeholder | 80 | 266 | 199.17 | 199.17 |
| Elephant camera group (Group 49) | 241 | 283 | 36 | 36 |
| Pelican photo ring (Oval) | 80 | 258 | 200 | 200 |
| Pelican placeholder outer group | 81 | 258 | 201 | 201 |
| Pelican placeholder Fill 1 | 81 | 258 | 199.17 | 199.17 |
| Pelican Lily_face | 81.44 | 258.44 | 200.56 | 200.56 |
| Pelican camera group / bg | 241 | 275 | 36 | 36 |
| Pelican white camera glyph | 250 | 283 | 23 | 21.65 |

Each inspected photo ring reports a 7 px border and 78% opacity. This applies to the selected ring, not the entire photo. Pelican camera bg is #8BD3E4 and its glyph #FFFFFF. Fox/Elephant camera child colors have not been independently inspected. Camera groups were identified by the shared Group 49 structure and visible icon; Pelican children explicitly include ic_photo_white_Plus and bg.

The 8 px Pelican vertical difference and 1 px Fox/Elephant horizontal differences are real observed reference differences. Do not silently normalize them; discuss whether the annotated anchoring rules should take precedence.

## Background illustration geometry

Each inspected illustration layer reports Width 459, Height 752, Left -49 and image Scale Crop. Top was not displayed; do not claim it was verified as zero. The reference frame width is 360, so the layer extends beyond its visible sides. This is layer geometry, not the original bitmap's pixel resolution. Use the uploaded source asset dimensions when calculating aspect ratio.

Fox also contains a layer named android_pelican_popup@4x reporting 360 x 592, without displayed Top/Left. Its visibility and role are unverified; do not export/use it as the Fox background just because it is present. Similar extra layers exist in other frames.

Fox status-bar mockup reports 360 x 24, with #25304A at 20%; navbar mockup reports 360 x 48, Top 752, with black/white colors. These are system-UI references, not assets to draw into the app.

## Explicit designer annotations

- Center age text section vertically in the space between status bar and the baby image circle.
- Illustration BG: keep aspect ratio and pin to bottom and sides.
- Pin baby image and Nanit logo to the center. The note does not explicitly distinguish horizontal from vertical centering; the visual references show horizontal centering.
- Bonus: position the camera icon so its center is on the baby image border line at a 45-degree angle.

Apply these layout rules alongside the reference coordinates.

## iOS controls inspected for Android reuse

The iOS page supplies shared controls absent from the inspected Android frames. These are iOS reference measurements, not verified Android placements:

- Back asset: Anniversary_Assets → `ic_utility_24/ic_back` (node 581:191), 24 × 24. Its Union artwork is 17.75 × 15 at left 3, top 5, filled #394562. It is a left arrow, not an X. Export the complete 24 × 24 asset as SVG to preserve its internal padding; wrap it in an Android touch target of at least 48 dp.
- Fox share group: `Group 7` (node 579:341), 179 × 42, left 98/top 569 in a 375 × 667 frame. Background `Rectangle` (579:342) is #EF7B7B with radius 21. Recreate the background and text as a native button rather than exporting the whole control.
- Share text: `Share the news` (579:347), BentonSans 400, 16 px, line height 100%, letter spacing 0, white. Assignment permits font substitution.
- Button arrow: `ic_share_pink_small` (579:344), 30.28 × 33.64 group bounds, left 116/top 0.19 within the 147 × 35 label/icon frame. Export the complete arrow group as SVG; its group bounds include internal spacing and should not be mistaken for the visible glyph bounds.
- Actual back-button placement within an iOS screen was not found. Share styling was inspected in Fox; appearance in Elephant/Pelican looked consistent but their properties were not independently measured. Use Android anchoring rules rather than copying iOS screen coordinates verbatim.

## Unresolved / remaining verification

- Close/back and share placements remain unverified in the Android frames. iOS control styling is recorded above; do not present iOS positions as Android measurements.
- Duplicate Pelican frames exist (including photo and annotated examples). The theme measurements above came from the first Pelican frame in the tree after Elephant. Alternate-frame measurements remain unverified.
- Imported backgrounds are 1083 x 1776 RGBA; final scaling/crop needs comparison with the reference.
- Need to confirm hidden extra bitmap layers and the unnamed Elephant/Pelican child layer are not required visible assets.
- Exact behavior across small/large screens requires implementation and visual comparison; no claim of exact fidelity yet.

## Upload naming

Use Fox/, Elephant/, Pelican/ for Background.png, BabyPlaceholder.svg (or PNG when bitmap), and Camera.svg where theme-specific. Shared/ can hold Logo.svg, LeftSwirl.svg, RightSwirl.svg and Numbers/. References/ can hold full screen and annotated PNGs. Preserve original names if easier; keep the parent theme folder to disambiguate misleading names. Do not export dynamic title/age text as app assets.

## Placeholder rendering

Use the filled baby-face placeholder without the outer photo border. Draw the border separately in Compose for BOTH placeholder and selected-photo states, using the theme border color in the mapping above. Placeholder fill belongs to the default graphic only; a selected photo replaces it. Keep camera separate. 

### Supplied control exports

The refreshed ZIP contains `Shared/Union.svg` (18 × 15 back glyph, #394562) and `Shared/ic_share.svg` (24 × 24 white share glyph). These exported canvases differ from the parent component/group measurements above. Preserve source geometry and place each within its native Compose button; no re-export is required.
