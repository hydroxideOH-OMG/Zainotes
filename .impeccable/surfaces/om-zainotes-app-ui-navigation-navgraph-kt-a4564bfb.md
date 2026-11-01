---
version: 1
slug: "om-zainotes-app-ui-navigation-navgraph-kt-a4564bfb"
primary_target: "app/src/main/kotlin/com/zainotes/app/ui/navigation/NavGraph.kt"
related_targets: ["app/src/main/kotlin/com/zainotes/app/ui/screens/home/HomeScreen.kt","app/src/main/kotlin/com/zainotes/app/ui/screens/editor/EditorScreen.kt","app/src/main/kotlin/com/zainotes/app/ui/screens/settings/SettingsScreen.kt"]
---

Scope: Zainotes Android app, including notes home, editor, settings, reusable note cards and block controls, and primary navigation. Mode: Operate.

Audience and task: Mobile note-takers, students, and planners capture a thought, organize it into expressive note blocks, then scan and act on notes from a customizable canvas grid. Preserve core features, offline storage, backups, note colors, dynamic color, and AMOLED mode. Prioritize comfortable system-bar/IME visibility, speed, clear organization, accessibility, and meaningful motion.

THESIS: Make the note itself the organizing surface: a visually varied, glanceable canvas where size, pastel tone, and task state make each note recognizable and immediately actionable. Refuse the generic flat note list and a decorative grid that makes scanning harder.

OWN-WORLD: Material 3 Expressive extended through restrained surface-role color, intentional shape contrast, a small number of prominent actions, accessible controls, and spring/morph motion only where it clarifies state. Dynamic Material roles drive app chrome; note colors distinguish user content. System bars, display cutouts, IME, and navigation are first-class layout boundaries.

STORY: On Home, identify the notes space, search/filter the collection, scan varied card sizes, and check tasks without opening. In the editor, focus title and blocks while block tools remain reachable above the IME and system navigation. In Settings, find appearance, layout, backup, and project information in calm, clearly grouped sections. Preserve navigation identity across compact and wide windows; keep editor as a focused task.

FIRST VIEWPORT: Home opens below the status/cutout inset with a compact app bar, prominent “Your notes” heading, semantic M3 search field, filter-chip row, and an adaptive staggered grid of distinguishable cards; the create FAB floats above navigation. Compact windows use a bottom NavigationBar for Notes and Settings; wider windows use a NavigationRail. Editor removes top-level nav and offers a title/canvas with block tools above the IME and gesture area.

FORM: Expressive canvas grid, selected by the user from the structure round. Position: selected over the action-first and collection/detail alternatives. Seed key: 8ac53db3 (degraded roll; no challenger catalog or quality-bar boards).

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
