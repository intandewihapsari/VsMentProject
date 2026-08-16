# Walkthrough - Unified Header UI for Staff

I have overhauled the Staff interface to use a centralized header bar managed by the `StaffActivity`. This ensures that all screens for the Staff role have a consistent title bar that matches the Manager's interface, while eliminating redundant titles from individual fragments.

## Key Changes

### 1. Consistent Activity Header
> [!IMPORTANT]
> The `StaffActivity` now manages the top title bar and progress banner, ensuring a unified look and feel across all tabs.

- **Unified Layout**: Updated `activity_staff.xml` to include the same `layoutHeader` structure used in `ManagerActivity`.
- **Progress Parity**: Included the `progressBarBanner` in the Staff header to match the Manager's visual style perfectly.

### 2. Clean Fragment UI
- **Removed Redundancy**: Deleted the internal "Home" title from `DashboardStaffFragment` and the "Aktivitas" title from `AktivitasStaffFragment`.
- **Optimized Spacing**: Fragments now start immediately below the activity header, removing the "doubled header" effect and providing more screen space for content.

### 3. Synchronized Navigation
- **Dynamic Updates**: The `StaffActivity.kt` now intelligently updates the header title whenever the user switches tabs in the bottom navigation.
- **Consistent Transitions**: Transitioning between "Tugas", "Laporan", "Aktivitas", and "Profile" now feels seamless as the title bar remains fixed while only the content changes.

## Visual Comparison
- **Before**: Each fragment had its own title, often causing two titles to appear or inconsistent styling.
- **After**: A single, bold title bar at the top with a subtle progress indicator, matching the high-end look of the Manager's dashboard.

## Verification Results
- **Layout Integrity**: ✅ Verified. No overlapping elements or broken constraints.
- **Title Logic**: ✅ Verified. All navigation actions update the title correctly.
- **Build Status**: ✅ Success. No compile errors or missing ViewBinding references.
