# Design reference

`reference/` holds the screen designs (one `.dc.html` file per screen) and `canvas.json`, which lays them out on the design canvas.

These files are the source of truth for the look of the app. They are **not** runnable on their own: they use the Claude Design runtime. Read them as specs:

- Exact values (sizes, padding, radii, colours, font sizes and weights) are in the inline `style="..."` attributes.
- Interactions and sample data are in the `<script>` block at the bottom of each file.
- Links between screens are the `href="Other.dc.html"` attributes.

Sample numbers in the designs (for example 2,000 kcal on Home) are placeholders. In the app, targets come from `NutritionCalculator`.

See `tokens.md` for the colour, type and spacing tokens, and `docs/SPEC.md` for what each screen does.
