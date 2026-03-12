package com.indri.vsmentproject.ui.staff.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.FragmentDashboardStaffBinding
import com.indri.vsmentproject.ui.staff.task.TugasChildAdapter

class DashboardStaffFragment : Fragment() {

    private var _binding: FragmentDashboardStaffBinding? = null
    private val binding get() = _binding

    private lateinit var rootRef: DatabaseReference
    private val listTugasHome = mutableListOf<TugasModel>()
    private var staffId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDashboardStaffBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootRef = FirebaseDatabase.getInstance().reference

        val sharedPref = activity?.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        staffId = sharedPref?.getString("staff_id", "") ?: ""

        setupRecyclerView()

        view.postDelayed({
            if (isAdded) {
                loadDashboardData()
                loadJadwalPenting()
            }
        }, 500)
    }

    private fun setupRecyclerView() {
        binding?.rvTugasHome?.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private fun loadDashboardData() {
        if (staffId.isEmpty()) return

        rootRef.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .orderByChild("worker_id").equalTo(staffId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null || !isAdded) return

                    listTugasHome.clear()
                    var total = 0
                    var selesai = 0
                    var pending = 0

                    try {
                        for (data in snapshot.children) {
                            val tugas = data.getValue(TugasModel::class.java)
                            tugas?.let {
                                it.id = data.key ?: ""
                                total++
                                if (it.status == "selesai") selesai++ else {
                                    pending++
                                    listTugasHome.add(it)
                                }
                            }
                        }
                        updateStatUI(total, selesai, pending)

                        val sortedPending = listTugasHome.sortedByDescending { it.created_at }
                        binding?.rvTugasHome?.adapter = TugasChildAdapter(
                            sortedPending,
                            onDone = { t -> updateStatusTugas(t) },
                            onReport = { /* Navigasi report */ }
                        )
                    } catch (e: Exception) {
                        Log.e("DASHBOARD_ERROR", "Parsing error: ${e.message}")
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadJadwalPenting() {
        if (staffId.isEmpty()) return

        // 1. PASANG CLICK LISTENER DULU (Biar responsif)
        binding?.cardJadwalPenting?.setOnClickListener {
            val intent = Intent(requireContext(), JadwalPentingActivity::class.java)
            startActivity(intent)
        }

        val query: Query = rootRef.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .orderByChild("worker_id")
            .equalTo(staffId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null || !isAdded) return

                val listTugasPending = mutableListOf<TugasModel>()
                for (data in snapshot.children) {
                    val tugas = data.getValue(TugasModel::class.java)
                    if (tugas?.status == "pending") {
                        listTugasPending.add(tugas!!)
                    }
                }

                if (listTugasPending.isNotEmpty()) {
                    // Cari yang paling urgent (High Priority & Deadline terdekat)
                    val tugasUrgent = listTugasPending.sortedWith(
                        compareByDescending<TugasModel> { it.prioritas == "High" }
                            .thenByDescending { it.prioritas == "Medium" }
                            .thenBy { it.deadline }
                    ).first()

                    // Tampilkan ke Card
                    binding?.apply {
                        tvJudulJadwal.text = tugasUrgent.tugas

                        val rawDeadline = tugasUrgent.deadline ?: ""
                        if (rawDeadline.contains(" ")) {
                            val parts = rawDeadline.split(" ")
                            tvTime.text = parts[1]
                            tvDate.text = formatTgl(parts[0])
                        } else {
                            tvTime.text = "08:00"
                            tvDate.text = formatTgl(rawDeadline)
                        }
                    }
                } else {
                    binding?.tvJudulJadwal?.text = "Semua tugas selesai! ✨"
                    binding?.tvTime?.text = "--:--"
                    binding?.tvDate?.text = "Justin hebat!"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun updateStatUI(total: Int, selesai: Int, pending: Int) {
        binding?.let { b ->
            b.itemTotal.tvCount.text = total.toString()
            b.itemDone.tvCount.text = selesai.toString()
            b.itemPending.tvCount.text = pending.toString()
        }
    }

    private fun updateStatusTugas(tugas: TugasModel) {
        val updates = mapOf("status" to "selesai", "completed_at" to System.currentTimeMillis())
        rootRef.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(tugas.id).updateChildren(updates)
    }

    private fun formatTgl(dateStr: String): String {
        return try {
            // Parser untuk membaca format dari Firebase (yyyy-MM-dd)
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            // Formatter untuk mengubah ke tampilan cantik (Contoh: 25 Jan 2026)
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

            val date = parser.parse(dateStr)
            if (date != null) formatter.format(date) else dateStr
        } catch (e: Exception) {
            // Jika format salah, kembalikan teks aslinya agar aplikasi tidak crash
            dateStr
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}