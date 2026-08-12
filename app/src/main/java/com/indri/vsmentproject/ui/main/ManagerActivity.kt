package com.indri.vsmentproject.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.utils.DatabaseSeeder
import com.indri.vsmentproject.databinding.ActivityManagerBinding
import com.indri.vsmentproject.ui.common.profile.ProfileFragment
import com.indri.vsmentproject.ui.manager.dashboard.DashboardFragment
import com.indri.vsmentproject.ui.manager.masterdata.DataFragment
import com.indri.vsmentproject.ui.manager.report.LaporanFragment
import com.indri.vsmentproject.ui.manager.task.TugasFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerBinding
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // =====================================================================
        // 🔥 JALANKAN SEEDER DI SINI
        // Jalankan sekali saja, setelah data masuk ke Firebase, hapus/komentari lagi kode ini.
        // =====================================================================
//        lifecycleScope.launch(Dispatchers.IO) {
//            DatabaseSeeder.seedMassiveOperationalData()
//        }
        // =====================================================================

        if (savedInstanceState == null) {
            binding.tvTitlePage.text = "Home"
            replaceFragment(DashboardFragment())
        }

        // Inisialisasi logika tombol Back
        setupBackNavigation()

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

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Ambil Fragment yang sedang aktif di container
                val currentFragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)

                // 1. CEK: Apakah sedang di DashboardFragment DAN overlay Kirim Notif/Instruksi sedang terbuka?
                if (currentFragment is DashboardFragment && currentFragment.isNotifOverlayOpen()) {
                    currentFragment.closeNotifOverlay() // Tutup overlay notifikasi saja!
                }
                // 2. CEK: Apakah sedang di TugasFragment DAN ada Form Overlay (Pilih Villa/Input Tugas) yang terbuka?
                else if (currentFragment is TugasFragment && currentFragment.isFormOverlayOpen()) {
                    currentFragment.closeFormOverlay() // Tutup form overlay di TugasFragment
                }
                // 3. CEK: Apakah ada Sub-Fragment di BackStack (misal StaffList, VillaList, DetailTask)?
                else if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack() // Tutup sub-fragment
                }
                // 4. CEK: Jika sedang tidak berada di Tab Home (Tugas/Laporan/Data/Profile)
                else if (binding.bottomNavigation.selectedItemId != R.id.navigation_home) {
                    binding.bottomNavigation.selectedItemId = R.id.navigation_home // Pindah ke Home
                }
                // 5. CEK: Jika sudah di Home tanpa overlay, berikan Toast untuk keluar
                else {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        finish() // Keluar Aplikasi
                    } else {
                        Toast.makeText(
                            this@ManagerActivity,
                            "Tekan sekali lagi untuk keluar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    backPressedTime = System.currentTimeMillis()
                }
            }
        })
    }}