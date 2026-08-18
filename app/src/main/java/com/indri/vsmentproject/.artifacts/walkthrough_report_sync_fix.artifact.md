# Walkthrough - Dashboard & Report Filter Synchronization

I have synchronized the filtering and sorting logic between the Manager's Dashboard and the Laporan screen. This ensures that the numbers you see on the dashboard always match the data displayed in the reports list.

## Key Fixes

### 1. Unified "Pending" Logic
> [!IMPORTANT]
> Previously, the Dashboard counted both **Pending** and **Proses** reports as active issues, but the Laporan filter only showed **Pending** items.

- **Sync**: Updated the "Pending" tab in the Laporan screen to show all unfinished reports (both *Pending* and *Proses* statuses).
- **Consistency**: Now, when your Dashboard says there are 5 reports, clicking the Pending tab will show exactly those 5 items.

### 2. High-Precision Sorting
> [!TIP]
> String-based date sorting can sometimes be unreliable if reports arrive in the same minute.

- **Timestamp Sorting**: Switched the list sorting to use the internal `created_at` timestamp.
- **Accuracy**: Reports are now perfectly sorted chronologically, with the most recent entry always appearing at the very top.

## Technical Details
- **LaporanFragment.kt**:
    - Refactored `applyFilter` to handle the consolidated unfinished status.
    - Optimized sorting logic to avoid expensive date parsing on every list update.
- **UI States**: Verified that "Empty States" still display correctly when no reports match the cumulative filters (Villa + Status).

## How to Verify
1. Log in as **Manager**.
2. Note the count for **Total Rusak** or **Hilang** on the Dashboard.
3. Go to the **Laporan** tab and click **Pending**.
4. Verify that the number of reports in the list (including those in progress) matches the Dashboard total.
5. Verify that the most recently submitted report is at the top of the list.
