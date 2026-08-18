# Implementation Plan - Enhancing Staff Experience: Notifications & Activity Details

This plan aims to make the "Jadwal Penting" (Important Schedule) openable and automatically mark notifications as read. It also adds a read-only detail view for "Aktivitas Staff" (Staff Activities).

## User Review Required

> [!IMPORTANT]
> **Real-time Read Status**: Opening the "Jadwal Penting" from the dashboard will now automatically mark the latest notification as read in Firebase. This will be reflected in the Manager's dashboard as a "greyed out" (read) notification.
>
> **Read-Only Activity Details**: In the Activity tab, staff can now tap on any finished task or report to see a detail popup (including photos and descriptions). No editing or deleting will be allowed in this view.

## Proposed Changes

### [Staff Dashboard]

#### [MODIFY] [DashboardStaffFragment.kt](file:///D:/KULIAH/VsMentProject/app/src/main/java/com/indri/vsmentproject/ui/staff/dashboard/DashboardStaffFragment.kt)
- Add a click listener to `cardJadwalPenting`.
- When clicked:
    - Call a helper function to mark the latest notification as read in Firebase.
    - Open `JadwalPentingActivity`.

#### [MODIFY] [JadwalPentingActivity.kt](file:///D:/KULIAH/VsMentProject/app/src/main/java/com/indri/vsmentproject/ui/staff/dashboard/JadwalPentingActivity.kt)
- Fix the Firebase reference (`dbRef`) to correctly point to the manager-scoped `operational/notifikasi` node.
- Ensure the manager's UID is retrieved (via `belongs_to_manager` mapping).

### [Staff Activity]

#### [MODIFY] [AktivitasAdapter.kt](file:///D:/KULIAH/VsMentProject/app/src/main/java/com/indri/vsmentproject/ui/staff/activity/AktivitasAdapter.kt)
- Add a constructor parameter `onItemClick: (Any) -> Unit`.
- Set a click listener on `root` to trigger this callback with either the `TugasModel` or `LaporanModel`.

#### [MODIFY] [AktivitasStaffFragment.kt](file:///D:/KULIAH/VsMentProject/app/src/main/java/com/indri/vsmentproject/ui/staff/activity/AktivitasStaffFragment.kt)
- Update `setupRecyclerView` to provide the item click listener.
- Implement `showDetailPopup(item: Any)`:
    - For `TugasModel`: Show a dialog with the task name, villa, staff, completion time, and evidence photos.
    - For `LaporanModel`: Show a dialog with the report type, item name, description, reporter, time, and evidence photos.
    - Both will be **read-only** (no edit/delete buttons).

## Verification Plan

### Manual Verification
1. **Notification Sync**:
    - As Staff, click the "Jadwal Penting" card.
    - Verify that the notification icon/status changes to "Read" in the database.
    - Verify as Manager that the corresponding dashboard notification becomes greyed out (if applicable) or indicates it has been read.
2. **Activity Preview**:
    - As Staff, go to the "Aktivitas" tab.
    - Tap on a finished task or a report.
    - Verify a popup appears showing all details and photos.
    - Verify there are no "Save", "Delete", or "Edit" options in this popup.
