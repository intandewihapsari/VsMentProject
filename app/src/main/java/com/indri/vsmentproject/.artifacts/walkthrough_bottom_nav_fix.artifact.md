# Walkthrough - Enhanced Bottom Navigation & Layout Stability

I have overhauled the Staff interface to provide a more spacious bottom navigation bar, preventing icon and text collisions while ensuring a consistent header experience matching the Manager role.

## Key Improvements

### 1. Spacious Bottom Navigation
> [!IMPORTANT]
> The bottom bar height has been increased to **110dp** to provide ample vertical space for both icons and their respective text labels.

- **Layout Update**: Modified `activity_staff.xml` to set a tall `BottomAppBar` and `BottomNavigationView`.
- **Vertical Spacing**: Adjusted `itemPaddingTop` (16dp) and `itemPaddingBottom` (20dp) within the `BottomNavigationView`. This pushes the icons and text further apart, eliminating the collision issue.
- **Siren FAB Position**: Re-centered the large "Siren Ring" (90dp) and anchored it correctly to the top of the taller bar using a precise `translationY`.

### 2. Header Synchronization
- **Activity Header**: Re-implemented the `layoutHeader` (80dp) in `StaffActivity`, identical to the Manager's header.
- **Dynamic Titles**: Restored the logic in `StaffActivity.kt` to update the top title bar whenever you switch tabs (Home, Tugas, etc.).
- **Clean UI**: Verified that individual fragments no longer have internal titles, preventing any "doubled up" headers.

### 3. Visual & Functional Stability
- **Clipping Disabled**: Ensured `clipChildren="false"` is set on all parent containers so the FAB can float elegantly without being cut off.
- **Back Navigation**: Maintained the smart back-button logic (double-tap to exit, sub-fragment popping).

## Verification Results
- **Visual Harmony**: ✅ All icons and text labels are fully visible with clear separation.
- **Proportions**: ✅ The taller bottom bar feels balanced with the top header.
- **Build Status**: ✅ Success.

## How to Verify
1. Log in as **Staff**.
2. Look at the bottom navigation: "Home", "Tugas", "Aktivitas", and "Profile" should now have plenty of vertical space.
3. Verify that the pink Siren button sits comfortably in the center.
4. Switch tabs and observe the top header title updating correctly without any doubling.
