# Zenith Launcher

A complete, minimalist Android launcher inspired by the philosophy, aesthetic balance, and digital-wellbeing principles of **Yin Yang Launcher**.

Zenith replaces device noise and algorithmic distraction with serene typography, intentional pauses, customizable workspaces, and local digital-wellbeing tools.

---

## Key Features

### 1. Minimalist Home Screen
- **Dual Layout Engines**:
  - **Minimal Text Mode**: Pure typographic application list, customizable clock, calm daily reflections, and intentional breathing room.
  - **Classic Icon Mode**: Streamlined app row with high-fidelity vector icons and clean spacing.
- **Zen Clock**:
  - Clean Digital, Typographic Stacked, and Zen Flow word styles.
  - Live seconds/minute updates and localized date formatting.
- **Mindful Reflections**:
  - Curated Stoic and Zen quotes (Marcus Aurelius, Lao Tzu, Seneca).
- **Bottom Navigation & Quick Search**:
  - Fluid transition to full Application Drawer with real-time app filtering.

### 2. Application Drawer & Management
- Discovers installed Android launchable applications via `PackageManager.queryIntentActivities`.
- Real app launching with intentionality guards.
- Long-press contextual management:
  - Add/Remove Favorites
  - Configure Mindful Pause per application
  - Set Daily Time Allowance / Budget
  - Apply Mindful Break (Digital Detox locking)
  - Toggle Focus Mode Whitelist
  - Hide unused or distracting apps

### 3. My Space (Productivity Workspace)
- **Notes**: Capture thoughts with title, body, and timestamps. Saved in persistent local Room database.
- **To-Do Lists**: Manage tasks, toggle completion states, and prioritize actions without notification spam.
- **Calendar Agenda**: Integrates with Android `CalendarContract` when permission is granted, displaying today's and upcoming events.

### 4. Mind Space (Digital Wellbeing Dashboard)
- Real device usage tracking via Android `UsageStatsManager`.
- Today's total screen time in hours and minutes.
- Application-level breakdown with proportional usage bars.
- Clear educational permission onboarding with direct link to Android Usage Access Settings.

### 5. Mindful Pause (Intentional Launch Interstitial)
- Calming breathing exercise before opening designated apps (e.g. social media).
- Inhale / Hold / Release concentric breathing animation with live seconds countdown.
- Reflective prompt: *"Is this intentional, or an impulse?"*
- Unlocked proceed button after deep breath, with prominent "Return Home" option.

### 6. Focus Mode & Pomodoro Timer
- Customizable work intervals (25/5, 45/10, 50/10) with circular progress indicator.
- Permitted application whitelist during focus sessions.
- History log of completed focus intervals recorded locally.

### 7. Mindful Break (Long-Term Restrictions)
- Digital detox locking for 1 day, 3 days, or custom intervals.
- Shows time remaining and the user's personal motivation note.
- Restores focus during exam weeks or work sprints.

### 8. Personalisation & Themes
- Five minimalist palettes:
  - **Yin Yang Mono** (Deep Charcoal & Pure White)
  - **Obsidian Dark** (OLED Pure Black)
  - **Paper Light** (Warm Cream & Dark Ink)
  - **Warm Sand** (Earthy Stone & Natural Tone)
  - **Zen Sage** (Calm Pine & Forest Mint)
- System Default Home App selector shortcut.

---

## Architecture & Engineering
- **Architecture**: MVVM with Repository Pattern, Kotlin Coroutines, and Flow.
- **UI Framework**: Jetpack Compose with Material 3.
- **Database**: Room Database with KSP (`ZenithDatabase`, `NoteDao`, `TaskDao`, `FocusSessionDao`, `AppRestrictionDao`, `LauncherPreferencesDao`).
- **Icons**: 100% Native Android Vector Graphics (`VectorDrawable` & Compose `ImageVector`). Strict Zero-Emoji Policy enforced system-wide.
- **Fluid Layouts**: No hardcoded text container dimensions; Scale-Independent Pixels (`sp`) for accessibility typography scaling; dynamic padding and wrapping.

---

## Artifact Persisted
- Build artifact generated at: `${rootDir}/builds/app-debug-v1.0.apk`
