---
name: Zainotes
description: An expressive, adaptive Android canvas for notes and tasks.
colors:
  primary-light: "#6750A4"
  primary-dark: "#D0BCFF"
  background-light: "#FEF7FF"
  background-amoled: "#000000"
  surface-amoled: "#080808"
  coral-note: "#FFDAD6"
  amber-note: "#FFE08B"
  sage-note: "#C7EBD1"
  mint-note: "#BAF4EB"
  sky-note: "#C8E6FF"
  lavender-note: "#EADBFF"
  rose-note: "#FFD8EB"
  slate-note: "#DEE2EC"
typography:
  display:
    fontFamily: "Roboto, sans-serif"
    fontSize: "54sp"
    fontWeight: 700
    lineHeight: "62sp"
  headline:
    fontFamily: "Roboto, sans-serif"
    fontSize: "30sp"
    fontWeight: 600
    lineHeight: "38sp"
  title:
    fontFamily: "Roboto, sans-serif"
    fontSize: "20sp"
    fontWeight: 600
    lineHeight: "26sp"
  body:
    fontFamily: "Roboto, sans-serif"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
  label:
    fontFamily: "Roboto, sans-serif"
    fontSize: "14sp"
    fontWeight: 500
    lineHeight: "20sp"
rounded:
  sm: "12dp"
  md: "18dp"
  lg: "20dp"
  xl: "28dp"
spacing:
  sm: "8dp"
  md: "12dp"
  lg: "16dp"
  xl: "20dp"
components:
  primary-action:
    backgroundColor: "{colors.primary-light}"
    textColor: "#FFFFFF"
    rounded: "{rounded.lg}"
    size: "48dp minimum interactive target"
  note-card:
    backgroundColor: "semantic surface role or selected note color"
    textColor: "on-surface or on-note-container"
    rounded: "{rounded.lg}"
    padding: "16dp"
  home-grid:
    size: "adaptive columns, minimum cell 176dp"
  navigation:
    size: "bottom NavigationBar below 600dp; NavigationRail from 600dp"
---

# Design System: Zainotes

## Overview

**Creative North Star: “The expressive notes canvas.”**

Zainotes uses Material 3 Expressive as a working interface system: dynamic semantic color for app chrome, a small set of note-specific color containers, varied card spans, tactile but restrained shape contrast, and motion attached to changes in task state and card content. The content stays primary; expression helps people recognize, scan, and act on their notes.

The UI adapts to Android theme, wallpaper color on Android 12+, AMOLED mode, window width, system bars, display cutouts, and keyboard insets. Routine settings and editing remain composed and readable. 

**Key Characteristics:**
- A glanceable, adaptive staggered note grid.
- Material semantic roles for app chrome; note colors belong to note content.
- Touch targets, system insets, and readable state are part of the layout.
- Motion explains checkbox progress and changing card content.

## Colors

Material dynamic color is used when enabled and supported; the listed light/dark values provide the static fallback. Note containers have paired foreground colors in `ui/theme/Color.kt`.

### Primary
- **Expressive violet** (`#6750A4` light / `#D0BCFF` dark): primary action and selection in the static schemes.

### Secondary
- **Muted violet** (`#625B71` light / `#CCC2DC` dark): supporting emphasis.

### Tertiary
- **Rose** (`#7D5260` light / `#EFB8C8` dark): contrasting expressive emphasis.

### Neutral
- **Light background** (`#FEF7FF`): static light theme canvas.
- **AMOLED pure black** (`#000000`) and **AMOLED surface** (`#080808`): the sole dark theme canvas for both manual AMOLED mode and system dark theme. Standard grayish dark mode is removed.
- **Note colors:** Coral, Amber, Sage, Mint, Sky, Lavender, Rose, and Slate; `DEFAULT` resolves from the active Material surface role.

### Named Rules
**The Semantic Chrome Rule.** App-level surfaces use Material color roles; user-selected note colors are reserved for note content and previews.
**The AMOLED Dark Rule.** Dark mode is always true `#000000` pitch black to maximize OLED power efficiency and eliminate grayish dark backgrounds.

## Typography

**Display Font:** Roboto system sans-serif.
**Body Font:** Roboto system sans-serif.
**Label/Mono Font:** Roboto system sans-serif.

