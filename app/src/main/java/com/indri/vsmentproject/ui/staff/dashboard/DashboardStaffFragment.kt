package com.indri.vsmentproject.ui.staff.dashboard

import android.content.Context
import android.content.Intent
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
import com.indri.vsmentproject.ui.staff.dashboard.JadwalPentingActivity
import com.indri.vsmentproject.ui.staff.report.LaporanStaffFragment
import com.indri.vsmentproject.ui.staff.task.TugasChildAdapter
import java.text.SimpleDateFormat
import java.util.*

class DashboardStaffFragment : Fragment() {

    private var _binding: FragmentDashboardStaffBinding? = null
    private val binding get() = _binding

    private lateinit var rootRef: DatabaseReference
    private val listTugasHome = mutableListOf<TugasModel>()
    private var staffId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardStaffBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootRef = FirebaseDatabase.getInstance().reference

        val sharedPref = requireActivity()
            .getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        staffId = sharedPref.getString("staff_id", "") ?: ""

        setupRecyclerView()

        view.postDelayed({
            if (isAdded) {
                loadDashboardData()     // 🔹 TASK (bawah)
                loadJadwalPenting()     // 🔹 NOTIF (card atas)
            }
        }, 300)
    }

    private fun setupRecyclerView() {
        binding?.rvTugasHome?.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    // =========================
    // 🔹 TASK LIST (BAWAH)
    // =========================
    private fun loadDashboardData() {
        if (staffId.isEmpty()) {
            Log.e("ERROR", "Staff ID kosong!")
            return
        }

        Log.d("STAFF_ID", "Current Staff: $staffId")

        rootRef.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (_binding == null || !isAdded) return

                    listTugasHome.clear()

                    var total = 0
                    var selesai = 0
                    var pending = 0

                    for (data in snapshot.children) {
                        val tugas = data.getValue(TugasModel::class.java)

                        tugas?.let {
                            it.id = data.key ?: ""

                            Log.d("TASK_DEBUG", "Task staff_id: ${it.staff_id}")

                            if (it.staff_id == staffId) {

                                total++

                                if (it.status == "selesai") {
                                    selesai++
                                } else {
                                    pending++
                                    listTugasHome.add(it)
                                }
                            }
                        }
                    }

                    updateStatUI(total, selesai, pending)

                    val sortedPending =
                        listTugasHome.sortedByDescending { it.created_at }

                    binding?.rvTugasHome?.adapter = TugasChildAdapter(
                        sortedPending,
                        onDone = { t -> updateStatusTugas(t) },
                        onReport = { t -> bukaLaporanDariTugas(t) }
                    )
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
    // =========================
    // 🔥 NOTIFIKASI (CARD ATAS)
    // =========================
    private fun loadJadwalPenting() {
        if (staffId.isEmpty()) return

        // klik card → buka semua notif
        binding?.cardJadwalPenting?.setOnClickListener {
            startActivity(
                Intent(requireContext(), JadwalPentingActivity::class.java)
            )
        }

        rootRef.child(FirebaseConfig.PATH_NOTIFIKASI)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null || !isAdded) return

                    val listNotif = mutableListOf<NotifikasiModel>()

                    for (data in snapshot.children) {
                        val notif = data.getValue(NotifikasiModel::class.java)

                        notif?.let {
                            // 🔥 filter khusus staff ini
                            if (it.target_uid == staffId || it.target_role == "staff") {
                                listNotif.add(it)
                            }
                        }
                    }

                    if (listNotif.isNotEmpty()) {

                        // 🔥 ambil notif terbaru
                        val latest = listNotif.maxByOrNull { it.timestamp }

                        val date = Date(latest?.timestamp ?: 0)

                        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val sdfDate =
                            SimpleDateFormat("dd MMM", Locale("id", "ID"))

                        binding?.apply {
                            tvJudulJadwal.text = latest?.judul ?: "-"
                            tvTime.text = sdfTime.format(date)
                            tvDate.text = sdfDate.format(date)
                        }

                    } else {
                        binding?.apply {
                            tvJudulJadwal.text = "Tidak ada notifikasi"
                            tvTime.text = "--:--"
                            tvDate.text = "-"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun bukaLaporanDariTugas(tugas: TugasModel) {

        val bundle = Bundle().apply {
            putString("VILLA_NAMA", tugas.villa_nama)
            putString("RUANGAN_NAMA", tugas.ruangan)
            putString("BARANG_NAMA", tugas.tugas)
        }

        val fragment = LaporanStaffFragment()
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(id, fragment) // 🔥 penting
            .addToBackStack(null)
            .commit()
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

        rootRef.child(FirebaseConfig.PATH_TASK_MANAGEMENT)
            .child(tugas.id)
            .updateChildren(updates)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}