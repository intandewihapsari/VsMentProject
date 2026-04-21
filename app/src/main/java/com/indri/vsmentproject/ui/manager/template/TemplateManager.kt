package com.indri.vsmentproject.ui.manager.template

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.indri.vsmentproject.databinding.ActivityTemplateManagerBinding

class TemplateManager : AppCompatActivity() {

    private lateinit var binding: ActivityTemplateManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTemplateManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.templateContainer.id, TemplateListFragment())
                .commit()
        }
    }
}