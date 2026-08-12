package com.indri.vsmentproject.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.indri.vsmentproject.R
import com.indri.vsmentproject.databinding.ActivityStaffBinding
import com.indri.vsmentproject.ui.common.profile.ProfileFragment
import com.indri.vsmentproject.ui.staff.activity.AktivitasStaffFragment
import com.indri.vsmentproject.ui.staff.dashboard.DashboardStaffFragment
import com.indri.vsmentproject.ui.staff.task.TugasStaffFragment
import com.indri.vsmentproject.ui.staff.report.LaporanStaffFragment
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.database.FirebaseDatabase


class StaffActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // PERBAIKAN: Menggunakan binding, bukan findViewById lagi
        binding.bottomNavigation.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_item_color)

        // Setup Window Insets (Padding System Bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Load Fragment Pertama kali
        if (savedInstanceState == null) {
            replaceFragment(DashboardStaffFragment())
        }

        setupBottomNav()
        setupFab()
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(DashboardStaffFragment())
                    true
                }
                R.id.navigation_tugas -> {
                    replaceFragment(TugasStaffFragment())
                    true
                }
                R.id.navigation_laporan -> {
                    replaceFragment(LaporanStaffFragment())
                    true
                }
                R.id.navigation_aktivitas -> {
                    replaceFragment(AktivitasStaffFragment())
                    true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFab() {
        // PERBAIKAN: Klik FAB memicu Bottom Navigation memilih menu laporan agar UI tetap sinkron
        binding.fab.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.navigation_laporan
            Toast.makeText(this, "Membuka Menu Laporan...", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi bantu untuk ganti fragment
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    fun saveFcmTokenToDatabase(managerId: String, staffUid: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener

            val token = task.result
            if (!token.isNullOrEmpty()) {
                // Simpan fcm_token di bawah profile staff
                FirebaseDatabase.getInstance().reference
                    .child("villa_management")
                    .child(managerId)
                    .child("master_data")
                    .child("staffs")
                    .child(staffUid) // Atau path custom_id staff
                    .child("fcm_token")
                    .setValue(token)
            }
        }
    }
}