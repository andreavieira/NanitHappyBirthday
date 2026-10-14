# Asset inspection

27 originals are preserved under docs/design-assets/, excluding macOS metadata. Android resources comprise 3 PNG backgrounds and 25 vectors. AssetManifest.json records source mappings and original checksums.

- Backgrounds: RGBA 1083 x 1776. Alpha bounds: Fox (0,131)-(1083,1776), Elephant (0,120)-(1083,1776), Pelican (0,290)-(1083,1776). Preserve transparency and canvas bounds.
- Placeholders: 200 x 200 SVG canvases, filled background and face artwork, no outer photo ring.
- Camera badges: 36 x 36. Elephant/Group 49.svg is the corrected complete export; the earlier circle-only Android resource is retained, although bg.svg is absent from the current original-assets directory. The native badge paths/colors match the complete export.
- Numbers: 0-12 with varying canvas sizes. Swirls: 52 x 45. Logo: 59 x 20.
- Logo conversion: flattened a solid rectangle under a white shape mask into its equivalent #394562 path. Other conversions preserve paths, fills, strokes, caps, joins and fill rules.

Conversion geometry was checked by reconstructing SVGs from native vector paths and comparing 4x renders: 21 initial SVGs matched exactly; the logo differed only at antialiased edges (maximum 3/255 per channel). This is not Android runtime visual validation.

Debug assembly and lint passed after import. Device visual comparison is pending. Full-screen references and font files are not included.

Refreshed ZIP consolidation: 25 originals matched exactly; added only the 18 × 15 back-arrow SVG and 24 × 24 share-arrow SVG. No existing originals or runtime artwork were overwritten. Both new vectors preserve the exported path geometry, fill rule, fill color, and canvas dimensions.
