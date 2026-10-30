# ✨ Zainotes

> A beautiful, expressive, and open-source Android notes & tasks app built with Jetpack Compose and Material 3 Expressive.

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Android](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84.svg?logo=android)](https://www.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3 Expressive](https://img.shields.io/badge/Design-Material_3_Expressive-FF6F00.svg)](https://m3.material.io/blog/building-with-m3-expressive)
[![CI Build](https://github.com/hydroxideOH-OMG/Zainotes/actions/workflows/build.yml/badge.svg)](https://github.com/hydroxideOH-OMG/Zainotes/actions)

---

## 🌟 Highlights

- 🎨 **Material 3 Expressive Design**: Vibrant tonal palettes, dynamic Monet theming, organic rounded corners, spring physics animations, and expressive interactive components.
- 🖤 **AMOLED Pure Black Mode**: True `#000000` dark theme for stunning contrast and battery savings on OLED screens.
- 🌈 **Vibrant Note Backgrounds**: Choose from curated pastel tonal backgrounds (Default, Coral, Sage, Lavender, Amber, Sky, Rose, Mint, Sand).
- 🧩 **Block-Based Hybrid Canvas**:
  - 📝 **Rich Paragraph Blocks**: Capture thoughts smoothly without awkward markdown delimiters.
  - ☑️ **Interactive Task Checkboxes**: Integrated check-lists inside notes with fluid strike-through animations.
  - 📌 **Bulletins / Bullet Points**: Structure thoughts and lists effortlessly.
  - 🏷️ **Headings & Callouts**: Organize distinct sections within each note.
- 📐 **Resizable Note Cards on Homepage**:
  - Expressive Staggered Masonry Grid.
  - Customize card sizes individually (Compact, Standard, Wide full-width banner, Tall/Hero).
  - Interact with tasks and checklists right from the home cards without entering the editor!
- 🔒 **100% Private & Offline-First**: Powered by Android Room (SQLite). Zero trackers, zero ads, no accounts required.
- 💾 **Backup & Restore**: Export and import your notes and tasks cleanly as JSON.
- ⚖️ **Free & Open Source**: Licensed under the GNU Affero General Public License v3.0 (AGPLv3).

---

## 📱 Screenshots & Demo

*(Screenshots generated from Material 3 Expressive UI components)*

| Homepage Staggered Grid | Block & Task Canvas | Color Palette & AMOLED |
| :---: | :---: | :---: |
| Masonry layout with custom card sizes | Checklist items, bulletins & text | Pure black & expressive pastel tones |

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.0.21
- **UI Toolkit:** Jetpack Compose + Compose Material 3 1.3.1 (Material 3 Expressive)
- **Database:** Android Room 2.6.1 + KSP
- **Data Flow:** Kotlin Coroutines + Flow + StateFlow
- **Architecture:** Clean Architecture + MVVM (Model-View-ViewModel)
- **Preferences:** Jetpack DataStore Preferences
- **Build System:** Gradle Kotlin DSL with Version Catalogs (`libs.versions.toml`)
- **CI/CD:** Automated GitHub Actions workflows for testing, APK compilation, and GitHub Releases.

---

## 🚀 Building & Releasing

Zainotes builds completely in the cloud via **GitHub Actions**:

1. **Continuous Integration**: Pushes and Pull Requests automatically trigger `.github/workflows/build.yml`, assembling the Debug APK and running code checks.
2. **Release Pipeline**: Pushing a version tag (e.g. `v1.0.0`) triggers `.github/workflows/release.yml`, which compiles the optimized release APK and publishes it to GitHub Releases.

### Local Development (Optional)
To build locally with Android Studio:
```bash
git clone https://github.com/hydroxideOH-OMG/Zainotes.git
cd Zainotes
./gradlew assembleDebug
```

---

## 📜 License

Zainotes is free and open-source software licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**. See the [LICENSE](LICENSE) file for the full license text.
