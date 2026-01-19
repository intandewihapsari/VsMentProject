package com.indri.vsmentproject.ui.manager.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.data.model.user.ProfileSummary
import com.indri.vsmentproject.data.utils.FirebaseConfig

class ProfileViewModel : ViewModel() {
    private val _summary = MutableLiveData<ProfileSummary>()
    val summary: LiveData<ProfileSummary> = _summary

    private val _userData = MutableLiveData<UserModel>()
    val userData: LiveData<UserModel> = _userData

    fun getData() {
        val rootRef = FirebaseDatabase.getInstance().reference
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        rootRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 1. Hitung Statistik menggunakan Config
                val vCount = snapshot.child(FirebaseConfig.PATH_VILLAS).childrenCount.toInt()

                // Hitung Staff yang ada di dalam folder users
                val sCount = snapshot.child(FirebaseConfig.PATH_USERS).children.count {
                    it.child(FirebaseConfig.FIELD_ROLE).value.toString() == "staff"
                }

                var lPending = 0
                snapshot.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN).children.forEach {
                    if (it.child(FirebaseConfig.FIELD_STATUS).value.toString() != "selesai") {
                        lPending++
                    }
                }
                _summary.postValue(ProfileSummary(vCount, sCount, lPending))

                // 2. Ambil Identitas User yang login
                val user = snapshot.child(FirebaseConfig.PATH_USERS).child(currentUid).getValue(UserModel::class.java)
                user?.let { _userData.postValue(it) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}