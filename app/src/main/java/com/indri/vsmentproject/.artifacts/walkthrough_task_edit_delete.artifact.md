# Walkthrough - Task Edit & Delete via Tap (Manager)

I have updated the Manager's Task management interface to allow editing and deleting tasks directly by tapping on them, eliminating the need for extra icons and keeping the UI clean.

## Key Changes

### 1. Reusable Options Dialog
> [!NOTE]
> Tapping a task card now triggers a clear options dialog instead of opening a screen immediately.

- **Interaction**: When a Manager taps on a task in the monitoring list, a `MaterialAlertDialog` appears with "Edit Tugas" and "Hapus Tugas" options.
- **Confirmation**: Deleting a task now requires an explicit confirmation step ("Apakah Anda yakin?") to prevent accidental data loss.

### 2. Seamless Edit Mode
- **Form Pre-filling**: Selecting "Edit Tugas" opens the existing task form, but with all fields (Task Name, PIC, Deadline) automatically populated with the task's current data.
- **Smart Logic**: The "Simpan" button intelligently detects whether it's creating a new task or updating an existing one based on the internal ID.

### 3. Backend Integration
- **ViewModel Update**: Added a `hapusTugas` method to `TugasViewModel` to handle real-time deletion from Firebase.
- **Adapter Refactoring**: Updated `InnerVillaAdapter` and `WaktuContainerAdapter` to propagate click events from the nested lists back to the Fragment.

## Technical Details
- **TugasFragment.kt**: Implemented `showOpsiTugas`, `konfirmasiHapus`, and refactored `bukaFormInput` into `bukaFormTugas` for better readability.
- **TugasViewModel.kt**: Added Firebase removal logic using absolute paths.
- **Adapters**: Simplified click handling across the adapter hierarchy.

## How to Verify
1. Log in as a **Manager**.
2. Navigate to the **Tugas** tab.
3. Tap on any task in the list.
4. Select **Edit Tugas**, change the task name, and click **Simpan**. Verify the change appears.
5. Tap the task again, select **Hapus Tugas**, and confirm. Verify the task is removed from the list.
