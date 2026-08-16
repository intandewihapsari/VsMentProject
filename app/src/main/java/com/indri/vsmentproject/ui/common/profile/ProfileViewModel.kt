package com.indri.vsmentproject.ui.common.profile

import androidx.lifecycle.*
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.user.ProfileSummary
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.data.utils.Resource
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileViewModel : ViewModel() {

    private val _userData = MutableLiveData<UserModel>()
    val userData: LiveData<UserModel> = _userData

    private val _summary = MutableLiveData<ProfileSummary>()
    val summary: LiveData<ProfileSummary> = _summary

    private val _updateStatus = MutableLiveData<Resource<String>>()
    val updateStatus: LiveData<Resource<String>> = _updateStatus

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    fun getData() {
        val uid = auth.currentUser?.uid ?: return

        db.child(FirebaseConfig.PATH_USER_MAPPING).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(mappingSnapshot: DataSnapshot) {
                    if (!mappingSnapshot.exists()) return

                    val role = mappingSnapshot.child(FirebaseConfig.FIELD_ROLE).value.toString()
                    val belongsToManager = mappingSnapshot.child(FirebaseConfig.FIELD_BELONGS_TO_MANAGER).value.toString()

                    val profileRef = if (role == "manager") {
                        FirebaseConfig.getManagerRef(uid).child(FirebaseConfig.CHILD_MANAGER_PROFILE)
                    } else {
                        FirebaseConfig.getStaffsRef(belongsToManager).child(uid)
                    }

                    profileRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val user = userSnapshot.getValue(UserModel::class.java) ?: return
                            user.uid = uid
                            user.manager_id = if (role == "manager") uid else belongsToManager
                            _userData.postValue(user)

                            fetchSnapshotForStats(user.manager_id, user)
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchSnapshotForStats(managerId: String, user: UserModel) {
        FirebaseConfig.getManagerRef(managerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    calculateStats(snapshot, user)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun calculateStats(managerSnapshot: DataSnapshot, user: UserModel) {
        if (user.role == "manager") {
            val totalVilla = managerSnapshot.child(FirebaseConfig.CHILD_VILLAS).childrenCount.toInt()
            val totalStaff = managerSnapshot.child(FirebaseConfig.CHILD_STAFFS).childrenCount.toInt()
            val totalLaporanPending = managerSnapshot.child(FirebaseConfig.CHILD_LAPORAN_KERUSAKAN).children.count {
                it.child(FirebaseConfig.FIELD_STATUS).getValue(String::class.java)?.lowercase() == FirebaseConfig.STATUS_PENDING
            }
            _summary.postValue(ProfileSummary(totalVilla, totalStaff, totalLaporanPending))
        } else {
            var totalBeres = 0
            var sisaTugas = 0
            var totalLaporan = 0
            val uid = user.uid

            // Hitung Tugas (Struktur flat task_management)
            managerSnapshot.child(FirebaseConfig.CHILD_TASK_MANAGEMENT).children.forEach { tugas ->
                val staffIdInTask = tugas.child("staff_id").getValue(String::class.java) ?: ""
                val statusTugas = tugas.child(FirebaseConfig.FIELD_STATUS).getValue(String::class.java) ?: ""

                if (staffIdInTask == uid) {
                    if (statusTugas.equals(FirebaseConfig.STATUS_DONE, ignoreCase = true)) totalBeres++ else sisaTugas++
                }
            }

            // Hitung Laporan
            managerSnapshot.child(FirebaseConfig.CHILD_LAPORAN_KERUSAKAN).children.forEach { report ->
                val staffIdInReport = report.child("staff_id").getValue(String::class.java) ?: ""
                if (staffIdInReport == uid) totalLaporan++
            }

            _summary.postValue(ProfileSummary(totalBeres, totalLaporan, sisaTugas))
        }
    }

    fun updateFullProfile(name: String, phone: String, email: String, photoUrl: String? = null, onResult: ((String) -> Unit)? = null) {
        val currentUser = _userData.value ?: return
        val firebaseUser = auth.currentUser

        if (firebaseUser == null) {
            _updateStatus.postValue(Resource.Error("Sesi login kedaluwarsa, silakan login ulang"))
            return
        }

        _updateStatus.postValue(Resource.Loading())

        // 1. Update Display Name di Firebase Auth
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .apply { photoUrl?.let { setPhotoUri(Uri.parse(it)) } }
            .build()

        firebaseUser.updateProfile(profileUpdates).addOnCompleteListener { authProfileTask ->
            if (authProfileTask.isSuccessful) {
                
                // 2. Update Email di Firebase Auth (jika berbeda)
                if (email != firebaseUser.email) {
                    firebaseUser.updateEmail(email).addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            updateDatabaseProfile(currentUser, name, phone, email, photoUrl, onResult)
                        } else {
                            _updateStatus.postValue(Resource.Error("Gagal update Email Auth: ${emailTask.exception?.message}"))
                        }
                    }
                } else {
                    updateDatabaseProfile(currentUser, name, phone, email, photoUrl, onResult)
                }
            } else {
                _updateStatus.postValue(Resource.Error("Gagal update profil Auth: ${authProfileTask.exception?.message}"))
            }
        }
    }

    private fun updateDatabaseProfile(
        currentUser: UserModel,
        name: String,
        phone: String,
        email: String,
        photoUrl: String?,
        onResult: ((String) -> Unit)?
    ) {
        val profileRef = if (currentUser.role == "manager") {
            FirebaseConfig.getManagerRef(currentUser.uid).child(FirebaseConfig.CHILD_MANAGER_PROFILE)
        } else {
            FirebaseConfig.getStaffsRef(currentUser.manager_id).child(currentUser.uid)
        }

        val updates = mutableMapOf<String, Any>("nama" to name, "telepon" to phone, "email" to email)
        photoUrl?.let { updates["foto_profil"] = it }

        profileRef.updateChildren(updates).addOnSuccessListener {
            currentUser.nama = name
            currentUser.telepon = phone
            currentUser.email = email
            photoUrl?.let { currentUser.foto_profil = it }
            _userData.postValue(currentUser)

            _updateStatus.postValue(Resource.Success("Profil berhasil diperbarui"))
            onResult?.invoke("Profil berhasil diperbarui")
        }.addOnFailureListener { dbError ->
            _updateStatus.postValue(Resource.Error("Gagal simpan ke DB: ${dbError.message}"))
        }
    }
}
