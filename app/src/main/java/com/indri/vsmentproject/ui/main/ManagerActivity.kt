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

        if (savedInstanceState == null) {
            binding.tvTitlePage.text = "Home"
            replaceFragment(DashboardFragment())
        }

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
                val currentFragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)

                if (currentFragment is DashboardFragment && currentFragment.isNotifOverlayOpen()) {
                    currentFragment.closeNotifOverlay()
                }
                else if (currentFragment is TugasFragment && currentFragment.isFormOverlayOpen()) {
                    currentFragment.closeFormOverlay()
                }
                else if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                }
                else if (binding.bottomNavigation.selectedItemId != R.id.navigation_home) {
                    binding.bottomNavigation.selectedItemId = R.id.navigation_home
                }
                else {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        finish()
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