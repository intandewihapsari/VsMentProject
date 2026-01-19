package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.model.user.StaffModel
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.databinding.FragmentDataBinding

class DataFragment : Fragment() {
    private var _binding: FragmentDataBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DataViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getData()
        binding.btnManageVilla.setOnClickListener { showVillaList() }
        binding.btnManageStaff.setOnClickListener { showStaffList() }
    }

    // --- LOGIC VILLA ---
    private fun showVillaList() {
        val villas = viewModel.villaList.value ?: emptyList()
        val names = villas.map { it.nama }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Kelola Villa")
            .setItems(names) { _, i -> dialogOpsiVilla(villas[i]) }
            .setNeutralButton("Tambah Villa Baru") { _, _ -> dialogInputVilla(null) }
            .show()
    }

    private fun dialogOpsiVilla(villa: VillaModel) {
        val opsi = arrayOf("Edit Nama Villa", "Kelola Ruangan", "Hapus Villa")
        AlertDialog.Builder(requireContext()).setItems(opsi) { _, i ->
            when (i) {
                0 -> dialogInputVilla(villa)
                1 -> dialogKelolaRuangan(villa)
                2 -> viewModel.hapusVilla(villa.id)
            }
        }.show()
    }

    private fun dialogKelolaRuangan(villa: VillaModel) {
        val list = villa.area.toMutableList()
        AlertDialog.Builder(requireContext())
            .setTitle("Ruangan: ${villa.nama}")
            .setItems(list.toTypedArray()) { _, i ->
                // Dialog edit/hapus satu ruangan
                list.removeAt(i)
                updateRuangan(villa, list)
            }
            .setPositiveButton("Tambah") { _, _ ->
                val et = EditText(requireContext())
                AlertDialog.Builder(requireContext()).setTitle("Nama Ruangan").setView(et)
                    .setPositiveButton("Simpan") { _, _ ->
                        list.add(et.text.toString())
                        updateRuangan(villa, list)
                    }.show()
            }.show()
    }

    private fun updateRuangan(villa: VillaModel, list: List<String>) {
        val data = mapOf("nama" to villa.nama, "area" to list, "foto" to villa.foto)
        viewModel.simpanVilla(villa.id, data)
    }

    private fun dialogInputVilla(villa: VillaModel?) {
        val et = EditText(requireContext()).apply { setText(villa?.nama) }
        AlertDialog.Builder(requireContext()).setView(et).setPositiveButton("Simpan") { _, _ ->
            val data = mapOf("nama" to et.text.toString(), "area" to (villa?.area ?: listOf("Umum")), "foto" to (villa?.foto ?: ""))
            viewModel.simpanVilla(villa?.id, data)
        }.show()
    }

    // --- LOGIC STAFF ---
    private fun showStaffList() {
        val staff = viewModel.staffList.value ?: emptyList()
        val names = staff.map { "${it.nama} (${it.posisi})" }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Kelola Staff")
            .setItems(names) { _, i -> dialogOpsiStaff(staff[i]) }
            .setNeutralButton("Tambah Staff Baru") { _, _ -> dialogInputStaff(null) }
            .show()
    }

    private fun dialogOpsiStaff(st: StaffModel) {
        AlertDialog.Builder(requireContext()).setItems(arrayOf("Edit", "Hapus")) { _, i ->
            if (i == 0) dialogInputStaff(st) else viewModel.hapusStaff(st.id)
        }.show()
    }

    private fun dialogInputStaff(st: StaffModel?) {
        val layout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 40, 50, 0) }
        val etNama = EditText(requireContext()).apply { hint = "Nama"; setText(st?.nama) }
        val etPosisi = EditText(requireContext()).apply { hint = "Posisi"; setText(st?.posisi) }
        layout.addView(etNama); layout.addView(etPosisi)

        val etEmail = EditText(requireContext()).apply { hint = "Email" }
        val etPass = EditText(requireContext()).apply { hint = "Password" }
        if (st == null) { layout.addView(etEmail); layout.addView(etPass) }

        AlertDialog.Builder(requireContext()).setView(layout).setPositiveButton("Simpan") { _, _ ->
            if (st == null) registerStaffAuth(etEmail.text.toString(), etPass.text.toString(), etNama.text.toString(), etPosisi.text.toString())
            else viewModel.simpanStaff(st.id, mapOf("nama" to etNama.text.toString(), "posisi" to etPosisi.text.toString()))
        }.show()
    }

    private fun registerStaffAuth(email: String, pass: String, nama: String, posisi: String) {
        // Trik: Gunakan secondary Firebase App agar Manager TIDAK Logout
        val options = FirebaseApp.getInstance().options
        val secondaryApp = try {
            FirebaseApp.initializeApp(requireContext(), options, "secondary")
        } catch (e: Exception) {
            FirebaseApp.getInstance("secondary")
        }

        FirebaseAuth.getInstance(secondaryApp).createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                val data = mapOf("id" to uid, "nama" to nama, "email" to email, "posisi" to posisi, "role" to "staff")

                // Simpan ke master_data/staff
                FirebaseDatabase.getInstance().getReference("master_data/staff").child(uid).setValue(data)
                FirebaseAuth.getInstance(secondaryApp).signOut() // Logout staff dari app secondary
                Toast.makeText(requireContext(), "Staff Berhasil!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}