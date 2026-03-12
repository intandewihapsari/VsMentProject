package com.indri.vsmentproject.ui.staff.home

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.ActivityJadwalPentingBinding
import com.indri.vsmentproject.ui.staff.task.TugasChildAdapter
import com.indri.vsmentproject.ui.staff.report.LaporanStaffFragment // Sesuaikan package laporanmu

class JadwalPentingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJadwalPentingBinding
    private lateinit var dbRef: DatabaseReference
    private var staffId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJadwalPentingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        staffId = sharedPref.getString("staff_id", "") ?: ""

        dbRef = FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_TASK_MANAGEMENT)

        setupRecyclerView()
        loadJadwalDinamis()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.rvJadwalPenting.layoutManager = LinearLayoutManager(this)
    }

    private fun loadJadwalDinamis() {
        if (staffId.isEmpty()) return

        binding.progressBar.visibility = View.VISIBLE

        dbRef.orderByChild("worker_id").equalTo(staffId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isFinishing) return

                    val listPending = mutableListOf<TugasModel>()
                    val listSelesai = mutableListOf<TugasModel>()

                    for (data in snapshot.children) {
                        val tugas = data.getValue(TugasModel::class.java)
                        tugas?.let {
                            it.id = data.key ?: ""
                            if (it.status == "pending") {
                                listPending.add(it)
                            } else {
                                listSelesai.add(it)
                            }
                        }
                    }

                    // --- LOGIKA PENGURUTAN ---

                    // 1. Urutkan Pending: Berdasarkan Deadline (terdekat) lalu Prioritas (High)
                    val sortedPending = listPending.sortedWith(
                        compareBy<TugasModel> { it.deadline }
                            .thenByDescending { it.prioritas == "High" }
                            .thenByDescending { it.prioritas == "Medium" }
                    )

                    // 2. Urutkan Selesai: Berdasarkan waktu selesai terbaru
                    val sortedSelesai = listSelesai.sortedByDescending { it.completed_at }

                    // GABUNGKAN: Pending dulu baru Selesai (Otomatis pindah ke bawah kalau dicentang)
                    val finalList = mutableListOf<TugasModel>()
                    finalList.addAll(sortedPending)
                    finalList.addAll(sortedSelesai)

                    binding.rvJadwalPenting.adapter = TugasChildAdapter(finalList,
                        onDone = { updateStatus(it) },
                        onReport = { bukaLaporanFragment(it) }
                    )

                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility = if (finalList.isEmpty()) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    private fun updateStatus(tugas: TugasModel) {
        val updates = mapOf(
            "status" to "selesai",
            "completed_at" to ServerValue.TIMESTAMP
        )
        dbRef.child(tugas.id).updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Tugas selesai!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bukaLaporanFragment(tugas: TugasModel) {
        // Karena ini Activity, kita panggil Fragment menggunakan SupportFragmentManager
        val bundle = Bundle().apply {
            putString("EXTRA_TUGAS_ID", tugas.id)
            putString("EXTRA_VILLA_NAME", tugas.villa_nama)
            putString("EXTRA_RUANGAN", tugas.ruangan)
        }

        val fragment = LaporanStaffFragment()
        fragment.arguments = bundle

        // Jika kamu menggunakan FrameLayout sebagai container di Activity ini (misal id: fragment_container)
        // Kalau tidak ada container, cara paling umum adalah pindah Activity ke Laporan
        // Tapi jika harus Fragment, kamu butuh sebuah Base Activity untuk menampung fragmentnya.

        Toast.makeText(this, "Membuka laporan untuk ${tugas.tugas}", Toast.LENGTH_SHORT).show()

        // Contoh navigasi jika ada container fragment:
        /*
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
        */
    }
}