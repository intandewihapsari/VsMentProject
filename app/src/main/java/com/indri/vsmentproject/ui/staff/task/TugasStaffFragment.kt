package com.indri.vsmentproject.ui.staff.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.FragmentTugasStaffBinding

class TugasStaffFragment : Fragment() {

    private var _binding: FragmentTugasStaffBinding? = null
    private val binding get() = _binding!!

    private lateinit var villaAdapter: VillaTugasAdapter
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    private var listTugasFull = mutableListOf<TugasModel>()
    private var currentFilter = "Seluruh Tugas"
    private var managerId: String? = null

    // Variabel penampung dinamis untuk data staff yang sedang login
    private var currentStaffNama: String = ""
    private var currentCustomStaffId: String = "" // Menampung STF_001, STF_002, dll.

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTugasStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterTabs()
        setupSearchBar()
        fetchManagerIdAndStaffProfile()
    }

    private fun setupRecyclerView() {
        villaAdapter = VillaTugasAdapter(
            onDoneClick = { tugas -> updateStatusTugas(tugas) },
            onReportClick = { tugas -> goToUploadBukti(tugas) }
        )
        binding.rvVillaTugas.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = villaAdapter
        }
    }

    private fun fetchManagerIdAndStaffProfile() {
        val uid = auth.currentUser?.uid ?: return

        // Step 1: Dapatkan Manager ID dari user_mapping
        db.child(FirebaseConfig.PATH_USER_MAPPING).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    managerId = snapshot.child(FirebaseConfig.FIELD_BELONGS_TO_MANAGER).value.toString()
                    if (managerId != "null" && managerId!!.isNotEmpty()) {

                        // Step 2: Cari data staff di master_data/staffs secara dinamis
                        db.child(FirebaseConfig.PATH_VILLA_MANAGEMENT)
                            .child(managerId!!)
                            .child(FirebaseConfig.CHILD_MASTER_DATA)
                            .child(FirebaseConfig.CHILD_STAFFS)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(staffsSnapshot: DataSnapshot) {

                                    // Cari anak node yang memiliki field uid sesuai dengan user yang login
                                    for (staffSnap in staffsSnapshot.children) {
                                        val staffUidInDb = staffSnap.child("uid").value?.toString()

                                        if (staffUidInDb == uid) {
                                            // Ambil KEY node-nya (misal: "STF_002" atau "W7lUve...")
                                            currentCustomStaffId = staffSnap.key ?: ""
                                            currentStaffNama = staffSnap.child("nama").value?.toString() ?: ""
                                            break
                                        }
                                    }

                                    // Jalankan loading tugas dengan data identitas asli yang dinamis
                                    loadTugasFromFirebase(managerId!!, uid, currentCustomStaffId, currentStaffNama)
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadTugasFromFirebase(mId: String, staffUid: String, customStaffId: String, staffNama: String) {
        // Step 3: Query masuk ke operational/task_management
        FirebaseConfig.getTaskManagementRef(mId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    listTugasFull.clear()

                    snapshot.children.forEach { villaSnap ->
                        villaSnap.child("list_tugas").children.forEach { tugasSnap ->
                            val tugas = tugasSnap.getValue(TugasModel::class.java)

                            if (tugas != null) {
                                // PERBAIKAN TOTAL: Pengecekan dinamis tanpa hardcode string lagi!
                                val isMyUid = tugas.staff_id == staffUid
                                val isMyCustomId = customStaffId.isNotEmpty() && tugas.staff_id == customStaffId
                                val isMyName = staffNama.isNotEmpty() && tugas.staff_nama.equals(staffNama, ignoreCase = true)

                                // Jika salah satu kriteria COCOK, maka tugas ini miliknya
                                if (isMyUid || isMyCustomId || isMyName) {
                                    tugas.id = tugasSnap.key ?: ""
                                    tugas.villa_id = villaSnap.key ?: ""
                                    listTugasFull.add(tugas)
                                }
                            }
                        }
                    }
                    filterData(binding.etSearch.text.toString(), currentFilter)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun filterData(query: String, filterStatus: String) {
        var filteredList = listTugasFull.filter {
            it.tugas.contains(query, ignoreCase = true) || it.villa_nama.contains(query, ignoreCase = true)
        }

        when (filterStatus) {
            "Pending" -> filteredList = filteredList.filter { it.status == FirebaseConfig.STATUS_PENDING }
            "Selesai" -> filteredList = filteredList.filter { it.status == FirebaseConfig.STATUS_DONE }
        }
        groupTugasByVilla(filteredList)
    }

    private fun groupTugasByVilla(list: List<TugasModel>) {
        if (_binding == null) return
        val grouped = list.groupBy { it.villa_id }.map { (villaId, tugasList) ->
            VillaTugasGroup(
                villa_id = villaId,
                namaVilla = tugasList.firstOrNull()?.villa_nama ?: "Villa",
                totalTugas = tugasList.size,
                tugasSelesai = tugasList.count { it.status == FirebaseConfig.STATUS_DONE },
                listTugas = tugasList
            )
        }
        villaAdapter.setData(grouped)
    }

    private fun updateStatusTugas(tugas: TugasModel) {
        val mId = managerId ?: return
        val newStatus = if (tugas.status == FirebaseConfig.STATUS_DONE) FirebaseConfig.STATUS_PENDING else FirebaseConfig.STATUS_DONE

        val updates = mapOf(
            "status" to newStatus,
            "completed_at" to if (newStatus == FirebaseConfig.STATUS_DONE) System.currentTimeMillis() else 0L
        )

        FirebaseConfig.getTaskManagementRef(mId)
            .child(tugas.villa_id)
            .child("list_tugas")
            .child(tugas.id)
            .updateChildren(updates)
            .addOnSuccessListener {
                if (_binding != null) {
                    Toast.makeText(context, "Status diperbarui", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToUploadBukti(tugas: TugasModel) {
        val mId = managerId ?: return
        val fragment = UploadBuktiTugasFragment().apply {
            arguments = Bundle().apply {
                putParcelable("TUGAS_DATA", tugas)
                putString("MANAGER_ID", mId)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupFilterTabs() {
        binding.toggleGroupFilter.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                currentFilter = when (checkedId) {
                    R.id.tabPending -> "Pending"
                    R.id.tabSelesai -> "Selesai"
                    else -> "Seluruh Tugas"
                }
                filterData(binding.etSearch.text.toString(), currentFilter)
            }
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener { text -> filterData(text.toString(), currentFilter) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}