package com.indri.vsmentproject.ui.staff.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.FragmentDashboardStaffBinding
import com.indri.vsmentproject.ui.staff.task.TugasChildAdapter

class DashboardStaffFragment : Fragment() {

    private var _binding: FragmentDashboardStaffBinding? = null
    private val binding get() = _binding

    private lateinit var rootRef: DatabaseReference
    private val listTugasHome = mutableListOf<TugasModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDashboardStaffBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootRef = FirebaseDatabase.getInstance().reference

        setupRecyclerView()

        // Gunakan Handler untuk menunda sedikit proses agar transisi Activity selesai dulu
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
        val sharedPref = activity?.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val staffId = sharedPref?.getString("staff_id", "") ?: ""

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
                            onReport = { /* Navigasi */ }
                        )
                    } catch (e: Exception) {
                        Log.e("DASHBOARD_ERROR", "Parsing error: ${e.message}")
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadJadwalPenting() {
        rootRef.child(FirebaseConfig.PATH_NOTIFIKASI)
            .limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null || !isAdded || !snapshot.exists()) return

                    val data = snapshot.children.firstOrNull()
                    val notif = data?.getValue(NotifikasiModel::class.java)
                    notif?.let {
                        val jam = it.waktu.split(" ").lastOrNull() ?: "--:--"
                        binding?.tvTime?.text = jam
                        // Tambahkan binding judul/villa di sini
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
        val updates = mapOf(
            "status" to "selesai",
            "completed_at" to System.currentTimeMillis()
        )
        rootRef.child(FirebaseConfig.PATH_TASK_MANAGEMENT).child(tugas.id).updateChildren(updates)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}