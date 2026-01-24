package com.indri.vsmentproject.ui.common.profile

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.user.*
import com.indri.vsmentproject.data.utils.FirebaseConfig

class ProfileViewModel : ViewModel() {
    private val _userData = MutableLiveData<UserModel>()
    val userData: LiveData<UserModel> = _userData

    private val _summary = MutableLiveData<ProfileSummary>()
    val summary: LiveData<ProfileSummary> = _summary

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    fun getData() {
        val uid = auth.currentUser?.uid ?: return
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user = snapshot.child(FirebaseConfig.PATH_MANAGERS).child(uid).getValue(UserModel::class.java)
                if (user == null) user = snapshot.child(FirebaseConfig.PATH_STAFFS).child(uid).getValue(UserModel::class.java)

                user?.let {
                    _userData.postValue(it)
                    calculateStats(snapshot, it)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun calculateStats(snapshot: DataSnapshot, user: UserModel) {
        if (user.role == "manager") {
            val vCount = snapshot.child(FirebaseConfig.PATH_VILLAS).childrenCount.toInt()
            val sCount = snapshot.child(FirebaseConfig.PATH_STAFFS).childrenCount.toInt()
            val lPending = snapshot.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN).children.count {
                it.child("status").value.toString() != "selesai"
            }
            _summary.postValue(ProfileSummary(vCount, sCount, lPending))
        } else {
            var done = 0
            var proc = 0
            snapshot.child(FirebaseConfig.PATH_LAPORAN_KERUSAKAN).children.forEach {
                if (it.child("uid_staff").value == user.uid) {
                    if (it.child("status").value == "selesai") done++ else proc++
                }
            }
            _summary.postValue(ProfileSummary(done, proc, (done + proc)))
        }
    }

    // Fungsi Update Lengkap (Database + Auth)
    fun updateFullProfile(name: String, phone: String, email: String, photoUrl: String? = null, onResult: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val path = if (_userData.value?.role == "manager") FirebaseConfig.PATH_MANAGERS else FirebaseConfig.PATH_STAFFS

        val updates = mutableMapOf<String, Any>("nama" to name, "telepon" to phone, "email" to email)
        photoUrl?.let { updates["foto_profil"] = it }

        // 1. Update Realtime Database
        db.child(path).child(uid).updateChildren(updates).addOnSuccessListener {
            // 2. Update Email di Firebase Auth (Jika berbeda)
            if (email != auth.currentUser?.email) {
                auth.currentUser?.updateEmail(email)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) onResult("Profil & Email diperbarui")
                    else onResult("Profil diperbarui, Email gagal (perlu login ulang)")
                }
            } else {
                onResult("Profil berhasil diperbarui")
            }
        }.addOnFailureListener { onResult("Gagal memperbarui database") }
    }
}