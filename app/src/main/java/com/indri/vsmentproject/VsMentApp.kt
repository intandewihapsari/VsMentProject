package com.indri.vsmentproject

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.utils.CloudinaryHelper

class VsMentApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. ENABLE OFFLINE PERSISTENCE (Must be called before any other DB usage)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // 2. LOCK LIGHT MODE (Mencegah tampilan rusak saat HP penguji di Dark Mode)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // 3. Inisialisasi Cloudinary di sini menjamin siap di Fragment mana pun
        CloudinaryHelper.init(this)
    }
}
