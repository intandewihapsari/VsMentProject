package com.indri.vsmentproject.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.indri.vsmentproject.R
import com.indri.vsmentproject.databinding.ActivityStaffBinding
import com.indri.vsmentproject.ui.common.profile.ProfileFragment
import com.indri.vsmentproject.ui.staff.activity.AktivitasStaffFragment
import com.indri.vsmentproject.ui.staff.dashboard.DashboardStaffFragment
import com.indri.vsmentproject.ui.staff.task.TugasStaffFragment // Pastikan package ini sesuai
import com.indri.vsmentproject.ui.staff.report.LaporanStaffFragment

class StaffActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inisialisasi Edge-to-Edge
        enableEdgeToEdge()

        // 2. Inisialisasi Binding
        binding = ActivityStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Setup Window Insets (Padding System Bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Bottom 0 karena sudah ada Bottom Navigation
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // 4. Load Fragment Pertama kali
        if (savedInstanceState == null) {
            replaceFragment(DashboardStaffFragment())
        }

        // 5. Setup Bottom Navigation & FAB
        setupBottomNav()
        setupFab()
    }

    private fun setupBottomNav() {
        // Agar icon tidak berwarna abu-abu (mengikuti warna asli icon jika perlu)
        binding.bottomNavigation.itemIconTintList = null

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
                R.id.navigation_laporan ->{
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
        // Aksi Tombol Lonceng (FAB Alert)
        binding.fabAlert.setOnClickListener {
            // Contoh aksi darurat
            Toast.makeText(this, "Emergency Alert Dikirim ke Manager!", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi bantu untuk ganti fragment
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}