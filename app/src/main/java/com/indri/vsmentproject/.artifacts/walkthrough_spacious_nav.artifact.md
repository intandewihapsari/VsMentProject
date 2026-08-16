# Walkthrough - Spacious Staff Bottom Navigation Fix

I have significantly increased the vertical space in the Staff bottom navigation bar to eliminate the cramped feeling and ensure icons and labels have plenty of room.

## Key Fixes

### 1. Expanded Navigation Real Estate
> [!IMPORTANT]
> The bottom navigation bar height has been increased to **100dp** (from 85dp), providing a very spacious layout that prevents any visual collision between elements.

- **Vertical Separation**: Set `itemPaddingTop="18dp"` and `itemPaddingBottom="12dp"` inside the `BottomNavigationView`. This creates a clear vertical gap between the icon and its text label.
- **Balanced Icon Size**: Reduced the icon size slightly to **20dp**. This makes the icons look cleaner and gives more prominence to the text labels below them, improving overall readability.

### 2. Symmetrical Layout Synchronization
- **Spacer Sync**: Updated the bottom spacer `View` to match the new **100dp** bar height, ensuring that fragment content ends perfectly where the navigation bar begins.
- **Siren FAB Calibration**: Adjusted the `translationY` to **50dp** to keep the siren button perfectly centered on the top edge of the taller navigation bar.

## Visual Improvements
- **Breathability**: The "Home", "Tugas", "Aktivitas", and "Profile" labels now sit comfortably below their icons with no overlapping.
- **Clarity**: Even with larger system fonts, the menu items should remain legible and un-cramped.

## How to Verify
1. Log in as a **Staff** user.
2. Observe the bottom navigation bar; it should feel much taller and less "busy".
3. Check the text labels under the icons to ensure they have clear white space above and below them.
4. Switch between tabs to verify that the top header (which remains synchronized with the Manager view) still works perfectly.
