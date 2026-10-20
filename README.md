# AR INFOTEK — Online Assessment (Android)

A native **Android app written in Kotlin** (Jetpack Compose) that is a mobile port of the
AR INFOTEK online assessment web app
([arinfotek-assesment.vercel.app](https://arinfotek-assesment.vercel.app)).

It lets students join a test with a test code, take a timed multiple-choice test,
review their scored result, and submit a training feedback survey. An admin panel
manages tests, scores, flagged attempts and survey feedback.

## Features

- **Join / verify** — enter Full Name, Register ID and Test Code
- **Choose** — take the test or take the feedback survey
- **Timed test** — countdown timer, question palette, previous/next navigation,
  answer tracking and a submit confirmation
- **Tab-switch detection** — leaving the app during a test raises a violation flag
  (the test can be re-approved from the admin panel)
- **Result** — score, percentage, pass/fail, and a full answer review
- **Survey** — multi-step feedback survey built from templates, with an NPS question
- **Admin panel** (password: `demo`) — create tests, browse questions, view scores,
  approve tab flags, create surveys, and review/analyse feedback

## Tech stack

- Kotlin 2.2
- Jetpack Compose (Material 3)
- AndroidX Lifecycle ViewModel
- kotlinx.serialization (local JSON persistence)

> The original web app used a Neon PostgreSQL backend. This port stores all data
> **on-device** as JSON so it runs fully offline inside Android Studio / an emulator.
> A demo test (`APT01`) and a survey are seeded on first launch.

## Project structure

```
app/src/main/java/com/example/myapplication/
├── MainActivity.kt            # entry point, navigation, admin auth dialog
├── AppViewModel.kt            # all app state + business logic
├── data/
│   ├── Models.kt              # data classes (Test, Question, Attempt, ...)
│   ├── AppRepository.kt       # local JSON persistence + seed data
│   └── SurveyTemplates.kt     # built-in survey templates
└── ui/
    ├── Common.kt              # header, footer, buttons
    ├── JoinScreen.kt          # join + verified choice screen
    ├── TestScreen.kt          # timed MCQ test
    ├── ResultScreen.kt        # score + answer review
    ├── SurveyScreen.kt        # feedback survey
    └── AdminPanel.kt          # admin panel
```

## Getting started

1. Open the project in **Android Studio**.
2. Let **Gradle sync** complete.
3. Select an emulator or connected device.
4. Press **Run ▶**.

### Demo credentials

- Test code: `APT01`
- Admin password: `demo`

## Build from the command line

```bash
# JDK (use Android Studio's bundled JDK on Windows)
#   set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr

./gradlew :app:assembleDebug      # build the debug APK
./gradlew :app:installDebug       # install on a connected device/emulator
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
