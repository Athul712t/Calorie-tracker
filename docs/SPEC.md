# CalorieTrack – product spec

Android app that logs meals from a photo, text or search, shows calories and macros against a personalised daily target, and tracks weight and progress photos. Designs are in `docs/design/reference`; tokens in `docs/design/tokens.md`.

## 1. Navigation map

| Route | Screen | Design file | Status |
|---|---|---|---|
| `onboarding` | Onboarding | Onboarding.dc.html | not built |
| `home` | Home (and first-day empty state) | Main.dc.html, EmptyHome.dc.html | old UI in place, to be replaced |
| `progress` | Progress | Progress.dc.html | not built |
| `photos` | Progress photos | Photos.dc.html | not built |
| `profile` | Profile | ProfileSimple.dc.html | not built |
| `profile/edit` | Edit profile | ProfileEdit.dc.html | not built |
| `goal/edit` | Edit goal | GoalEdit.dc.html | not built |
| `capture` | Capture (auto-detect) | CaptureAuto.dc.html | not built |
| `analysing` | Analysing | – | not designed |
| `meal/{mealId}` | Meal detail with chat edit | MealDetail.dc.html, MealChat.dc.html | not built |
| `food/add` | Add food (search) | AddFood.dc.html | not built |
| `history` | Day history | DayHistory.dc.html | not built |

Not used: `Profile.dc.html` was removed; `Capture.dc.html` (tabbed capture) is superseded by `CaptureAuto.dc.html`; `Attached.dc.html` is Home with a photo attached (a state of Home, not a screen).

Typical flow: Onboarding → Home → (Capture or Add food or chat bar) → Analysing → Meal detail → Home. Home → Progress (top toggle) → Photos. Home → Profile (top right) → Edit profile / Edit goal. Home meal log header → History.

## 2. Screens

**Onboarding.** Stepped flow, one question per step, progress shown. Login first (single Login action, Google sign-in). Collects goal type, sex, age, height, weight, goal weight, activity, how you eat (chips plus a "custom" chip that opens a text input), then a "Your plan is ready" screen showing the daily target and date to reach the goal, computed with section 4.

**Home.** Header: logo, Home/Progress toggle, profile button. Week strip of days (small calorie ring per day; tap to switch day). Calorie card: ring with eaten kcal and the kcal left (or over, in amber). Macro card: three rings with grams eaten and status. Weight card: current weight, small trend line, change this week, plus button that opens the weight-log sheet (round − / + steppers, Save). Meal log: rows with photo thumbnail, name, time and meal type, macros and kcal; tap opens Meal detail. "History" button opens History. Floating bottom bar: camera button (opens Capture) and a chat input ("Enter your food…"); a photo can be attached to the chat. Empty state (first day): "Log your first meal" card with Take a photo and Search food, rings at zero, "No meals yet" card, three tips.

**Capture.** Full-screen camera with gallery button, shutter and flash. No tabs. The title at the top changes as the content is detected (meal, scale, body photo). Meal photo → Analysing → Meal detail.

**Analysing.** (Not designed.) Shows the photo with a progress state. On failure: "Couldn't identify this" with Try again, Search food, and Describe it.

**Meal detail.** Photo, name, meal type dropdown and portion text input in one row, total kcal and macros, item list (name, portion, kcal, − / + and delete per item), nutrition summary. Bottom row: small chat icon and a wide Done button. Chat edit: the icon opens a chat thread where the user types changes ("make it 3 rotis", "no ghee"); the close icon becomes a camera icon while open. The assistant applies the change and shows the updated totals.

**Add food.** Search field with camera shortcut, meal type chips (Breakfast, Lunch, Snack, Dinner), list of common foods (filters as you type). Tapping a food opens a sheet with servings text input, quick chips (0.5×, 1×, 1.5×, 2×), live kcal and macros, and "Add to <meal>". An "Added" bar shows items and total kcal with Done. "Can't find it? Describe it" opens the chat edit flow.

**History.** Month calendar (Mon–Sun). Each day is tinted: on target (green), over (amber), under (blue), not logged (dashed). Month stats: days logged, days on target, average kcal. Selecting a day shows its totals, macros, meals, Add meal, and Copy to today. Empty day: "Nothing logged" with Add a meal and Copy yesterday. Future days are disabled.

**Progress.** Insights card on top (expandable, with sample Q&A), weight card with chart, progress-photos card (few previews and an arrow to Photos), calories bar chart, macros "Average of 1 week". A chat bar at the bottom of the page (not inside a card) for asking questions about progress.

