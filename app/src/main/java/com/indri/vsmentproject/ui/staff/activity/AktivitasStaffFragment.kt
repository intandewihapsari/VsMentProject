package com.indri.vsmentproject.ui.staff.activity

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.report.LaporanModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.FragmentAktivitasStaffBinding
import java.text.SimpleDateFormat
import java.util.*

class AktivitasStaffFragment : Fragment() {

    private var _binding: FragmentAktivitasStaffBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AktivitasAdapter
    private val database = FirebaseDatabase.getInstance()

    private val listTugas = mutableListOf<TugasModel>()
    private val listLaporan = mutableListOf<LaporanModel>()

    private var selectedTimeFilter = "Semua"
    private var selectedTypeFilter = "Semua"

    private var staffId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAktivitasStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        staffId = sharedPref.getString("staff_id", "") ?: ""

        setupRecyclerView()
        setupFilterButtons()
        fetchData()

        updateTimeFilterUI(binding.btnSemuaWaktu)
        updateTypeFilterUI(binding.btnSemuaJenis)
    }

    private fun setupRecyclerView() {
        adapter = AktivitasAdapter(emptyList())
        binding.rvAktivitas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAktivitas.adapter = adapter
    }

    private fun fetchData() {

        // 🔥 TASK SELESAI
        database.getReference(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .orderByChild("status")
            .equalTo("selesai")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return

                    listTugas.clear()

                    for (ds in snapshot.children) {
                        val tugas = ds.getValue(TugasModel::class.java)
                        tugas?.let {
                            it.id = ds.key ?: ""

                            if (it.staff_id == staffId) {
                                listTugas.add(it)
                            }
                        }
                    }

                    Log.d("TASK_DEBUG", listTugas.toString())
                    applyFilters()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", error.message)
                }
            })

        // 🔥 LAPORAN
        database.getReference(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return

                    listLaporan.clear()

                    for (ds in snapshot.children) {
                        val laporan = ds.getValue(LaporanModel::class.java)
                        laporan?.let {
                            it.id = ds.key ?: ""

                            if (it.staff_id == staffId) {
                                listLaporan.add(it)
                            }
                        }
                    }

                    Log.d("LAPORAN_DEBUG", listLaporan.toString())
                    applyFilters()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", error.message)
                }
            })
    }

    private fun applyFilters() {
        if (_binding == null) return

        val finalList = mutableListOf<Any>()

        val tugasFiltered = if (selectedTypeFilter == "Semua" || selectedTypeFilter == "Tugas") {
            listTugas.filter { filterByTime(it.completed_at) }
        } else emptyList()

        val laporanFiltered = if (selectedTypeFilter == "Semua" || selectedTypeFilter == "Laporan") {
            listLaporan.filter {
                val timestamp = parseLaporanTime(it.waktu_lapor)
                filterByTime(timestamp)
            }
        } else emptyList()

        finalList.addAll(tugasFiltered)
        finalList.addAll(laporanFiltered)

        val sortedList = finalList.sortedByDescending {
            when (it) {
                is TugasModel -> it.completed_at
                is LaporanModel -> parseLaporanTime(it.waktu_lapor)
                else -> 0L
            }
        }

        adapter.updateData(sortedList)
    }

    private fun filterByTime(timestamp: Long): Boolean {
        if (selectedTimeFilter == "Semua") return true

        // 🔥 FIX: timestamp kosong tetap tampil
        if (timestamp <= 0L) return true

        val now = Calendar.getInstance()
        val itemDate = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when (selectedTimeFilter) {
            "Hari Ini" -> {
                now.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == itemDate.get(Calendar.DAY_OF_YEAR)
            }
            "Minggu Ini" -> {
                now.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR) &&
                        now.get(Calendar.WEEK_OF_YEAR) == itemDate.get(Calendar.WEEK_OF_YEAR)
            }
            "Bulan Ini" -> {
                now.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR) &&
                        now.get(Calendar.MONTH) == itemDate.get(Calendar.MONTH)
            }
            else -> true
        }
    }

    private fun parseLaporanTime(waktu: String?): Long {
        if (waktu.isNullOrEmpty()) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            sdf.parse(waktu)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun setupFilterButtons() {
        val timeMap = mapOf(
            binding.btnSemuaWaktu to "Semua",
            binding.btnHariIni to "Hari Ini",
            binding.btnMingguIni to "Minggu Ini",
            binding.btnBulanIni to "Bulan Ini"
        )

        timeMap.forEach { (btn, value) ->
            btn.setOnClickListener {
                selectedTimeFilter = value
                updateTimeFilterUI(btn)
                applyFilters()
            }
        }

        val typeMap = mapOf(
            binding.btnSemuaJenis to "Semua",
            binding.btnTugas to "Tugas",
            binding.btnLaporan to "Laporan"
        )

        typeMap.forEach { (btn, value) ->
            btn.setOnClickListener {
                selectedTypeFilter = value
                updateTypeFilterUI(btn)
                applyFilters()
            }
        }
    }

    private fun updateTimeFilterUI(selected: Button) {
        val buttons = listOf(binding.btnSemuaWaktu, binding.btnHariIni, binding.btnMingguIni, binding.btnBulanIni)
        buttons.forEach {
            it.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            it.setTextColor(Color.BLACK)
        }
        selected.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C64756"))
        selected.setTextColor(Color.WHITE)
    }

    private fun updateTypeFilterUI(selected: Button) {
        val buttons = listOf(binding.btnSemuaJenis, binding.btnTugas, binding.btnLaporan)
        buttons.forEach {
            it.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            it.setTextColor(Color.BLACK)
        }
        selected.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF1CC"))
        selected.setTextColor(Color.BLACK)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}