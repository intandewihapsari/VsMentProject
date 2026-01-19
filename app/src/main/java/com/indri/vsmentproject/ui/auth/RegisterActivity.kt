package com.indri.vsmentproject.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.R
import com.indri.vsmentproject.data.model.user.User
import com.indri.vsmentproject.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            val nama = binding.etNama.text.toString().trim()
            val posisi = binding.etPosisi.text.toString().trim()

            if (email.isEmpty() || pass.length < 6 || nama.isEmpty()) {
                Toast.makeText(this, "Lengkapi data & Password min 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener { result ->
                val uid = result.user?.uid
                // User baru otomatis jadi 'staff'. Intan nanti rubah manual di Firebase ke 'manager'
                val user = User(uid, nama, email, "staff", posisi)

                FirebaseDatabase.getInstance().getReference("master_data/staff")
                    .child(uid!!).setValue(user).addOnSuccessListener {
                        Toast.makeText(this, "Daftar Berhasil!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvToLogin.setOnClickListener { finish() }
    }
}