# Zainotes v1.2 — First Initial Clean Release ✨

Welcome to the official **first initial clean release** of **Zainotes (`com.zainotes.app`)**, a modern, native Android notes, tasks, and bulletin application built from the ground up with Jetpack Compose and Material 3 Expressive, licensed under the GNU Affero General Public License v3.0 (AGPLv3).

---

## 🔑 Permanent Release Signing Key & Seamless In-Place Updates
- **Why Previous Version Updates Failed**:
  Previously, releases were signed dynamically in GitHub Actions with ephemeral, throwaway keystores generated during each runner build. Because Android enforces cryptographic signature parity across package updates (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`), attempting to install one version over an earlier version resulted in a signature mismatch error.
- **The Permanent Fix**:
  Starting with this **v1.2 clean release**, Zainotes is built and signed with a permanent, dedicated repository keystore valid through **2054**. 
  > **Notice for previous testers**: Please uninstall any older test builds of Zainotes (v1.0.x / v1.1.x) once, then install this v1.2 release. From this release onward, all future updates will install seamlessly in-place over existing installations!

---

## 🎨 What's New in v1.2

### 1. Google Keep Background Themes & Native Vector Artwork
- **Every theme offered by Google Keep & more**:
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
- **100% Native Vector Drawing**:
  - Implemented entirely via Compose `Canvas` drawing paths, lines, and arcs.
  - Zero external bitmap bloat (0 KB asset overhead, 100% offline).
  - Dynamically scales to any card dimension or full-screen editor while automatically tuning opacity and tint to your theme.

### 2. Vibrant Expressive Note Colors
- Replaced pale, muted pastel tints with punchy, high-contrast colors in both Light and Dark modes:
  - **Coral Red** (`#FF8A80` light / `#6B1E19` dark)
  - **Peach Orange** (`#FFAB91` light / `#6E3310` dark)
  - **Warm Amber** (`#FFD54F` light / `#5E4200` dark)
  - **Electric Lime** (`#D4E157` light / `#3E4E0C` dark)
  - **Emerald Green** (`#81C784` light / `#1B532C` dark)
  - **Teal Mint** (`#4DB6AC` light / `#104E48` dark)
  - **Sky Blue** (`#4FC3F7` light / `#124A75` dark)
  - **Electric Indigo** (`#7986CB` light / `#252D68` dark)
  - **Royal Purple** (`#BA68C8` light / `#53165E` dark)
  - **Hot Rose** (`#F06292` light / `#661034` dark)
  - **Warm Mocha** (`#A1887F` light / `#4B342C` dark)
  - **Cool Slate** (`#90A4AE` light / `#293941` dark)
- Paired with strict WCAG-compliant foreground text tokens so readability is crystal clear in both Light and pure AMOLED `#000000` modes.

### 3. Editor Floating Bottom Navigation Dock
- Replaced static bottom toolbars with an elevated floating dock:
  - **Left Circular Tick Button**: 56dp circular FAB (`✓`) to complete, save, and exit the note in a single tap.
  - **10dp Separation Gap**: Visual separation between the completion trigger and the toolbar.
  - **Right Long Pill Toolbar**: Elevated pill housing actions for:
    - **Text**: Paragraph / body text block.
    - **Bullets**: Quick bulleted lists.
    - **Checklist**: Interactive checkboxes.
    - **1 2 3 List**: Numbered ordered lists with automatic numbering and enter-to-continue flow.
    - **Heading**: Prominent title and section headings.
    - **Theme & Styling**: Opens the combined color swatch and background image picker.
    - **Card Sizing**: In-place card layout picker.

### 4. Floating Home Bottom Navigation Dock
- Centered, floating navigation dock with active-pill horizontal expansion:
  - **Home**: Dynamic filled/outlined notes canvas navigation.
  - **AI Chat**: Intelligent note assistant workspace.
  - **Settings**: Complete appearance, layout, backup, and database controls.
  - **Circular FAB**: Prominent `+` button separated by a 10dp gap to draft new notes instantly.

### 5. Pure AMOLED `#000000` Dark Mode
- Eliminated muddy grayish dark themes. 
- AMOLED dark mode features pitch-black `#000000` canvas and `#080808` surfaces for maximum OLED battery savings.

### 6. Freeform Note Sizing & Adaptive Canvas
- Uncluttered homepage canvas with expressive filter chips (`All`, `Tasks`, `Pinned`).
- Live card size switching: `Standard`, `Wide`, `Compact`, `Hero`, and `Freeform` (displays full content without truncation).

### 7. Local Offline Privacy & Full Data Ownership
- 100% offline Room SQLite database with automated migration.
- Complete JSON backup export and import functionality.
- Minimum 48dp touch targets across all interactive controls.
- Full edge-to-edge window insets handling status bars, navigation bars, and IME keyboard.

---

## 📦 Download & Verification
- **APK File**: `Zainotes-signed.apk`
- **Application ID**: `com.zainotes.app`
- **Min Android SDK**: Android 8.0 (API 26+)
- **Target Android SDK**: Android 15 (API 35)
- **License**: GNU Affero General Public License v3.0 (AGPLv3)
