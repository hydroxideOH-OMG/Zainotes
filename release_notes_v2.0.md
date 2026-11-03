# Zainotes v2.0.0

A major milestone release for Zainotes (com.zainotes.app). This release introduces full R8 code shrinking and resource optimization, integrated AI assistant chat, extended note block architecture, and polished Material 3 Expressive UI components.

---

### Key Highlights in Version 2.0.0

#### 1. R8 Code Shrinking and Resource Optimization
- Enabled full R8 minification (`isMinifyEnabled = true`) and resource shrinking (`isShrinkResources = true`).
- Configured dedicated optimization and keep rules preserving Room models, JSON converters, serialization attributes, and coroutines.
- Significantly reduced APK download size and improved runtime startup efficiency.

#### 2. Native AI Assistant Chat and Tooling
- Dedicated AI chat surface accessible directly from navigation.
- Multi-provider AI client support with flexible endpoint configuration.
- Persistent session storage allowing multiple conversation threads.
- Contextual note actions, drafting assistance, and task generation.

#### 3. Enhanced Rich Note Blocks
- Extended note block system with rich formatting, code blocks, quote blocks, and tables.
- Sequential 1 2 3 numbered list clustering that automatically counts within runs and resets after interruptions.
- Real-time task progress tracking with animated expressive progress indicators.

#### 4. Interactive Free-Form Sizing and Pure AMOLED Mode
- Interactive crop-style resizing dialog with 4-corner draggable handles and rule-of-thirds composition guidelines.
- Pure `#000000` AMOLED black compliance across the canvas, floating docks, sheets, and system navigation bars.
- 15 native vector background themes with adaptive bounding for both card previews and full-screen editor views.

#### 5. Material 3 Expressive Foundations
- Expressive motion physics using spring curves for smooth transitions and state morphs.
- Floating bottom navigation dock and editor action dock designed for ergonomics and single-handed use.
- Dual-tier toolbar with seamless keyboard avoidance.

---

### In-Place Upgrade Compatibility
- Signed with the permanent repository release keystore valid through 2054.
- In-place upgrades from v1.2 will install seamlessly without signature mismatch errors.

### Package Details
- **Application ID**: `com.zainotes.app`
- **Version Name**: `2.0.0`
- **Version Code**: `20000`
- **Minimum SDK**: Android 8.0 (API 26+)
- **Target SDK**: Android 15 (API 35)
- **License**: GNU Affero General Public License v3.0 (AGPLv3)
