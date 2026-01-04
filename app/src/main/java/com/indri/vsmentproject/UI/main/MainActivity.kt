package com.indri.vsmentproject.UI.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.indri.vsmentproject.databinding.ActivityMainBinding
import com.indri.vsmentproject.ui.dashboard.DashboardFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
           .replace(binding.fragmentContainer.id, DashboardFragment())
           .commit()
   }

}