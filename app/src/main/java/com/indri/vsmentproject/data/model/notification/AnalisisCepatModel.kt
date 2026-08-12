package com.indri.vsmentproject.data.model.notification

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnalisisCepatModel(
    var progressTugas: String = "0%", // Menampung "66%", "100%", dst
    ) : Parcelable