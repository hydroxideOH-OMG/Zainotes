# Zainotes v1.2 — First Initial Clean Release ✨

Welcome to the official **first initial clean release** of **Zainotes (`com.zainotes.app`)**, a modern, native Android notes, tasks, and bulletin application built with Jetpack Compose and Material 3 Expressive, licensed under the GNU Affero General Public License v3.0 (AGPLv3).

---

## 🔑 Permanent Release Signing Key & Seamless In-Place Updates
- **Why Previous Version Updates Failed**:
  Previously, releases were signed dynamically in GitHub Actions with ephemeral, throwaway keystores generated during each runner build. Because Android enforces cryptographic signature parity across package updates (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`), attempting to install one version over an earlier version resulted in a signature mismatch error.
- **The Permanent Fix**:
  Starting with this **v1.2 clean release**, Zainotes is built and signed with a permanent, dedicated repository keystore valid through **2054**. 
  > **Notice for previous testers**: Please uninstall any older test builds of Zainotes (v1.0.x / v1.1.x) once, then install this v1.2 release. From this release onward, all future updates will install seamlessly in-place over existing installations!

---

## 🎨 What's New & Refined in v1.2

### 1. Interactive Free-Form Sizing (Image-Crop Style)
- Long-pressing any note card now presents a dedicated **"Free-form sizing (Crop resize)"** option.
- Opens an interactive image-crop canvas overlay featuring:
  - 4 corner drag handles with live gesture manipulation
  - 3x3 rule-of-thirds composition guidelines
  - Thicker visual crop corner accents
  - Real-time preview of the note card inside the crop boundary
  - Quick Cancel and Confirm FABs to commit the chosen card dimension

### 2. Strict Pure AMOLED `#000000` Black
- Complete overhaul of AMOLED Dark Mode across all UI surfaces:
  - Background: Pitch black `#000000`
  - Floating bottom navigation dock: Pitch black container with 0dp tonal tinting
  - Editor action pill toolbar: Pitch black container with 0dp tonal tinting
  - Card Size and Theme bottom sheets: Pitch black containers with 0dp tonal elevation
  - Full system window bar integration with pure `#000000` status and navigation bars

### 3. Google Keep Background Themes & Vector Art Bounding
- Fixed vector illustrations across all 15 themes so they scale properly and stay crisp and visible in both note cards and full-screen note editor views:
  - 🛒 **Groceries**: Shopping tote bag, stalks/baguette, and fresh fruit.
  - 🍳 **Food & Cooking**: Skillet, chef hat, and aromatic steam curls.
  - 🎵 **Music**: Modern over-ear headphones and floating musical notes.
  - 🥣 **Recipes**: Rolling pin, whisk, and mixing bowl.
  - 📝 **Notes & Pen**: Spiral notepad, fountain pen, and paperclips.
  - ✈️ **Travel & Places**: Mountain peaks, compass, and soaring airplane trail.
  - 🎬 **Cinema & Film**: Clapperboard, film strip, and popcorn.
  - 🎉 **Celebration**: Festive party balloons and confetti stars.
  - ⛈️ **Storm & Weather**: Billowy storm cloud, lightning bolt, and raindrops.
  - 🌙 **Night Sky & Dusk**: Crescent moon and sparkling constellation stars.
  - 🌿 **Flora & Nature**: Tropical split-leaf Monstera deliciosa foliage.
  - ✏️ **Abstract Doodles**: Playful spirals, sparks, and squiggles.
  - 📏 **Ruled Lines**: Classic lined notebook paper.
  - 📐 **Graph Grid**: Technical drafting grid.
  - 🔘 **Dot Matrix**: Bullet journaling dot matrix.

### 4. Smart 1 2 3 Numbered Lists
- Corrected list numbering behavior:
  - Numbered lists now automatically calculate sequence based on consecutive numbered blocks.
  - Starting a numbered list at any line begins properly at `1.`
  - Inserting body text, tasks, or bullet points resets the numbered list so the next numbered group starts fresh at `1.`
  - Supports enter-to-continue flow while editing.

### 5. Floating Bottom Navigation & Editor Docks
- **Home Dock**: Elevated pill dock with active-pill horizontal expansion + circular `+` FAB separated by a 10dp gap.
- **Editor Dock**: 56dp circular tick/done button (`✓`) separated by a 10dp gap from the action pill containing Text, Bullets, Checklist, 1 2 3 List, Heading, Theme, and Size tools.

---

## 📦 Download & Verification
- **APK File**: `Zainotes-signed.apk`
- **Application ID**: `com.zainotes.app`
- **Version Name**: `1.2.0`
- **Min Android SDK**: Android 8.0 (API 26+)
- **Target Android SDK**: Android 15 (API 35)
- **License**: GNU Affero General Public License v3.0 (AGPLv3)
