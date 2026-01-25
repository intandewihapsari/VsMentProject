package com.indri.vsmentproject.ui.staff.activity

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
        fetchData()
        setupFilterButtons()

        // Inisialisasi tampilan UI tombol agar sesuai dengan variabel inisial
        updateTimeFilterUI(binding.btnSemuaWaktu)
        updateTypeFilterUI(binding.btnSemuaJenis)
    }

    private fun setupRecyclerView() {
        adapter = AktivitasAdapter(emptyList())
        binding.rvAktivitas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAktivitas.adapter = adapter
    }

    private fun fetchData() {
        // Ambil Tugas yang sudah selesai
        database.getReference(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .orderByChild(FirebaseConfig.FIELD_STATUS)
            .equalTo(FirebaseConfig.STATUS_DONE)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    listTugas.clear()
                    snapshot.children.forEach { ds ->
                        ds.getValue(TugasModel::class.java)?.let {
                            it.id = ds.key ?: ""
                            listTugas.add(it)
                        }
                    }
                    applyFilters()
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", error.message)
                }
            })

        // Ambil Laporan
        database.getReference(FirebaseConfig.PATH_LAPORAN_KERUSAKAN)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    listLaporan.clear()
                    snapshot.children.forEach { ds ->
                        ds.getValue(LaporanModel::class.java)?.let {
                            it.id = ds.key ?: ""
                            listLaporan.add(it)
                        }
                    }
                    applyFilters()
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", error.message)
                }
            })
    }

    private fun applyFilters() {
        if (_binding == null) return

        val filteredList = mutableListOf<Any>()

        val tugasFiltered = if (selectedTypeFilter == "Semua" || selectedTypeFilter == "Tugas") {
            listTugas.filter { filterByTime(it.completed_at) }
        } else emptyList()

        val laporanFiltered = if (selectedTypeFilter == "Semua" || selectedTypeFilter == "Laporan") {
            listLaporan.filter {
                val timestamp = parseLaporanTime(it.waktu_lapor)
                filterByTime(timestamp)
            }
        } else emptyList()

        filteredList.addAll(tugasFiltered)
        filteredList.addAll(laporanFiltered)

        val sortedList = filteredList.sortedByDescending {
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
        if (timestamp <= 0L) return false

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

    private fun parseLaporanTime(waktuLapor: String): Long {
        if (waktuLapor.isBlank()) return 0L
        return try {
            val format = if (waktuLapor.contains("-")) "yyyy-MM-dd HH:mm" else "yyyy/MM/dd HH:mm"
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            sdf.parse(waktuLapor)?.time ?: 0L
        } catch (e: Exception) { 0L }
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