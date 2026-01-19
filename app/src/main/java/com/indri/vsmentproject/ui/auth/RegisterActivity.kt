package com.indri.vsmentproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.model.user.UserModel
import com.indri.vsmentproject.databinding.ActivityRegisterBinding
import com.indri.vsmentproject.ui.main.ManagerActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // RegisterActivity.kt
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            val nama = binding.etNama.text.toString().trim()

            auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener { result ->
                val uid = result.user?.uid
                val user = UserModel(uid, nama, email, "manager", "Supervisor") // Langsung set Manager

                FirebaseDatabase.getInstance().getReference("users")
                    .child(uid!!).setValue(user).addOnSuccessListener {
                        Toast.makeText(this, "Berhasil Daftar sebagai Manager", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ManagerActivity::class.java))
                        finish()
                    }
            }.addOnFailureListener { Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show() }
        }

        binding.tvToLogin.setOnClickListener { finish() }
    }
}