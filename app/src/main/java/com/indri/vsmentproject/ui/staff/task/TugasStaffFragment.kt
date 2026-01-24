package com.indri.vsmentproject.ui.staff.task

import android.graphics.Typeface
import android.os.Bundle
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
    private val binding get() = _binding!!

    private lateinit var villaAdapter: VillaTugasAdapter
    private lateinit var dbRef: DatabaseReference

    private var listTugasFull = mutableListOf<TugasModel>()
    private var currentFilter = "Seluruh Tugas"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTugasStaffBinding.inflate(inflater, container, false)
        return binding.root
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
        binding.rvVillaTugas.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = villaAdapter
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener { text ->
            filterData(text.toString(), currentFilter)
        }
    }

    private fun setupFilterTabs() {
        val tabs = listOf(binding.tabAll, binding.tabPending, binding.tabSelesai)
        tabs.forEach { tab ->
            tab.setOnClickListener {
                // UI Update: Reset Gaya Tab
                tabs.forEach {
                    it.setBackgroundResource(0)
                    it.setTypeface(null, Typeface.NORMAL)
                }

                // UI Update: Highlight Tab Terpilih
                tab.setBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.myWhite))
                tab.setTypeface(null, Typeface.BOLD)

                currentFilter = (it as TextView).text.toString()
                filterData(binding.etSearch.text.toString(), currentFilter)
            }
        }
    }

    private fun loadTugasFromFirebase() {
        // 1. Ambil ID Staff yang sedang login (Contoh dari SharedPreferences)
        val sharedPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        val currentStaffId = sharedPref.getString("staff_id", "") ?: ""

        // 2. Gunakan ID tersebut untuk query, bukan lagi NIM manual
        if (currentStaffId.isNotEmpty()) {
            dbRef.orderByChild("worker_id").equalTo(currentStaffId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        listTugasFull.clear()
                        for (data in snapshot.children) {
                            val tugas = data.getValue(TugasModel::class.java)
                            tugas?.let {
                                it.id = data.key ?: ""
                                listTugasFull.add(it)
                            }
                        }
                        filterData(binding.etSearch.text.toString(), currentFilter)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(context, "ID Staff tidak ditemukan!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun filterData(query: String, filterStatus: String) {
        // Filter berdasarkan pencarian nama tugas atau nama villa
        var filteredList = listTugasFull.filter {
            it.tugas.contains(query, ignoreCase = true) || it.villa_nama.contains(query, ignoreCase = true)
        }

        // Filter berdasarkan status tab
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
        // Toggle status: jika sudah selesai jadi pending, jika pending jadi selesai
        val newStatus = if (tugas.status == "selesai") "pending" else "selesai"
        dbRef.child(tugas.id).child("status").setValue(newStatus)
            .addOnFailureListener {
                Toast.makeText(context, "Gagal update status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToLaporanKerusakan(tugas: TugasModel) {
        Toast.makeText(context, "Melaporkan: ${tugas.tugas}", Toast.LENGTH_SHORT).show()
        // Navigasi ke LaporanFragment bisa ditambahkan di sini
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}