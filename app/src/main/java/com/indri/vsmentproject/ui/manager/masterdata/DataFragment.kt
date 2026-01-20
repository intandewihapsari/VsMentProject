package com.indri.vsmentproject.ui.manager.masterdata

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indri.vsmentproject.data.model.user.StaffModel
import com.indri.vsmentproject.data.model.villa.VillaModel
import com.indri.vsmentproject.databinding.FragmentDataBinding
import com.indri.vsmentproject.data.utils.FirebaseConfig

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

    // --- LOGIC VILLA (SINKRON DENGAN areas) ---
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
        val opsi = arrayOf("Edit Detail", "Kelola Ruangan", "Hapus Villa")
        AlertDialog.Builder(requireContext()).setItems(opsi) { _, i ->
            when (i) {
                0 -> dialogInputVilla(villa)
                1 -> dialogKelolaRuangan(villa)
                2 -> viewModel.hapusVilla(villa.id)
            }
        }.show()
    }

    private fun dialogKelolaRuangan(villa: VillaModel) {
        // SINKRONISASI: Gunakan 'areas' sesuai JSON baru
        val list = villa.areas.toMutableList()
        AlertDialog.Builder(requireContext())
            .setTitle("Ruangan: ${villa.nama}")
            .setItems(list.toTypedArray()) { _, i ->
                val roomName = list[i]
                AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Ruangan?")
                    .setMessage("Hapus '$roomName' dari daftar?")
                    .setPositiveButton("Hapus") { _, _ ->
                        list.removeAt(i)
                        updateRuangan(villa, list)
                    }.setNegativeButton("Batal", null).show()
            }
            .setPositiveButton("Tambah") { _, _ ->
                val et = EditText(requireContext()).apply { hint = "Nama Ruangan (Misal: Kamar 3)" }
                AlertDialog.Builder(requireContext()).setTitle("Tambah Ruangan").setView(et)
                    .setPositiveButton("Simpan") { _, _ ->
                        if (et.text.isNotEmpty()) {
                            list.add(et.text.toString())
                            updateRuangan(villa, list)
                        }
                    }.show()
            }.show()
    }

    private fun updateRuangan(villa: VillaModel, list: List<String>) {
        // Gunakan field 'areas' agar tidak rusak
        val data = mapOf(
            "nama" to villa.nama,
            "areas" to list,
            "foto" to villa.foto,
            "alamat" to villa.alamat
        )
        viewModel.simpanVilla(villa.id, data)
    }

    private fun dialogInputVilla(villa: VillaModel?) {
        val layout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 40, 50, 0) }
        val etNama = EditText(requireContext()).apply { hint = "Nama Villa"; setText(villa?.nama) }
        val etAlamat = EditText(requireContext()).apply { hint = "Alamat"; setText(villa?.alamat) }
        layout.addView(etNama); layout.addView(etAlamat)

        AlertDialog.Builder(requireContext())
            .setTitle(if (villa == null) "Tambah Villa" else "Edit Villa")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val idFix = villa?.id ?: "V${System.currentTimeMillis().toString().takeLast(3)}"
                val data = mapOf(
                    "id" to idFix,
                    "nama" to etNama.text.toString(),
                    "alamat" to etAlamat.text.toString(),
                    "areas" to (villa?.areas ?: listOf("Umum")),
                    "foto" to (villa?.foto ?: "https://res.cloudinary.com/do8dnkpew/image/upload/v1766219984/WhatsApp_Image_2025-12-19_at_18.16.11_qdynh3.jpg")
                )
                viewModel.simpanVilla(idFix, data)
            }.show()
    }

    // --- LOGIC STAFF (SINKRON DENGAN users/staffs) ---
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
            if (i == 0) dialogInputStaff(st) else viewModel.hapusStaff(st.uid)
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
            else viewModel.simpanStaff(st.uid, mapOf("nama" to etNama.text.toString(), "posisi" to etPosisi.text.toString()))
        }.show()
    }

    private fun registerStaffAuth(email: String, pass: String, nama: String, posisi: String) {
        val options = FirebaseApp.getInstance().options
        val secondaryApp = try { FirebaseApp.initializeApp(requireContext(), options, "secondary") }
        catch (e: Exception) { FirebaseApp.getInstance("secondary") }

        FirebaseAuth.getInstance(secondaryApp).createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                // SINKRONISASI: Path 'users/staffs' & field lengkap
                val data = mapOf(
                    "uid" to uid,
                    "nama" to nama,
                    "email" to email,
                    "posisi" to posisi,
                    "role" to "staff",
                    "foto_profil" to "https://res.cloudinary.com/do8dnkpew/image/upload/v1766219984/WhatsApp_Image_2025-12-19_at_18.16.11_qdynh3.jpg"
                )

                FirebaseDatabase.getInstance().getReference(FirebaseConfig.PATH_STAFFS).child(uid).setValue(data)
                FirebaseAuth.getInstance(secondaryApp).signOut()
                Toast.makeText(requireContext(), "Staff Berhasil Ditambah!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}