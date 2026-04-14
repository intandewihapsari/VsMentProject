package com.indri.vsmentproject.ui.main

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.indri.vsmentproject.R
import com.indri.vsmentproject.ui.manager.masterdata.DataFragment
import com.indri.vsmentproject.ui.manager.report.LaporanFragment
import com.indri.vsmentproject.ui.common.profile.ProfileFragment
import com.indri.vsmentproject.ui.manager.task.TugasFragment
import com.indri.vsmentproject.databinding.ActivityManagerBinding
import com.indri.vsmentproject.ui.manager.dashboard.DashboardFragment

class ManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inisialisasi Binding
        binding = ActivityManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Tampilan Awal saat aplikasi dibuka (Dashboard)
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
        }

        // 2. Listener Klik Menu Bawah menggunakan Binding
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(DashboardFragment())
                    true
                }
                R.id.navigation_tugas -> {
                    replaceFragment(TugasFragment()) // Sekarang sudah bisa dipanggil
                    true
                }
                R.id.navigation_laporan -> {
                    replaceFragment(LaporanFragment())
                    true
                }
                R.id.navigation_data -> {
                    replaceFragment(DataFragment())
                    true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
        // Di dalam onCreate ManagerActivity
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    binding.tvTitlePage.text = "Home"
                    replaceFragment(DashboardFragment())
                    true
                }
                R.id.navigation_tugas -> {
                    binding.tvTitlePage.text = "Tugas"
                    replaceFragment(TugasFragment())
                    true
                }
                R.id.navigation_laporan -> {
                    binding.tvTitlePage.text = "Laporan"
                    replaceFragment(LaporanFragment())
                    true
                }
                R.id.navigation_data -> {
                    binding.tvTitlePage.text = "Data Villa & Staff"
                    replaceFragment(DataFragment())
                    true
                }
                R.id.navigation_profile -> {
                    binding.tvTitlePage.text = "Profile"
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }
    fun setHeaderVisible(isVisible: Boolean) {
        val header = findViewById<View>(R.id.layoutHeader)
        header.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    // Fungsi bantu untuk ganti fragment (ID fragmentContainer TETAP SAMA)
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}