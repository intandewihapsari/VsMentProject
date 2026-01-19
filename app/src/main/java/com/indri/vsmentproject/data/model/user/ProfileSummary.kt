package com.indri.vsmentproject.data.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileSummary(
    val totalVilla: Int = 0,
    val totalStaff: Int = 0,
    val totalLaporanPending: Int = 0
) : Parcelable