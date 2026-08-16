# Walkthrough - Fix Staff Bottom Navigation & FAB Cradle

I have fixed the visual issue where the Floating Action Button (FAB) was overlapping incorrectly with the Bottom Navigation labels. By properly utilizing the `BottomAppBar` cradle feature, the UI now has a clean, professional cutout for the FAB.

## Key Changes

### 1. Corrected Component Hierarchy
> [!IMPORTANT]
> The FAB must be a direct child of the `CoordinatorLayout` to interact correctly with the `BottomAppBar`.

- **Refactored `activity_staff.xml`**: Removed the `FrameLayout` wrapper around the FAB. The `FloatingActionButton` is now anchored directly to the `BottomAppBar`.
- **Enabled FAB Cradle**: Configured `app:fabCradleMargin`, `app:fabCradleRoundedCornerRadius`, and `app:fabCradleVerticalOffset` on the `BottomAppBar` to create a smooth curved cutout.

### 2. Transparent Navigation Background
- Set the `BottomNavigationView` background to `@android:color/transparent`. This ensures that the white background of the `BottomAppBar` (with its cradle shape) is what the user sees, preventing the navigation bar from "covering" the FAB.

### 3. Balanced Spacing
- The `bottom_nav_menu_staff.xml` already includes a placeholder item in the center. Combined with the new layout structure, this ensures that the "Home", "Tugas", "Aktivitas", and "Profile" labels are perfectly distributed on either side of the FAB.

## Visual Verification
- **FAB Cradle**: The FAB now sits in a rounded "dip" in the bottom bar, preventing it from obscuring the menu text.
- **Menu Labels**: All labels are clearly visible and appropriately spaced.
- **Icon Integrity**: The "Siren" icon is now fully visible and centered.

## How to Verify
1. Open the app and log in as a **Staff** member.
2. Check the bottom navigation bar. You should see a clean, curved cutout surrounding the pink siren button.
3. Verify that clicking "Home", "Tugas", "Aktivitas", and "Profile" still navigates to the correct sections.
4. Verify that the center FAB still triggers the "Laporan" action (as per the `StaffActivity.kt` logic).
