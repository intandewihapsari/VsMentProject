package com.indri.vsmentproject.data.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileSummary(
    val first: Int = 0,
    val second: Int = 0,
    val third: Int = 0
) : Parcelable