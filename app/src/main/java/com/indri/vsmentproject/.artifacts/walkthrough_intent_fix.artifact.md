# Walkthrough - Fixing Permission Denial & Navigation Reliability

I have implemented a definitive fix for the "Permission Denial" crash and improved the reliability of the Siren FAB navigation.

## Key Fixes

### 1. Intent Visibility (Manifest Queries)
> [!IMPORTANT]
> Modern Android versions require explicit declaration of which system intents your app intends to interact with.

- **AndroidManifest.xml**: Added a `<queries>` block for `IMAGE_CAPTURE`. This ensures the system allows our app to see and launch camera applications, preventing the security exception seen in the previous error logs.

### 2. Mandatory Runtime Permission
- **Reasoning**: Many device manufacturers (e.g., Huawei, Samsung) require the `CAMERA` permission to be granted at runtime even if we are using an external system camera app via an intent.
- **Implementation**: Both `LaporanStaffFragment` and `UploadBuktiTugasFragment` now perform a mandatory runtime permission check. If the permission is missing, the app will ask for it and automatically proceed to open the camera once granted.

### 3. Reliable Siren FAB Navigation
- **Issue**: Previously, clicking the "Siren" button while already on the report screen wouldn't do anything because the menu selection wouldn't change.
- **Fix**: Updated `StaffActivity` to **force replace** the fragment and re-trigger the automatic camera logic every time the Siren button is clicked. This ensures a consistent "Click to Snap" experience.

## Technical Improvements
- **LaporanStaffFragment.kt**: Integrated `requestPermissionLauncher` and `ContextCompat` checks.
- **UploadBuktiTugasFragment.kt**: Added identical safety guards for task evidence.
- **StaffActivity.kt**: Refined `setupFab()` for guaranteed navigation.

## How to Verify
1. **Permission Test**: Uninstall the app or clear its data. Open the app as **Staff** and click the **Siren FAB**. You should see the standard Android permission dialog. Allow it, and the camera should open instantly.
2. **Reselection Test**: Capture one photo for a report. Click the **Siren FAB** again. The camera should open immediately for the second photo.
3. **Task Test**: Go to any pending task and click **Upload Bukti**. Verify the camera opens reliably.
