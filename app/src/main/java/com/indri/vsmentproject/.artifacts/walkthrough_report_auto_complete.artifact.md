# Walkthrough - Auto-Complete Task on Report Submission

I have implemented a "Linked Workflow" feature that automatically marks a task as **Selesai** (Done) when a staff member submits a report directly from that task.

## Key Changes

### 1. Data Context Passing
> [!NOTE]
> When a staff member clicks the "Report" icon on a task in the dashboard, the app now "remembers" which task it came from.

- **DashboardStaffFragment.kt**: Updated the navigation logic to pass the `TASK_ID` and `VILLA_ID` to the reporting screen as background arguments.

### 2. Atomic Batch Update
> [!IMPORTANT]
> To ensure data integrity, the report creation and the task completion happen at the exact same time in the database.

- **LaporanStaffFragment.kt**: Updated the `saveToFirebase` method.
    - If a linked task is detected, it prepares a batch update.
    - **Report Node**: Creates the new report record with captured photos.
    - **Task Node**: Updates the linked task status to `"selesai"`, sets the completion timestamp, and attaches the report photos as evidence.

### 3. Workflow Efficiency
- Staff no longer need to "Upload Bukti" separately if they have already submitted a "Lapor Kendala" for that specific task. The report photos serve as the completion evidence.

## Verification Results
- **Functional Sync**: ✅ Verified. Submitting a report from a task dashboard item immediately strikes through that task and marks it as done.
- **Standalone Safety**: ✅ Verified. Submitting a direct report (via Siren FAB) does NOT affect any tasks.
- **Build Status**: ✅ Success.

## How to Test
1. Log in as **Staff**.
2. Go to the **Dashboard** and find a **Pending Task**.
3. Tap the **Report Icon** on that task.
4. Snap 3 photos and submit the report.
5. You will see a message: *"Laporan Berhasil & Tugas Selesai!"*
6. Verify that the task in the dashboard is now marked as done.
