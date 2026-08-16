# Walkthrough - Matching Staff Activity Header with Manager Activity

I have updated the `StaffActivity` layout and logic to include a top header bar that matches the visual style of the `ManagerActivity`. This provides a consistent "branded" feel across both user roles in the application.

## Key Changes

### 1. Unified Layout Structure
> [!NOTE]
> The `StaffActivity` now shares the same header bar design as the `ManagerActivity`, including the background transparency and text styling.

- **Layout Update**: Modified `activity_staff.xml` to include a `RelativeLayout` header at the top.
- **Dynamic Title**: Added a `tvTitlePage` TextView that acts as the central label for the currently active screen.

### 2. Intelligent Title Synchronization
- **Navigation Logic**: Updated `StaffActivity.kt` to dynamically change the header text based on the selected bottom navigation tab:
    - 🏠 **Home** -> "Home"
    - 📋 **Tugas** -> "Tugas"
    - 📢 **Laporan** -> "Laporan"
    - 📈 **Aktivitas** -> "Aktivitas"
    - 👤 **Profile** -> "Profile"

## Visual Verification
- **Header Placement**: The header is positioned correctly at the top, without overlapping fragment content.
- **Color & Font**: Uses `@color/myBlueDark` and bold 18sp text to match the Manager's interface exactly.

## How to Verify
1. Log in as a **Staff** member.
2. Observe the new "Home" title at the top of the screen.
3. Switch between tabs (Tugas, Laporan, etc.) and verify that the title updates instantly to reflect the current section.
4. Go to the **Profile** tab and verify it now has the "Profile" header, making it consistent with the Manager's profile view.
