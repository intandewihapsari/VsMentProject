# Walkthrough - Header Cleanup and Profile Synchronization

I have removed the horizontal header lines (progress bars) from both the Manager and Staff interfaces and unified the profile logic to ensure a consistent experience across all user roles.

## Key Changes

### 1. Header Cleanup
> [!IMPORTANT]
> The horizontal "line" (ProgressBar) at the bottom of the top header has been removed to provide a cleaner, more minimal look.

- **activity_manager.xml**: Removed `progressBarBanner`.
- **activity_staff.xml**: Removed `progressBarBanner`.

### 2. Profile Synchronization
- **ProfileFragment.kt**: Cleaned up commented-out code and ensured that the profile display logic (Name, Position, Role, Photo) is identical for both Manager and Staff.
- **Visual Consistency**: Both roles now use the exact same layout and logic for their profile screens, matching the "branded" feel of the app.

## Verification Results
- **Visual**: The top header bar now only contains the page title, without any distracting lines below it.
- **Functional**: Profil editing, photo updates, and logout functions remain fully operational and synchronized for all roles.
- **Build Status**: ✅ Success.

## How to Verify
1. Log in as a **Manager** or **Staff**.
2. Observe the top header bar; the progress line should no longer be visible.
3. Navigate to the **Profile** tab and verify that the layout and information displayed are consistent and correctly formatted.
