package com.indri.vsmentproject.UI.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.indri.vsmentproject.Data.Model.ProfileModel
import com.indri.vsmentproject.Data.Model.ProfileSummary

class ProfileViewModel : ViewModel() {
    private val _summary = MutableLiveData<ProfileSummary>()
    val summary: LiveData<ProfileSummary> = _summary

    private val _managerData = MutableLiveData<ProfileModel>()
    val managerData: LiveData<ProfileModel> = _managerData

    fun getData() {
        val rootRef = FirebaseDatabase.getInstance().reference

        rootRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Hitung Statistik
                val vCount = snapshot.child("master_data/villa_list").childrenCount.toInt()
                val sCount = snapshot.child("master_data/staff").childrenCount.toInt()

                var lPending = 0
                snapshot.child("operational/laporan_barang").children.forEach {
                    if (it.child("status_laporan").value.toString() != "selesai") {
                        lPending++
                    }
                }

                // Isi Model ProfileSummary
                _summary.postValue(ProfileSummary(vCount, sCount, lPending))

                // Isi Model ProfileModel (Ambil info identitas)
                val manager = snapshot.child("master_data/manager_info").getValue(ProfileModel::class.java)
                if (manager != null) _managerData.postValue(manager)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}