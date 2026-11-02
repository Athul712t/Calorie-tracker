# Release checklist

What is done, and what only you can do.

## Done in the repo
- App data backup is off (`allowBackup=false`, empty backup and device-transfer rules). All data is in Firestore and Storage.
- Unused libraries removed: Room, Retrofit, OkHttp, Moshi, Coil, and the KSP plugin (nothing in the code used them).
- CI builds and tests on every pull request and attaches the debug APK to the run.
- Signing keys (`*.jks`) are git-ignored.

## Package id: left as it is on purpose
`applicationId` is `com.aistudio.calorietracker.inmpxq` (AI Studio generated it). The Kotlin `namespace` (`com.example`) is only a code folder name and does not matter to users.

Changing `applicationId` makes Android treat the app as a different app, and it breaks the Firebase setup until you redo it:
1. Add a new Android app in the Firebase console with the new id and download its `google-services.json`.
2. Add the SHA-1 and SHA-256 of the debug and release keys to that app (Google sign-in needs them).
3. Register the new app in App Check.
4. Update `applicationId` in `app/build.gradle.kts`.

Do this once, before the first Play Store upload, because the id cannot change after that. Suggested form: `in.<yourname>.calorietrack` or any domain you own.

## Before the first Play Store upload
- [ ] Final package id (above)
- [ ] Release signing key and Play App Signing; set `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` in CI secrets
- [ ] Turn on code shrinking (`isMinifyEnabled = true`) and test the release build on a device; Firebase and Credential Manager may need keep rules
- [ ] Deploy `firestore.rules` and `storage.rules`
- [ ] App Check: switch from debug provider to Play Integrity for release
- [ ] Privacy policy URL and the Play data-safety form (collects: account, body measurements, food logs, photos)
- [ ] In-app account deletion (Play requires it for apps with accounts): delete Firestore data, Storage files and the Firebase user
- [ ] Real app icon and store listing
