# Walkthrough - Villa Task Progress PDF Export

I have implemented the PDF export feature for the "Villa Task Progress" view. Managers can now download a professional summary of all tasks for a specific date, complete with evidence photos.

## Key Features

### 1. Professional Progress Summary
> [!IMPORTANT]
> The exported PDF provides a detailed table of tasks, showing exactly what was done, who did it, and the final status.

- **Task Table**: A clean, grey-header table listing:
    - **Tugas**: The description of the task.
    - **PIC Staff**: The name of the staff member responsible.
    - **Status**: Clearly color-coded in the PDF (**GREEN** for Done, **RED** for Pending).
- **Villa Info**: Displays the Villa Name and the specific Deadline Date at the top of every report.

### 2. Full Photo Evidence Integration
> [!TIP]
> Just like the report feature, this export includes **all unique evidence photos** captured by the staff for the tasks on that day.

- **Grid Layout**: Photos are arranged in a proportional 2-column grid.
- **Smart Scaling**: Each photo is scaled to fit the page while maintaining its original quality and aspect ratio.
- **Auto-Pagination**: If there are many tasks or photos, the system automatically creates **Page 2, 3, etc.**, ensuring no data is lost.

### 3. Background Performance
- **Non-Blocking**: The PDF generation happens entirely in the background. The app remains responsive, and you'll see a Toast notification once the file is ready in your **Downloads** folder.

## Technical Details
- **DeadlineAdapter.kt**:
    - Integrated Kotlin Coroutines for safe background processing.
    - Implemented a custom PDF Canvas drawing engine with dynamic height calculation.
    - Uses Glide for high-performance synchronous bitmap fetching.
- **Storage**: Files are named descriptively: `Progres_[VillaName]_[Date]_[Timestamp].pdf`.

## How to Verify
1. Log in as **Manager**.
2. Go to **Progres Detail** and select a Villa.
3. Select a **Date** (Deadline) that has completed tasks and photos.
4. In the popup, click the **Download icon** (Blue background button).
5. Wait for the message *"PDF Progres berhasil diunduh!"*.
6. Open your **Downloads** folder to view the complete task progress report.
