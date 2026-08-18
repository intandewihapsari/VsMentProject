# Walkthrough - Including Images in PDF Report

I have enhanced the PDF export feature to include the report's evidence photos. This ensures that the generated document provides a complete and professional visual summary of the reported villa issues.

## Key Enhancements

### 1. Professional PDF Layout with Images
> [!IMPORTANT]
> The PDF document now automatically embeds up to the first **two captured photos** to provide visual context alongside the textual data.

- **Smart Scaling**: Images are automatically scaled to fit within the A4 page margins while preserving their original aspect ratio.
- **Auto-Placement**: The system intelligently places images between the report description and the manager's notes.
- **Optimized Content**: To keep the PDF file size manageable and ensure it fits on a single page, the most relevant photos are prioritized.

### 2. Smooth Background Processing
> [!TIP]
> Generating a PDF with images requires downloading them from the cloud. I have implemented background threading to keep the app responsive.

- **Non-blocking UI**: The image fetching and PDF creation now happen on a background thread using Kotlin Coroutines. You will no longer see any "freezing" while the document is being prepared.
- **Glide Integration**: Leveraged Glide's advanced caching and bitmap decoding capabilities to ensure high-quality image rendering in the final document.

## Technical Details
- **DetailLaporanActivity.kt**:
    - Migrated `generatePdfReport` to a `suspend` function.
    - Implemented synchronous bitmap fetching using `Glide.submit().get()`.
    - Added complex `Canvas` drawing logic for bitmaps with bounds checking.
- **Storage**: Reports continue to be saved in the device's public **Downloads** folder for easy access and sharing.

## How to Verify
1. Log in as **Manager**.
2. Open a report that contains photos.
3. Edit the **Catatan Manager**.
4. Click **Simpan Catatan**.
5. Wait for the notification *"Catatan disimpan & PDF diunduh!"*.
6. Open your **Downloads** folder and view the new PDF. You should now see the villa photos included in the document!
