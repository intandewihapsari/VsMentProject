package com.indri.vsmentproject.ui.staff.activity

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.indri.vsmentproject.R
import com.indri.vsmentproject.ui.manager.task.progressVilla.FotoGridAdapter
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

        // Set default UI state
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
        adapter = AktivitasAdapter(emptyList()) { item ->
            showDetailPopup(item)
        }
        binding.rvAktivitas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAktivitas.adapter = adapter
    }

    private fun showDetailPopup(item: Any) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_detail_tanggal)

        val tvTanggal = dialog.findViewById<TextView>(R.id.tvTanggal)
        val rvTugas = dialog.findViewById<RecyclerView>(R.id.rvTugas)
        val rvFoto = dialog.findViewById<RecyclerView>(R.id.rvFoto)
        val layoutEmptyFoto = dialog.findViewById<View>(R.id.layoutEmptyFoto)
        val btnDownload = dialog.findViewById<View>(R.id.btnDownload)
        val btnClose = dialog.findViewById<View>(R.id.btnClose)

        btnDownload.visibility = View.GONE
        btnClose.setOnClickListener { dialog.dismiss() }

        when (item) {
            is TugasModel -> {
                tvTanggal.text = "Detail Tugas Selesai"
                rvTugas.layoutManager = LinearLayoutManager(requireContext())
                rvTugas.adapter = com.indri.vsmentproject.ui.manager.task.progressVilla.TugasSimpleAdapter(listOf(item))
                
                if (item.bukti_foto?.isNotEmpty() == true) {
                    layoutEmptyFoto.visibility = View.GONE
                    rvFoto.visibility = View.VISIBLE
                    rvFoto.layoutManager = GridLayoutManager(requireContext(), 3)
                    val fotoGrid = FotoGridAdapter()
                    rvFoto.adapter = fotoGrid
                    fotoGrid.setData(item.bukti_foto ?: emptyList())
                } else {
                    layoutEmptyFoto.visibility = View.VISIBLE
                    rvFoto.visibility = View.GONE
                }
            }
            is LaporanModel -> {
                tvTanggal.text = "Detail Laporan ${item.tipe_laporan.uppercase()}"
                rvTugas.layoutManager = LinearLayoutManager(requireContext())
                val pseudoTask = TugasModel(tugas = item.nama_barang, status = "selesai", deskripsi = item.deskripsi)
                rvTugas.adapter = com.indri.vsmentproject.ui.manager.task.progressVilla.TugasSimpleAdapter(listOf(pseudoTask))

                if (item.bukti_foto.isNotEmpty()) {
                    layoutEmptyFoto.visibility = View.GONE
                    rvFoto.visibility = View.VISIBLE
                    rvFoto.layoutManager = GridLayoutManager(requireContext(), 3)
                    val fotoGrid = FotoGridAdapter()
                    rvFoto.adapter = fotoGrid
                    fotoGrid.setData(item.bukti_foto)
                } else {
                    layoutEmptyFoto.visibility = View.VISIBLE
                    rvFoto.visibility = View.GONE
                }
            }
        }

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
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
            it.setTextColor(Color.BLACK)
        }
        selected.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF1CC"))
        selected.setTextColor(Color.parseColor("#805B00"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}