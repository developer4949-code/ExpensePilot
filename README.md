# ExpensePilot

ExpensePilot is a native Android personal finance companion app built in plain Java and XML for the Zorvyn Mobile App Developer Intern assignment. The app is designed as a lightweight daily finance product, not a banking app, with a strong focus on product thinking, smooth user experience, local data handling, and polished mobile flows.

It helps users track transactions, understand spending patterns, monitor savings progress, receive smart recommendations, and stay engaged through privacy-friendly local reminders.

## APK

- Download APK: [ExpensePilot Debug APK](https://drive.google.com/file/d/1XcLcElBJBO_7ICrUZfvC3PoJrUMjWrtt/view?usp=sharing)

## Assignment Goals Covered

- Home dashboard with quick financial summaries
- Transaction tracking with add, edit, delete, search, and filtering
- Goal-based engagement feature through monthly savings tracking and streak-style nudges
- Insights screen for category and trend understanding
- Smooth mobile-first navigation and screen hierarchy
- Local database-driven finance data
- Organized native Android code structure and state handling

## Highlights

- Native Android stack only: Java + XML
- Strict local Room database for finance data
- Firebase email/password authentication
- Dark mode and light mode with in-app theme switching
- Biometric app lock with protected screen reveal
- Smart local notification engine with per-type toggles
- AI-powered recommendation cards for finance guidance
- CSV export for spreadsheet or recruiter review
- Animated transitions and post-login branded loading experience

## Core Features

### 1. Home Dashboard

The home screen gives an instant view of the user’s money activity with:

- current balance
- total income
- total expenses
- savings momentum
- weekly spending pulse chart
- top spending categories
- recent activity snapshot
- AI coach recommendation card

The layout is intentionally designed to feel informative without becoming crowded.

### 2. Transaction Tracking

Users can fully manage their finance entries with:

- add transaction
- edit transaction
- delete transaction
- searchable history
- type filtering for all, income, and expense
- fields for amount, type, category, date, and note

### 3. Goal and Engagement Layer

To make the app feel more product-oriented instead of only functional, ExpensePilot includes:

- monthly savings goal tracking
- progress-based goal feedback
- low-spend streak motivation
- smart nudges when savings progress falls behind

### 4. Insights Screen

The insights area presents data in a mobile-friendly format using:

- current month summary
- spending comparison between this week and last week
- category mix
- 6-month expense trend
- AI-powered recommendation refresh

### 5. Authentication

Authentication uses Firebase Email/Password login with:

- dedicated signup screen
- dedicated login screen
- persistent session handling
- auth gate on app launch
- redirect to home after successful login
- sign out from the toolbar menu

Important: finance records remain local to the device database. Firebase is used only for account authentication.

### 6. Dark Mode and Theme Switching

The app includes:

- light theme
- dark theme
- bluish gradient visual palette
- in-app theme selection from the overflow menu

### 7. Biometric Lock

ExpensePilot supports an optional biometric lock flow inspired by modern finance apps:

- biometric lock can be enabled from the toolbar menu
- protected screens remain hidden until biometric verification succeeds
- app content stays covered when reopening from background

### 8. Smart Local Notifications

The reminder system is fully local and automatic. It uses on-device analytics and does not require a backend.

Supported reminder types:

- activity reminders
- overspending alerts
- savings goal nudges
- streak rescue reminders

The app now also includes a dedicated in-app notification settings screen where the user can:

- turn the full reminder system on or off
- enable or disable each reminder type individually

### 9. AI Recommendations

To add a differentiating feature, the app includes AI-generated finance suggestions.

- recommendations are shown on Home and Insights
- prompts are built from compact finance summaries rather than raw full-history dumps
- local fallback logic is used if AI is unavailable

This keeps the feature helpful while preserving the app’s local-first structure.

## Tech Stack

- Language: Java
- UI: XML layouts with Material Components
- Architecture: Activity + Fragments + shared ViewModel + Repository
- Local Database: Room on top of SQLite
- Authentication: Firebase Authentication
- Background Work: WorkManager
- State Handling: LiveData
- Security: Android BiometricPrompt

## Data Strategy

ExpensePilot follows a clear hybrid data approach:

- finance data is stored locally using Room
- analytics are computed locally from transaction data
- reminders are generated locally
- authentication is handled through Firebase Email/Password

This keeps the core finance experience offline-friendly and aligned with the assignment’s request for sensible local data handling.

## Project Structure

```text
app/src/main/java/com/debiprasaddas/expensepilot/
|- ExpensePilotApp.java
|- MainActivity.java
|- auth/
|- data/
|  |- dao/
|  |- db/
|  |- entity/
|  |- repo/
|- ui/
|  |- common/
|  |- goals/
|  |- home/
|  |- insights/
|  |- settings/
|  |- transactions/
|- util/

app/src/main/res/
|- drawable/
|- drawable-nodpi/
|- layout/
|- menu/
|- values/
|- values-night/
```

## Main Screens

- Login
- Signup
- Post-login branded loading screen
- Home
- Transactions
- Goals
- Insights
- Notification Settings
- Add/Edit Transaction

## UX Decisions

- The dashboard is optimized for scanning, not dense reporting.
- Transaction entry is designed to be quick and touch-friendly.
- Insights are simplified for small screens instead of looking like a spreadsheet.
- Dark mode, gradients, animation, and branding were used to make the app feel like a polished mobile product.
- Notification preferences are user-controlled to keep reminders useful instead of intrusive.

## Setup Instructions

### Prerequisites

- Android Studio
- Android SDK
- A device or emulator running Android 7.0+ minimum

### Run Locally

1. Open the project in Android Studio.
2. Let Gradle sync complete.
3. Add your local `local.properties` values for Android SDK and, if you want AI recommendations enabled, your Hugging Face token.
4. Make sure Firebase Email/Password sign-in is enabled in the Firebase console.
5. Run the `app` configuration on an emulator or Android device.

### Command-Line Build

```powershell
.\gradlew.bat assembleDebug
```

### Local Properties Example

```properties
sdk.dir=YOUR_ANDROID_SDK_PATH
hfApiToken=YOUR_HUGGING_FACE_TOKEN
hfModel=Qwen/Qwen3-4B-Instruct-2507:cheapest
```

## Firebase Notes

Firebase has already been configured for package name:

`com.debiprasaddas.expensepilot`

Required console step:

- enable `Email/Password` inside Firebase Authentication

## Optional Permissions Used

- `POST_NOTIFICATIONS` for smart reminders on Android 13+
- `INTERNET` for Firebase auth and AI recommendation requests

## Assumptions

- Currency formatting is optimized for INR-style values.
- Finance records are device-local by design.
- AI recommendations are treated as assistive guidance, not authoritative financial advice.
- The app is intended as an assessment-quality product build, not a production banking system.

## Verification

- Debug build verified with `.\gradlew.bat assembleDebug`
- Latest verification completed on April 2, 2026

