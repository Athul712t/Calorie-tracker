# Design tokens

Implemented in `app/src/main/java/com/example/ui/theme/AppTokens.kt` and `Type.kt`.

## Typeface
DM Sans only (Regular 400, Medium 500, SemiBold 600, Bold 700). Never use another font.

## Colours
| Token | Hex | Use |
|---|---|---|
| Background | #F7F8FA | Screen background |
| Surface | #FFFFFF | Cards |
| Border | #ECEEF2 | Card borders |
| BorderStrong | #E3E7EC | Inputs, outlined buttons |
| Track | #E9ECF1 | Empty part of rings and bars |
| Chip | #F1F3F6 | Round icon buttons, chips |
| Segment | #ECEFF3 | Segmented-control background |
| Ink | #14181F | Primary text, primary buttons |
| InkSoft | #3A4352 | Secondary text |
| Muted | #5F6B7A | Captions, labels |
| Ghost | #8A94A3 | Placeholders, disabled |
| Green | #22A672 | Progress, on target, positive |
| GreenDeep / GreenTint | #14784C / #E8F6EF | Green text and chip background |
| Blue | #3B82F6 | Focus ring, chat send |
| Amber | #D97706 | Over target (text #8A4F05, tint #FDF3E3) |

Macros: Protein #3B6FE8 (deep #2548B8, tint #EEF3FE), Carbs #D97706 (deep #8A4F05, tint #FDF3E3), Fat #8B5CF6 (deep #6D3BD2, tint #F3EEFE).

## Shape and spacing
Screen gutter 20 dp. Card radius 24 dp (28 dp for hero cards). Bottom sheet top radius 32 dp. Primary button 56 dp high, pill shaped. Input rows 60 dp high, radius 22 dp. Round icon buttons 44 dp (40 dp in cards).

## Rules
- Light theme only.
- Floating bottom bars sit over a gradient from transparent to the background colour.
- Icon-only buttons need a content description.
- Colour is never the only signal: pair it with text.
