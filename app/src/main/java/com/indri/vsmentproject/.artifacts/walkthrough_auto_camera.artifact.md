# Walkthrough - Automatic Camera Launch for Staff Features

I have successfully updated the Staff interface to automatically trigger the camera when entering the reporting or task evidence screens. This streamlines the workflow for staff members by reducing manual button clicks.

## Key Changes

### 1. Instant Capture Flow
> [!IMPORTANT]
> The app now detects if a user is starting a new report or task evidence set. If no photos have been taken yet, it **automatically launches the camera**.

- **Laporan (Siren Button/Menu)**: Clicking the central Siren FAB or the "Laporan" menu now immediately opens the camera app.
- **Task Evidence**: Clicking "Upload Bukti" inside a task now immediately opens the camera app for the first photo.

### 2. Smart Logic to Avoid Loops
- **Conditional Trigger**: The automatic launch only occurs when the `capturedPhotos` list is empty.
- **User Control**: Once the first photo is taken and the user is back on the form, the camera will NOT reopen automatically. This allows staff to fill in descriptions or manually decide when to take subsequent photos (up to 5 total).

## Visual & Functional Verification
- **FAB Click**: ✅ App navigates to Laporan AND camera opens instantly.
- **Task Click**: ✅ App navigates to Upload Bukti AND camera opens instantly.
- **Sequential Stability**: ✅ Verified. Returning to the form after 1+ photos does not trigger the camera again.

## How to Test
1. Log in as **Staff**.
2. Tap the **Pink Siren Button** in the bottom navigation. Verify the camera opens immediately.
3. Take one photo. Verify you are returned to the form and the camera does **not** open again automatically.
4. Go to any **Pending Task** and click **Upload Bukti**. Verify the camera opens immediately.
