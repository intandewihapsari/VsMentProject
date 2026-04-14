package com.indri.vsmentproject.ui.staff.task

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.model.task.VillaTugasGroup
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.FragmentTugasStaffBinding
import com.indri.vsmentproject.ui.staff.report.LaporanStaffFragment

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

    private fun loadTugasFromFirebase() {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", 0)
        val staffId = sharedPref.getString("staff_id", "") ?: ""

        if (staffId.isNotEmpty()) {
            dbRef.orderByChild("staff_id").equalTo(staffId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (_binding == null) return
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
                    override fun onCancelled(error: DatabaseError) {}
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
        val updates = HashMap<String, Any>()
        updates["status"] = newStatus
        updates["completed_at"] = if (newStatus == "selesai") System.currentTimeMillis() else 0L

        dbRef.child(tugas.id).updateChildren(updates)
    }

    private fun goToLaporanKerusakan(tugas: TugasModel) {
        val fragment = LaporanStaffFragment()
        val bundle = Bundle().apply {
            putString("VILLA_NAMA", tugas.villa_nama)
            putString("RUANGAN_NAMA", tugas.ruangan)
            putString("BARANG_NAMA", tugas.tugas)
        }
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupFilterTabs() {
        val tabs = listOf(binding.tabAll, binding.tabPending, binding.tabSelesai)
        tabs.forEach { tab ->
            tab.setOnClickListener {
                tabs.forEach {
                    it.setBackgroundResource(0)
                    it.setTypeface(null, Typeface.NORMAL)
                }
                tab.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.myWhite))
                tab.setTypeface(null, Typeface.BOLD)
                currentFilter = tab.text.toString()
                filterData(binding.etSearch.text.toString(), currentFilter)
            }
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener { text ->
            filterData(text.toString(), currentFilter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}