**Progress photos.** Grid of four angles (Front, Left, Right, Back) with labels. Tapping opens a full-screen viewer with angle tabs. A "Memory lane" slider below the grid moves all photos through time (one stop per weigh-in with photos), showing date, weight and change since start. Capture button floats at the bottom.

**Profile.** Profile card (avatar, name, "Male · 30 yrs · 175 cm") with a pencil to Edit profile. Goal card (goal line, current and goal weight, progress bar, kcal and macro chips) with its own pencil to Edit goal. Then diet and allergies as a text line with edit, preferences, and a three-dots menu for the rarely used items.

**Edit profile.** Name, Male/Female, age, height, activity level (Light, Moderate, Active, each with a one-line explanation). Weight is not here; it comes from weigh-ins.

**Edit goal.** Goal type (Lose, Maintain, Gain), goal weight, pace (Gentle 0.25, Steady 0.5, Faster 0.75 kg/week). Live preview card with daily kcal, macros and the date to reach the goal. "Set manually" switch reveals fields for calories, protein, carbs and fat and a note on whether the macros add up to the calories; "Reset to recommended" turns it off. A line shows the profile values used, with an Edit link.

## 3. Data model (Firestore, all under `users/{uid}`)

| Collection / doc | Fields |
|---|---|
| `profile/current` | name, sex, birthYear or age, heightCm, activity, dietNotes, allergies |
| `goals/current` | type, goalWeightKg, paceKgPerWeek, manual (bool), calories, proteinG, carbsG, fatG |
| `meals/{id}` | name, mealType, dateString (yyyy-MM-dd), loggedAt, calories, protein, carbs, fat, fiber, servings, items[] (name, portion, quantity, unit kcal and macros), photoPath, confidence, source (photo, text, search) |
| `weights/{id}` | dateString, kg, loggedAt |
| `photos/{id}` | angle, dateString, storagePath |
| `menu_items/{id}` | saved foods (existing) |

Photos are stored in Firebase Storage under `users/{uid}/photos/...` and meal photos under `users/{uid}/meals/...`; Firestore keeps only the path. Firestore rules (`firestore.rules`) must be extended for each new collection, keeping the owner-only and field validation pattern.

## 4. Calorie model (implemented in `domain/NutritionCalculator.kt`)

- BMR (Mifflin-St Jeor) = 10 × kg + 6.25 × cm − 5 × age + 5 (male) or − 161 (female).
- Maintenance = BMR × activity: Light 1.4, Moderate 1.55, Active 1.7.
- Daily calories = maintenance − pace × 1100 (lose), + pace × 1100 (gain), or maintenance (maintain). Round to the nearest 50. Minimum 1200.
- Protein = 1.6 g × kg (rounded). Fat = 25% of calories ÷ 9 (rounded). Carbs = the remainder ÷ 4, minimum 0.
- Weeks to goal = ceil(|current − goal| ÷ pace). Date = today + weeks.
- Manual mode overrides all four numbers; show the kcal the macros add up to (4 / 4 / 9) and warn if it differs from calories by more than 5%.
- Day status (History): on target = within −10% to +5% of the goal (1,800–2,100 kcal for a 2,000 goal); over = above that; under = below it.

## 5. AI behaviour

- Meals are analysed with Gemini through Firebase AI Logic (no API key in the app, App Check on).
- Input: optional photo, optional text, optional notes, time-based default meal type. Output: strict JSON with name, mealType, totals, confidence and an `ingredients` list (name, portion, quantity, per-unit kcal and macros). Indian values follow ICMR-NIN IFCT 2017 (the existing prompt in `AiFoodService`).
- Totals are always recomputed from the items on the client; the model's totals are not trusted.
- Chat edit: send the current meal JSON plus the user's instruction; the model returns the updated meal in the same schema.
- Failure: never substitute made-up values. Show the error with Try again, Search food and Describe it.
- Offline or failed: `IndianFoodDatabase.parseMultiItems` parses text such as "2 roti and dal" locally.
- Progress Q&A (Insights chat): answers use the user's own logged data passed as context.

## 6. Permissions and privacy
Camera and photo-library access are requested at the moment of first use with a one-line reason. Body details and photos are private to the user; account deletion removes Firestore data and Storage files. Backup of app data is off unless the backup rules are reviewed.

## 7. Build order
1. Foundation: tokens, DM Sans, navigation shell, calculator and tests, docs, CI (this branch).
2. Core loop: Onboarding, Home, Capture, Analysing, Meal detail with chat edit.
3. Plan and profile: Profile, Edit profile, Edit goal, weight log.
4. Browse and search: Add food, History, empty state.
5. Progress and Photos.
6. Release hardening: package id, backup rules, dependency cleanup.
