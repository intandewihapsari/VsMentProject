package com.indri.vsmentproject.data.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    var uid: String = "",
    var nama: String = "",
    var email: String = "",
    var role: String = "",          // manager / staff
    var posisi: String = "",
    var telepon: String = "",
    var foto_profil: String = "",
    var manager_id: String = "",
    var status: String = "aktif"
) : Parcelable