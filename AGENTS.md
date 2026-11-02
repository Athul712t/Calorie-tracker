# Working on CalorieTrack

Read `docs/SPEC.md` first. It defines the screens, data model, calorie formulas and AI behaviour. If code and spec disagree, change the spec in the same pull request or ask the owner.

## Stack
Android, Kotlin, Jetpack Compose (Material 3), Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`). Firebase Auth, Firestore, Firebase AI Logic (Gemini) and App Check. Tests: JUnit 4 and Robolectric on the local JVM.

## Design
- Screens must match `docs/design/reference/*.dc.html`. Use tokens from `ui/theme/AppTokens.kt`; do not hardcode colours or sizes.
- Font is DM Sans only.
- The old AI Studio UI (`CalorieTrackerScreen` and `ui/components/*`) is being replaced screen by screen. Do not extend it.

## Rules
- Calorie and macro maths lives only in `domain/NutritionCalculator.kt` and must have tests. Never duplicate formulas in UI code.
- No API keys in the app. Gemini is called through Firebase AI Logic.
- Do not edit the `signingConfigs` or the `signingConfig` lines in `app/build.gradle.kts`; AI Studio needs them.
- Add dependencies through `gradle/libs.versions.toml` with pinned versions.
- Photos go to Firebase Storage, never into Firestore documents.
- Keep commits small. Work on a branch and open a pull request; do not push to `main`.
- Branch names: `claude/<topic>` for Claude, `studio/<topic>` for AI Studio.

## Before opening a pull request
Run `./gradlew testDebugUnitTest` (or `gradle testDebugUnitTest`) and make sure it passes.
