package com.indri.vsmentproject.ui.staff.home

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.task.TugasModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.ActivityJadwalPentingBinding
import com.indri.vsmentproject.ui.staff.task.TugasChildAdapter
import java.text.SimpleDateFormat
import java.util.*

class JadwalPentingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJadwalPentingBinding
    private lateinit var dbRef: DatabaseReference
    private var staffId: String = ""
    private var listener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJadwalPentingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        staffId = sharedPref.getString("staff_id", "") ?: ""

        dbRef = FirebaseDatabase.getInstance()
            .getReference(FirebaseConfig.PATH_TASK_MANAGEMENT)

        setupRecyclerView()
        loadJadwal()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.rvJadwalPenting.layoutManager = LinearLayoutManager(this)
    }

    private fun loadJadwal() {
        if (staffId.isEmpty()) return

        binding.progressBar.visibility = View.VISIBLE

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val listPending = mutableListOf<TugasModel>()
                val listSelesai = mutableListOf<TugasModel>()

                for (data in snapshot.children) {

                    // 🔥 Ambil TASK langsung (TASK_J01 dll)
                    val tugas = data.getValue(TugasModel::class.java)
                    tugas?.let {
                        it.id = data.key ?: ""

                        if (it.worker_id == staffId) {
                            if (it.status == "pending") {
                                listPending.add(it)
                            } else {
                                listSelesai.add(it)
                            }
                        }
                    }

                    // 🔥 Ambil nested task (V01 > list_tugas)
                    val listTugas = data.child("list_tugas")
                    for (child in listTugas.children) {
                        val tugasVilla = child.getValue(TugasModel::class.java)
                        tugasVilla?.let {
                            it.id = child.key ?: ""

                            if (it.worker_id == staffId || it.worker_name.isNotEmpty()) {
                                if (it.status == "pending") {
                                    listPending.add(it)
                                } else {
                                    listSelesai.add(it)
                                }
                            }
                        }
                    }
                }

                // 🔥 SORTING (ini penting banget)
                val sortedPending = listPending.sortedWith(
                    compareBy<TugasModel> { parseDate(it.deadline) }
                        .thenByDescending { it.prioritas == "High" }
                        .thenByDescending { it.prioritas == "Medium" }
                )

                val sortedSelesai = listSelesai.sortedByDescending {
                    it.completed_at ?: 0
                }

                val finalList = mutableListOf<TugasModel>()
                finalList.addAll(sortedPending)
                finalList.addAll(sortedSelesai)

                binding.rvJadwalPenting.adapter = TugasChildAdapter(
                    finalList,
                    onDone = { updateStatus(it) },
                    onReport = { bukaLaporan(it) }
                )

                binding.progressBar.visibility = View.GONE
                binding.tvEmpty.visibility =
                    if (finalList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
            }
        }

        dbRef.addValueEventListener(listener!!)
    }

    private fun parseDate(dateStr: String?): Long {
        return try {
            val format1 = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val format2 = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            val date = format1.parse(dateStr ?: "")
                ?: format2.parse(dateStr ?: "")

            date?.time ?: Long.MAX_VALUE
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }

    private fun updateStatus(tugas: TugasModel) {
        val updates = mapOf(
            "status" to "selesai",
            "completed_at" to ServerValue.TIMESTAMP
        )

        dbRef.child(tugas.id).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Tugas selesai!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bukaLaporan(tugas: TugasModel) {
        Toast.makeText(
            this,
            "Laporan untuk ${tugas.tugas}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.let { dbRef.removeEventListener(it) }
    }
}