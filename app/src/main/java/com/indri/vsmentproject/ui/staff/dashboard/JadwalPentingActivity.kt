package com.indri.vsmentproject.ui.staff.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.indri.vsmentproject.data.model.notification.NotifikasiModel
import com.indri.vsmentproject.data.utils.FirebaseConfig
import com.indri.vsmentproject.databinding.ActivityJadwalPentingBinding
import com.indri.vsmentproject.R

class JadwalPentingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJadwalPentingBinding
    private lateinit var dbRef: DatabaseReference

    private var staffId: String = ""
    private var listener: ValueEventListener? = null

    private val listData = mutableListOf<NotifikasiModel>()
    private lateinit var adapter: RecyclerView.Adapter<VH>

    // =========================
    // VIEW HOLDER (ONLY 1 - FIXED)
    // =========================
    private inner class VH(view: View) : RecyclerView.ViewHolder(view) {

        val title: TextView = view.findViewById(R.id.tvNamaTugas)
        val pesan: TextView = view.findViewById(R.id.tvNamaVilla)
        val check: ImageView = view.findViewById(R.id.cbRead)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJadwalPentingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        staffId = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            .getString("staff_id", "") ?: ""

        dbRef = FirebaseDatabase.getInstance()
            .getReference(FirebaseConfig.PATH_NOTIFIKASI)

        setupRecycler()
        loadData()

        binding.btnBack.setOnClickListener { finish() }
    }

    // =========================
    // SETUP RECYCLER
    // =========================
    private fun setupRecycler() {

        binding.rvJadwalPenting.layoutManager = LinearLayoutManager(this)

        adapter = object : RecyclerView.Adapter<VH>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notifikasi, parent, false)
                return VH(view)
            }

            override fun getItemCount(): Int = listData.size

            override fun onBindViewHolder(holder: VH, position: Int) {

                val item = listData[position]

                holder.title.text = item.judul
                holder.pesan.text = item.pesan

                // status read icon
                holder.check.setImageResource(
                    if (item.is_read)
                        R.drawable.ic_circle_checked
                    else
                        R.drawable.ic_circle_outline
                )

                // mark as read
                holder.check.setOnClickListener {
                    markAsRead(item)
                }

                holder.itemView.setOnClickListener {
                    Toast.makeText(
                        this@JadwalPentingActivity,
                        item.judul,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                android.util.Log.d("TEST_NOTIF", "pesan = ${item.pesan}")
            }
        }

        binding.rvJadwalPenting.adapter = adapter
    }

    // =========================
    // LOAD DATA FIREBASE
    // =========================
    private fun loadData() {

        if (staffId.isEmpty()) return

        binding.progressBar.visibility = View.VISIBLE

        listener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                listData.clear()

                for (data in snapshot.children) {

                    val item = data.getValue(NotifikasiModel::class.java)

                    item?.let {
                        it.id = data.key ?: ""

                        if (it.target_uid == staffId || it.target_role == "staff") {
                            listData.add(it)
                        }
                    }
                }

                listData.sortByDescending { it.timestamp ?: 0L }

                binding.progressBar.visibility = View.GONE
                binding.tvEmpty.visibility =
                    if (listData.isEmpty()) View.VISIBLE else View.GONE

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@JadwalPentingActivity,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dbRef.addValueEventListener(listener!!)
    }

    // =========================
    // UPDATE IS_READ
    // =========================
    private fun markAsRead(notif: NotifikasiModel) {

        dbRef.child(notif.id)
            .updateChildren(mapOf("is_read" to true))
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.let { dbRef.removeEventListener(it) }
    }
}