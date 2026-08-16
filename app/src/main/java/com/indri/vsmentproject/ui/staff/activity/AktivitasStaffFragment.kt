package com.indri.vsmentproject.ui.staff.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.FragmentAktivitasStaffBinding
import java.util.*

class AktivitasStaffFragment : Fragment() {

    private var _binding: FragmentAktivitasStaffBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AktivitasAdapter
    private val auth = FirebaseAuth.getInstance()

    private var listTugas = mutableListOf<TugasModel>()
    private var listLaporan = mutableListOf<LaporanModel>()

    private var selectedTimeFilter = "Semua"
    private var selectedTypeFilter = "Semua"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAktivitasStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        getManagerIdAndFetchData()
        setupFilterButtons()

        // SET DEFAULT UI STATE
        updateTimeUI(binding.btnSemuaWaktu)
        updateTypeUI(binding.btnSemuaJenis)
    }

    private fun getManagerIdAndFetchData() {
        val currentUserId = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_USER_MAPPING).child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val managerId = snapshot.child(FirebaseConfig.FIELD_BELONGS_TO_MANAGER).value.toString()
                    if (managerId != "null" && managerId.isNotEmpty()) {
                        fetchData(managerId, currentUserId)
                    } else {
                        fetchData(currentUserId, currentUserId)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchData(managerId: String, currentStaffUid: String) {
        FirebaseConfig.getTaskManagementRef(managerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    listTugas.clear()

                    snapshot.children.forEach { villaSnap ->
                        villaSnap.child("list_tugas").children.forEach { tugasSnap ->
                            val tugas = tugasSnap.getValue(TugasModel::class.java)

                            if (tugas != null &&
                                tugas.staff_id == currentStaffUid &&
                                tugas.status == FirebaseConfig.STATUS_DONE) {

                                tugas.id = tugasSnap.key ?: ""
                                listTugas.add(tugas)
                            }
                        }
                    }
                    applyFilters()
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("DATABASE_ERROR", "Gagal load tugas: ${error.message}")
                }
            })

        FirebaseConfig.getLaporanKerusakanRef(managerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    listLaporan.clear()

                    snapshot.children.forEach { ds ->
                        val laporan = ds.getValue(LaporanModel::class.java)

                        if (laporan != null && laporan.staff_id == currentStaffUid) {
                            laporan.id = ds.key ?: ""
                            listLaporan.add(laporan)
                        }
                    }
                    applyFilters()
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("DATABASE_ERROR", "Gagal load laporan: ${error.message}")
                }
            })
    }

    private fun setupRecyclerView() {
        adapter = AktivitasAdapter(emptyList())
        binding.rvAktivitas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAktivitas.adapter = adapter
    }

    private fun applyFilters() {
        val filteredList = mutableListOf<Any>()

        val tugasFiltered = if (selectedTypeFilter == "Semua" || selectedTypeFilter == "Tugas") {
            listTugas.filter { filterByTime(it.created_at) }
        } else emptyList()

        val laporanFiltered = if (selectedTypeFilter == "Semua" || selectedTypeFilter == "Laporan") {
            listLaporan.filter { filterByTime(it.created_at) }
        } else emptyList()

        filteredList.addAll(tugasFiltered)
        filteredList.addAll(laporanFiltered)

        val sortedList = filteredList.sortedByDescending {
            when (it) {
                is TugasModel -> it.created_at
                is LaporanModel -> it.created_at
                else -> 0L
            }
        }

        adapter.updateData(sortedList)
    }

    private fun filterByTime(timestamp: Long): Boolean {
        if (selectedTimeFilter == "Semua") return true
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return when (selectedTimeFilter) {
            "Hari Ini" -> timestamp >= calendar.timeInMillis
            "Minggu Ini" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                timestamp >= calendar.timeInMillis
            }
            "Bulan Ini" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                timestamp >= calendar.timeInMillis
            }
            else -> true
        }
    }

    private fun setupFilterButtons() {
        val timeButtons = mapOf(
            binding.btnSemuaWaktu to "Semua",
            binding.btnHariIni to "Hari Ini",
            binding.btnMingguIni to "Minggu Ini",
            binding.btnBulanIni to "Bulan Ini"
        )
        timeButtons.forEach { (btn, filter) ->
            btn.setOnClickListener {
                selectedTimeFilter = filter
                updateTimeUI(btn)
                applyFilters()
            }
        }

        val typeButtons = mapOf(
            binding.btnSemuaJenis to "Semua",
            binding.btnTugas to "Tugas",
            binding.btnLaporan to "Laporan"
        )
        typeButtons.forEach { (btn, filter) ->
            btn.setOnClickListener {
                selectedTypeFilter = filter
                updateTypeUI(btn)
                applyFilters()
            }
        }
    }

    private fun updateTimeUI(selected: Button) {
        listOf(binding.btnSemuaWaktu, binding.btnHariIni, binding.btnMingguIni, binding.btnBulanIni).forEach {
            it.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            it.setTextColor(Color.BLACK)
        }
        selected.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C64756"))
        selected.setTextColor(Color.WHITE)
    }

    private fun updateTypeUI(selected: Button) {
        listOf(binding.btnSemuaJenis, binding.btnTugas, binding.btnLaporan).forEach {
            it.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
        }
        selected.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF1CC"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}