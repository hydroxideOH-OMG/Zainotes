# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Everyday note-takers, students, knowledge workers, and task/checklist planners on mobile who want instant capture, frictionless task management, and private offline storage without clutter, sign-ups, or cloud lock-in.

## Product Purpose

Zainotes provides an expressive, fluid, and distraction-free mobile workspace for capturing thoughts, structuring tasks, and managing checklists on Android. Success means zero-latency capture, delightful touch feedback, intuitive visual organization on the home screen, and complete local data sovereignty.

## Positioning

A hybrid non-markdown canvas blending freeform notes, interactive checklist tasks, bullet lists, and section headers into adaptable cards. Unlike rigid text-only or complex markdown editors, tasks remain directly actionable from both within the note and from the resizable masonry grid on the homepage, combined with native Material 3 Expressive aesthetics and a true AMOLED pure black dark mode.

## Operating Context

- Mobile handheld usage on Android devices (phones and small foldables, portrait and landscape).
- Dynamic system environments: system bars (status bar, navigation bar), edge-to-edge layout, camera cutouts / display notches, keyboard insets (`imePadding`), and dynamic system palettes (Monet).
- Daily workflows: quick idea dumping, daily grocery/errand checklists, project bulletin points, and home-screen glanceable task progress.

## Capabilities and Constraints

- **Capabilities:**
  - Block-based hybrid canvas: Plain text paragraphs, task checkboxes with animated strike-through, bulletin points, and headings.
  - Interactive homepage cards: Checklists can be ticked/unticked right from the home card preview.
  - Resizable note card spans: Compact, Standard (1x1), Wide Banner (2x1), and Expanded Hero spotlight.
  - Visual customization: Curated pastel color palette (Coral, Amber, Sage, Mint, Sky, Lavender, Rose, Slate) + Pure AMOLED Black mode + Android 12+ Monet dynamic theming.
  - Offline-first Room database (SQLite) with JSON backup export and import.
- **Constraints:**
  - Strict AGPLv3 open-source licensing.
  - No local Gradle compilation on the host device; all APK builds and releases run strictly via GitHub Actions.
  - Offline-first: zero trackers, zero cloud accounts, zero unsolicited network calls.

## Brand Commitments

- **Name:** Zainotes (`com.zainotes.app`)
- **Tone & Voice:** Expressive, clean, calm, respectful of battery and user attention.
- **Visual identity:** Material 3 Expressive motion and squircle shapes, paired with fluid Apple-grade physical motion responsiveness (instant touch-down feedback, interruptible springs, zero input lag).

## Evidence on Hand

- Native Kotlin & Jetpack Compose codebase under `/data/data/com.termux/files/home/zainotes`.
- AGPLv3 License file in repository root.
- Automated GitHub Actions build (`build.yml`) and release pipeline (`release.yml`).
- Released v1.0.0 APK on GitHub Releases.

## Product Principles

1. **Fluid Direct Manipulation:** Touch feedback must be instantaneous with zero lag. Checkboxes toggle immediately with delightful spring animations.
2. **Glanceable & Actionable:** Notes aren't static walls of text; tasks can be checked off directly from the home feed without diving into the editor.
3. **Respect for Screen & Hardware:** Impeccable edge-to-edge handling with safe insets across status bars, navigation bars, and display cutouts, coupled with true AMOLED black for OLED efficiency.
4. **Zero Lock-In & Total Privacy:** User notes remain strictly on-device in SQLite, with clean JSON backup/restore portability.

## Accessibility & Inclusion

- Dynamic contrast adaptation for all 9 pastel container colors across both light, dark, and pure AMOLED black modes.
- Support for system font scaling (Dynamic Type) without clipping.
- Minimum 48dp touch target zones on all interactive items, icons, and block toggles.
- Proper content descriptions for assistive screen readers across all actions.
