# Tasks - Comprehensive Audit & Stability Fixes

- [x] **Core Configuration**
    - [x] Force Light Mode in `VsMentApp.kt`
    - [x] Lock Portrait Orientation in `AndroidManifest.xml`
- [x] **Memory & Lifecycle (ViewModel Cleanup)**
    - [x] Refactor `DashboardViewModel.kt` to clear listeners
    - [x] Refactor `TugasViewModel.kt` to clear listeners
- [x] **Null Safety & Error Handling (Fragment Refinement)**
    - [x] Clean up `TugasFragment.kt` (NPE prevention)
    - [x] Clean up `LaporanStaffFragment.kt` (Async safety)
- [x] **Validation & Navigation**
    - [x] Refine `LoginActivity.kt` validation
    - [x] Implement `BackPressedCallback` in `StaffActivity.kt`
- [x] **Final Verification**
    - [x] Run `./gradlew assembleDebug`
