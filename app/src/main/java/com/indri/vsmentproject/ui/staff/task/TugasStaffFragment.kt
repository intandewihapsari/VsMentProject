package com.indri.vsmentproject.ui.staff.task

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.FragmentTugasStaffBinding

class TugasStaffFragment : Fragment() {

    private var _binding: FragmentTugasStaffBinding? = null
    // Gunakan backing property yang aman
    private val binding get() = _binding

    private lateinit var villaAdapter: VillaTugasAdapter
    private lateinit var dbRef: DatabaseReference

    private var listTugasFull = mutableListOf<TugasModel>()
    private var currentFilter = "Seluruh Tugas"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentTugasStaffBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbRef = FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_TASK_MANAGEMENT)

        setupRecyclerView()
        setupFilterTabs()
        setupSearchBar()
        loadTugasFromFirebase()
    }

    private fun setupRecyclerView() {
        villaAdapter = VillaTugasAdapter(
            onDoneClick = { tugas -> updateStatusTugas(tugas) },
            onReportClick = { tugas -> goToLaporanKerusakan(tugas) }
        )
        binding?.rvVillaTugas?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = villaAdapter
        }
    }

    private fun setupSearchBar() {
        binding?.etSearch?.addTextChangedListener { text ->
            filterData(text.toString(), currentFilter)
        }
    }

    private fun setupFilterTabs() {
        // Gunakan binding? agar aman
        binding?.let { b ->
            val tabs = listOf(b.tabAll, b.tabPending, b.tabSelesai)
            tabs.forEach { tab ->
                tab.setOnClickListener {
                    tabs.forEach {
                        it.setBackgroundResource(0)
                        it.setTypeface(null, Typeface.NORMAL)
                    }

                    tab.setBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.myWhite))
                    tab.setTypeface(null, Typeface.BOLD)

                    currentFilter = (it as TextView).text.toString()
                    filterData(b.etSearch.text.toString(), currentFilter)
                }
            }
        }
    }

    private fun loadTugasFromFirebase() {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        val currentStaffId = sharedPref.getString("staff_id", "") ?: ""

        if (currentStaffId.isNotEmpty()) {
            dbRef.orderByChild("worker_id").equalTo(currentStaffId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // CEK NULL DISINI (Penting!)
                        if (_binding == null) return

                        listTugasFull.clear()
                        for (data in snapshot.children) {
                            val tugas = data.getValue(TugasModel::class.java)
                            tugas?.let {
                                it.id = data.key ?: ""
                                listTugasFull.add(it)
                            }
                        }
                        // Gunakan safe call binding?.etSearch
                        filterData(binding?.etSearch?.text.toString(), currentFilter)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", error.message)
                    }
                })
        }
    }

    private fun filterData(query: String, filterStatus: String) {
        var filteredList = listTugasFull.filter {
            it.tugas.contains(query, ignoreCase = true) || it.villa_nama.contains(query, ignoreCase = true)
        }

        when (filterStatus) {
            "Pending" -> filteredList = filteredList.filter { it.status == "pending" }
            "Selesai" -> filteredList = filteredList.filter { it.status == "selesai" }
        }

        groupTugasByVilla(filteredList)
    }

    private fun groupTugasByVilla(list: List<TugasModel>) {
        val grouped = list.groupBy { it.villa_id }.map { (villaId, tugasList) ->
            VillaTugasGroup(
                villa_id = villaId,
                namaVilla = tugasList.firstOrNull()?.villa_nama ?: "Tanpa Nama",
                totalTugas = tugasList.size,
                tugasSelesai = tugasList.count { it.status == "selesai" },
                listTugas = tugasList
            )
        }
        villaAdapter.setData(grouped)
    }

    private fun updateStatusTugas(tugas: TugasModel) {
        val newStatus = if (tugas.status == "selesai") "pending" else "selesai"

        // Gunakan map untuk update beberapa field sekaligus
        val updates = HashMap<String, Any>()
        updates["status"] = newStatus

        // Jika dicentang selesai, catat waktu selesainya sekarang
        if (newStatus == "selesai") {
            updates["completed_at"] = System.currentTimeMillis()
        } else {
            updates["completed_at"] = 0L // Reset jika dikembalikan ke pending
        }

        dbRef.child(tugas.id).updateChildren(updates)
            .addOnFailureListener {
                if (_binding != null) {
                    Toast.makeText(context, "Gagal update status", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToLaporanKerusakan(tugas: TugasModel) {
        Toast.makeText(context, "Melaporkan: ${tugas.tugas}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}