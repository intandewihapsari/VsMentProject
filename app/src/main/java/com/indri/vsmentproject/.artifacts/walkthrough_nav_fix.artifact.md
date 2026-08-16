# Walkthrough - Fix Cut-off Navigation Icons

I have adjusted the Staff bottom navigation bar to ensure that all icons and labels are fully visible and not clipped at the bottom of the screen.

## Key Changes

### 1. Optimized Navigation Height
> [!TIP]
> Setting the `BottomNavigationView` to fill its parent ensures that the icons have maximum vertical space within the navigation bar.

- **Layout Fix**: Changed `BottomNavigationView` height from `wrap_content` to `match_parent`.
- **Background Transparency**: Removed the white background from the `BottomNavigationView` and set it to transparent. This allows the underlying `BottomAppBar` to handle the background color, preventing unintended clipping from nested containers.

### 2. Precise Vertical Alignment
- **Spacer Sync**: Updated the bottom spacer `View` in the main layout to match the `BottomAppBar` height exactly (80dp). This ensures that fragment content is correctly padded and doesn't hide behind or "push" into the navigation bar.
- **Elevation Cleanup**: Removed redundant elevation from the navigation view, delegating all shadow/depth effects to the parent `BottomAppBar`.

## Visual Verification
- **Icon Visibility**: All icons (Home, Tugas, Aktivitas, Profile) are now centered vertically within the white bar.
- **Label Clarity**: Text labels under the icons are fully rendered without being cut off by the physical edge of the screen or the bar's padding.

## How to Verify
1. Log in as a **Staff** user.
2. Look at the bottom navigation bar.
3. Confirm that the text "Home", "Tugas", etc., is fully legible and has clear space below it.
4. Verify that the center Siren FAB is still centered and functional.
