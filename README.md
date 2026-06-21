# ClassTrack 🎓

An elegant, modern, and production-ready class attendance tracker and timetable companion built with **Jetpack Compose** and **Material Design 3**. ClassTrack simplifies managing university or school schedules, helping students keep their attendance on target with fluid, swipeable screens and clean visual progress indicators.

---

## ✨ Features & Polish

### 🎨 Material 3 Expressive Design
* **Adaptive Tonal Elements**: Styled with state-of-the-art Material 3 components, linear card gradients, and beautiful negative-space distributions.
* **Smart Percentage Displays**: High-visibility radial progress meters dynamically format indicators (`100%`) without clipping or text overflows inside circular frames.
* **Interactive Ripple Feedback**: Touch targets conform to a minimum of `48dp x 48dp` for accessibility, featuring custom micro-interactions and tactile state indications.

### 🔄 Dynamic Navigation System
* **Fluent Swipe Gestures**: Powered by a robust `HorizontalPager` architecture. Seamlessly swipe horizontally or use the high-contrast bottom navigation bar to bounce between panels.
* **Animated Page Transitions**: Dynamic horizontal slides with ease-in-out properties matching navigation directions.

### 📅 Advanced Timetable & Routine Scheduler
* **Custom Period Mapping**: Beautifully organize each day's layout with active Dropdown selectors, free periods, and custom course associations.
* **Quick Time Calibration**: Quick-action period editors to modify starting and finishing bounds on-the-fly.

### 📊 Attendance Analytics & Log
* **Daily Attendance Registry**: Clean check-in workflows for current days with rapid rollback mechanisms (Select Today).
* **Subject Portfolio**: Visualized breakdowns of each course’s compliance rate, color-coded present/absent counters, and easy subject onboarding without messy icon clutter.

---

## 🛠️ Architecture & Tech Stack

ClassTrack employs standard Android engineering principles and robust modern patterns:
* **Architecture**: Model-View-ViewModel (MVVM) providing clean separations of concerns.
* **UI Framework**: Native **Jetpack Compose** with Material 3 integration.
* **Local Persistence**: **Room Database** powering low-overhead offline queries, data safety, and responsive state flows.
* **Asynchronous Streams**: Built around Native Kotlin **Coroutines** and reactive **Flow** constructs (`StateFlow`).
* **UI Testing & Verification**: JVM unit testing and robust coverage models supported by **Robolectric** integrations.

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio Ladybug** (or newer)
* **SDK 34** (target/compile API)
* **Gradle 8.0+**

### Building from Source

To compile and assemble a debug release APK:
```bash
gradle assembleDebug
```

To run the local JVM test suite (unit and architecture validation logs):
```bash
gradle :app:testDebugUnitTest
```

---

## 📂 Core Package Map

```
/app/src/main/java/com/example/
├── data/              # Database entities, DAO definitions, and Repository layers
│   ├── AppDatabase.kt
│   └── AttendanceRepository.kt
├── ui/                # Core Compose View declarations, ViewModels, and App theming
│   ├── DashboardScreen.kt
│   └── theme/
└── MainActivity.kt    # App Launcher class and system environment setup
```
