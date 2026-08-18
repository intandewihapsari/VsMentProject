# Walkthrough - Real-time Multi-Photo Camera Capture for Staff

I have transformed the photo acquisition process for Staff members. Instead of picking images from the gallery, staff are now required to take **3 to 5 real-time photos** sequentially for both "Lapor Kendala" (Asset Reporting) and task evidence.

## Key Changes

### 1. Sequential Camera Flow
> [!IMPORTANT]
> To comply with the "real-time" requirement, the gallery option has been removed. Staff must snap photos one by one.

- **Capture Loop**: Staff tap "Ambil Foto", snap a picture, and see it added to a horizontal list. They repeat this until they have at least 3 photos.
- **Visual Feedback**: Each captured photo is displayed in a thumbnail list with a "Remove" button if they want to retake a specific shot.

### 2. Multi-Photo Data Model
- **LaporanModel**: Updated the data structure to include `bukti_foto` (a list of strings) to store all captured evidence URLs in Firebase.
- **Consistency**: Both Task Evidence and Asset Reports now follow the same multi-photo logic.

### 3. Manager Visibility
- **Enhanced Detail View**: Updated the Manager's `DetailLaporanActivity` to include a horizontal scrollable list of all photos submitted by the staff, replacing the previous single-image view.

## Technical Implementation
- **CapturedFotoAdapter**: A new shared adapter for the staff-side preview of local `Uri` objects.
- **Atomic Upload**: Implemented a batch upload logic that waits for all photos to be successfully hosted on **Cloudinary** before writing the final record to the **Firebase Realtime Database**.
- **Lifecycle Safety**: Added null-checks and `isAdded` verifications to handle asynchronous network callbacks safely.

## How to Test
1. **As Staff (Lapor Kendala)**:
    - Open the report screen.
    - Click **Ambil Foto Realtime**. The camera will open.
    - Take 3 different photos. Observe them appearing in the horizontal list.
    - Click **Laporkan**.
2. **As Staff (Tugas)**:
    - Open a pending task.
    - Snap 3-5 photos of your work.
    - Click **Submit Bukti**.
3. **As Manager**:
    - Open the report details.
    - You should see all 3-5 photos in a scrollable horizontal bar.
