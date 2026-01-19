package com.indri.vsmentproject

import android.app.Application
import com.indri.vsmentproject.data.utils.CloudinaryHelper

class VsMentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inisialisasi di sini menjamin Cloudinary siap di Fragment mana pun
        CloudinaryHelper.init(this)
    }
}