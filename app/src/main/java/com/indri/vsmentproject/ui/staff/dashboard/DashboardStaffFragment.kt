package com.indri.vsmentproject.ui.staff.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.FragmentDashboardStaffBinding
import com.indri.vsmentproject.ui.staff.report.LaporanStaffFragment
import com.indri.vsmentproject.ui.staff.task.TugasChildAdapter
import java.text.SimpleDateFormat
import java.util.*

class DashboardStaffFragment : Fragment() {

    private var _binding: FragmentDashboardStaffBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    private val listTugasHome = mutableListOf<TugasModel>()
    private var managerId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardStaffBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchManagerIdAndLoadData()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN_TEST", "Token HP Kamu: $token")
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvTugasHome.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private fun fetchManagerIdAndLoadData() {
        val uid = auth.currentUser?.uid ?: return

        // Step 1: Dapatkan Manager ID dari user_mapping
        db.child(FirebaseConfig.PATH_USER_MAPPING).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    managerId =
                        snapshot.child(FirebaseConfig.FIELD_BELONGS_TO_MANAGER).value.toString()
                    if (managerId != "null") {
                        loadDashboardTasks(managerId!!, uid)
                        loadJadwalPenting(managerId!!, uid)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadDashboardTasks(mId: String, staffUid: String) {
        // Path: operational/task_management
        FirebaseConfig.getTaskManagementRef(mId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    listTugasHome.clear()

                    var total = 0
                    var selesai = 0
                    var pending = 0

                    snapshot.children.forEach { villaSnap ->
                        villaSnap.child("list_tugas").children.forEach { tugasSnap ->
                            val tugas = tugasSnap.getValue(TugasModel::class.java)
                            if (tugas?.staff_id == staffUid) {
                                total++
                                if (tugas.status == FirebaseConfig.STATUS_DONE) {
                                    selesai++
                                } else {
                                    pending++
                                    listTugasHome.add(tugas.apply { id = tugasSnap.key ?: "" })
                                }
                            }
                        }
                    }

                    updateStatUI(total, pending, selesai)

                    val sortedPending = listTugasHome.sortedByDescending { it.created_at }
                    binding.rvTugasHome.adapter = TugasChildAdapter(
                        sortedPending,
                        onDone = { t -> updateStatusTugas(t) },
                        onReport = { t -> bukaLaporanDariTugas(t) },
                        isLastTask = sortedPending.size == 1
                    )
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadJadwalPenting(mId: String, staffUid: String) {
        // Path: operational/notifikasi
        FirebaseConfig.getNotifikasiRef(mId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    val listNotif =
                        snapshot.children.mapNotNull { it.getValue(NotifikasiModel::class.java) }
                            .filter { it.target_uid == staffUid || it.target_role == "staff" }

                    if (listNotif.isNotEmpty()) {
                        val latest = listNotif.maxByOrNull { it.timestamp }
                        val date = Date(latest?.timestamp ?: 0)
                        binding.apply {
                            tvJudulJadwal.text = latest?.judul ?: "-"
                            tvPesan.text = latest?.pesan ?: "-"
                            tvTime.text =
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                            tvDate.text =
                                SimpleDateFormat("dd MMM", Locale("id", "ID")).format(date)
                        }
                    } else {
                        binding.tvJudulJadwal.text = "Tidak ada notifikasi"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateStatusTugas(tugas: TugasModel) {
        val mId = managerId ?: return
        val updates = mapOf(
            "status" to FirebaseConfig.STATUS_DONE,
            "completed_at" to System.currentTimeMillis()
        )

        // Path: operational/task_management/[villaId]/list_tugas/[taskId]
        FirebaseConfig.getTaskManagementRef(mId)
            .child(tugas.villa_id)
            .child("list_tugas")
            .child(tugas.id)
            .updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Tugas selesai!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bukaLaporanDariTugas(tugas: TugasModel) {
        val fragment = LaporanStaffFragment().apply {
            arguments = Bundle().apply {
                putString("VILLA_NAMA", tugas.villa_nama)
                putString("RUANGAN_NAMA", tugas.ruangan)
                putString("BARANG_NAMA", tugas.tugas)
                putString("TASK_ID", tugas.id) // Pass Task ID for auto-completion
                putString("VILLA_ID", tugas.villa_id) // Pass Villa ID for targeting
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun updateStatUI(total: Int, pending: Int, selesai: Int) {
        binding.itemTotal.tvCount.text = total.toString()
        binding.itemPending.tvCount.text = selesai.toString()
        binding.itemDone.tvCount.text = pending.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
