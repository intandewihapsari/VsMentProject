# Walkthrough - Fixing Filter Crash in TugasStaffFragment

I have stabilized the filtering logic in the Staff Tasks screen to prevent force closes when switching between "Pending" and "Selesai" tabs.

## Key Fixes

### 1. Official Toggle Listener
> [!IMPORTANT]
> Manually managing click listeners and background states on a `MaterialButtonToggleGroup` often leads to inconsistent UI states and crashes.

- **Refactor**: Switched from manual `OnClickListener` to the official `addOnButtonCheckedListener`. This ensures that the toggle group handles the selection logic internally and safely.
- **UI Decoupling**: Removed manual background and typeface modifications in code, allowing the XML selector (`selector_filter_tab`) to manage visual states automatically and reliably.

### 2. Context Safety Guards
- **Null Checks**: Added `_binding != null` checks before showing Toasts or updating lists within asynchronous callbacks (like Firebase listeners). This prevents "Force Close" errors if the user navigates away from the screen while a data operation is still processing.

## Technical Details
- **TugasStaffFragment.kt**:
    - Cleaned up imports and removed unused `Typeface`.
    - Implemented safe `Toast` logic in `updateStatusTugas`.
    - Added safety guard in `groupTugasByVilla`.

## Verification Results
- **Filter Switching**: ✅ Verified. Rapidly clicking between "Pending" and "Selesai" no longer causes crashes.
- **Search Consistency**: ✅ Verified. The search bar remains functional and synchronized with the active filter.
- **Build Status**: ✅ Success.

## How to Test
1. Log in as **Staff**.
2. Go to the **Tugas** tab.
3. Rapidly click between **Seluruh Tugas**, **Pending**, and **Selesai**.
4. Verify that the list updates smoothly without the app closing.