**Character:** Familiar Android system typography, using weight and scale to clarify hierarchy without competing with note content. All text sizes use `sp` and follow system scaling.

### Hierarchy
- **Display** (bold, 54sp / 62sp): large expressive moments only.
- **Headline** (semibold, 30sp / 38sp): screen and note hierarchy.
- **Title** (semibold, 20sp / 26sp): cards and grouped areas.
- **Body** (regular, 16sp / 24sp): editable note content and explanations.
- **Label** (medium, 14sp / 20sp): controls and compact metadata.

## Layout

Home uses an adaptive staggered grid with freeform note sizing: notes can span standard tiles, compact previews, wide banners, hero spotlights, or flow organically with full content length (`FREEFORM`). The homepage masthead features expressive filter chips (All, Tasks, Pinned) and a quick Freeform layout switcher, with the search bar removed for an uncluttered note-taking experience. Compact windows use a bottom NavigationBar for Notes and Settings; windows at or above 600dp use a NavigationRail. The editor is a focused destination and hides top-level navigation while editing. Settings content is vertically scrollable, width-bounded, and organized into semantic Material 3 cards. Material app bars, Scaffold insets, and IME insets keep content clear of system UI.

The component rhythm uses 8–20dp spacing. Interactive controls retain at least a 48dp touch area where specified by the component.

## Elevation & Depth

Depth is primarily tonal, following Material surface roles. Note cards are flat tonal containers by default; component elevation is reserved for controls that need separation, such as the floating create action and bottom block toolbar.

## Shapes

The shape scale distinguishes compact controls from content containers: 8dp extra-small, 12dp small, 18dp medium, 20dp large/card, and 28dp extra-large. Chips use a distinct 14dp silhouette. Shape is not applied uniformly; card containers and small selections retain different roles.

## Components

### Buttons
- **Shape:** Material component shapes from `ExpressiveShapes`.
- **Primary:** One prominent create FAB on the home canvas.
- **Focus / state:** Use Material component focus, selected, disabled, and pressed behavior.

### Chips
- **Style:** FilterChip for collection filtering; AssistChip for contextual block insertion.
- **State:** Selection follows Material filter-chip semantics, not color alone.

### Cards / Containers
- **Corner Style:** 20dp note cards, using the selected note container or a semantic surface role.
- **Internal Padding:** 16dp.
- **Behavior:** Card tap opens its note; task checkboxes act in place; overflow actions contain pin, size, and delete choices.

### Inputs / Fields
- **Style:** Material outlined search and note-title fields with semantic labels.
- **Behavior:** Search filters notes and blocks; title edits persist to local storage.

### Navigation
- **Floating Bottom Navigation Dock:** Centered, elevated floating navigation dock composed of an M3 Expressive floating navigation pill (Home, AI Chat, Settings) with active-pill horizontal expansion, and a standalone circular M3 FAB separated by a 10dp gap.
- **Dock Elevation:** Level 2/3 drop shadow (`6dp` shadow elevation, `ambientColor` and `spotColor`).
- **Dock Colors:** `surfaceContainerHighest` container, `secondaryContainer` active indicator pill, `onSecondaryContainer` active text and icon, `onSurfaceVariant` inactive icons.
- **Editor:** Temporarily hides the floating navigation dock to keep the note canvas and block toolbar focused; system Back returns to the prior destination.

### Note Canvas
The staggered card grid is the product's organizing surface. Task completion is available directly from card previews. Progress and card content changes use spring animation while the task's checked state remains explicitly represented by its checkbox and strike-through.

## Do's and Don'ts

### Do:
- **Do** use semantic Material roles for app chrome and pair note colors with their corresponding foreground colors.
- **Do** adapt collection density and navigation to the available window width.
- **Do** respect system bars, cutouts, the navigation gesture area, and the IME.
- **Do** preserve clear text and controls when motion or custom note colors are present.

### Don't:
- **Don't** use color alone to communicate selection or task completion.
- **Don't** place essential controls under system bars or keyboard surfaces.
- **Don't** turn unrelated actions into navigation or use filter chips as primary actions.
- **Don't** animate routine content without communicating a meaningful state change.
