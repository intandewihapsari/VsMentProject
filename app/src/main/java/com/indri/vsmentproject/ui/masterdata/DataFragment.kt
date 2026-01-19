package com.indri.vsmentproject.ui.masterdata

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

    // --- LOGIC VILLA & RUANGAN ---
    private fun showVillaList() {
        val villas = viewModel.villaList.value ?: emptyList()
        val names = villas.map { it.nama }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Kelola Villa")
            .setItems(names) { _, i -> dialogOpsiVilla(villas[i]) }
            .setNeutralButton("Tambah Villa Baru") { _, _ -> dialogInputVilla(null) }
            .setNegativeButton("Tutup", null)
            .show()
    }

    private fun dialogOpsiVilla(villa: VillaModel) {
        val opsi = arrayOf("Edit Nama Villa", "Kelola Ruangan/Area", "Hapus Villa")
        AlertDialog.Builder(requireContext())
            .setTitle("Opsi: ${villa.nama}")
            .setItems(opsi) { _, i ->
                when (i) {
                    0 -> dialogInputVilla(villa)
                    1 -> dialogKelolaRuangan(villa)
                    2 -> {
                        viewModel.hapusVilla(villa.id)
                        Toast.makeText(requireContext(), "Villa dihapus", Toast.LENGTH_SHORT).show()
                    }
                }
            }.show()
    }

    private fun dialogKelolaRuangan(villa: VillaModel) {
        val listRuangan = villa.area.toMutableList()
        val displayRuangan = if (listRuangan.isEmpty()) arrayOf("Belum ada ruangan") else listRuangan.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Ruangan di ${villa.nama}")
            .setItems(displayRuangan) { _, i ->
                if (listRuangan.isNotEmpty()) dialogOpsiSatuRuangan(villa, listRuangan, i)
            }
            .setPositiveButton("Tambah Ruangan") { _, _ ->
                dialogInputSatuRuangan(villa, listRuangan, null)
            }
            .setNegativeButton("Kembali", null)
            .show()
    }

    private fun dialogOpsiSatuRuangan(villa: VillaModel, list: MutableList<String>, index: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Ruangan: ${list[index]}")
            .setItems(arrayOf("Edit Nama Ruangan", "Hapus Ruangan")) { _, i ->
                if (i == 0) {
                    dialogInputSatuRuangan(villa, list, index)
                } else {
                    list.removeAt(index)
                    updateRuanganKeFirebase(villa, list)
                }
            }.show()
    }

    private fun dialogInputSatuRuangan(villa: VillaModel, list: MutableList<String>, index: Int?) {
        val et = EditText(requireContext()).apply {
            hint = "Nama Ruangan (Misal: Dapur)"
            if (index != null) setText(list[index])
        }
        AlertDialog.Builder(requireContext())
            .setTitle(if (index == null) "Tambah Ruangan" else "Edit Ruangan")
            .setView(et)
            .setPositiveButton("Simpan") { _, _ ->
                val namaBaru = et.text.toString()
                if (namaBaru.isNotEmpty()) {
                    if (index == null) list.add(namaBaru) else list[index] = namaBaru
                    updateRuanganKeFirebase(villa, list)
                }
            }.show()
    }

    private fun updateRuanganKeFirebase(villa: VillaModel, listBaru: List<String>) {
        val data = mapOf(
            "nama" to villa.nama,
            "area" to listBaru,
            "foto" to villa.foto
        )
        viewModel.simpanVilla(villa.id, data)
        Toast.makeText(requireContext(), "Ruangan diperbarui", Toast.LENGTH_SHORT).show()
    }

    private fun dialogInputVilla(villa: VillaModel?) {
        val etNama = EditText(requireContext()).apply {
            hint = "Nama Villa"
            setText(villa?.nama)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(if (villa == null) "Tambah Villa" else "Edit Nama Villa")
            .setView(etNama)
            .setPositiveButton("Simpan") { _, _ ->
                val namaVilla = etNama.text.toString()
                if (namaVilla.isNotEmpty()) {
                    val data = mutableMapOf<String, Any>("nama" to namaVilla)
                    data["area"] = villa?.area ?: listOf("Umum")
                    data["foto"] = villa?.foto ?: ""
                    viewModel.simpanVilla(villa?.id, data)
                }
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
            .setNegativeButton("Tutup", null)
            .show()
    }

    private fun dialogOpsiStaff(st: StaffModel) {
        AlertDialog.Builder(requireContext())
            .setTitle(st.nama)
            .setItems(arrayOf("Edit Staff", "Hapus Staff")) { _, i ->
                if (i == 0) dialogInputStaff(st) else viewModel.hapusStaff(st.id)
            }.show()
    }


    private fun dialogInputStaff(st: StaffModel?) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 0)
        }

        val etNama = EditText(requireContext()).apply { hint = "Nama Lengkap"; setText(st?.nama) }
        val etPosisi = EditText(requireContext()).apply { hint = "Posisi (Ex: Housekeeping)"; setText(st?.posisi) }

        // TAMBAHKAN EMAIL DAN PASSWORD (Hanya untuk staff baru)
        val etEmail = EditText(requireContext()).apply { hint = "Email Staff" }
        val etPass = EditText(requireContext()).apply { hint = "Password Default (min 6 karakter)" }

        layout.addView(etNama)
        layout.addView(etPosisi)

        if (st == null) { // Jika Tambah Baru, munculkan input login
            layout.addView(etEmail)
            layout.addView(etPass)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (st == null) "Registrasi Staff Baru" else "Edit Profil Staff")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString()
                val posisi = etPosisi.text.toString()

                if (st == null) {
                    // LOGIC TAMBAH STAFF BARU (Auth + Database)
                    val email = etEmail.text.toString()
                    val pass = etPass.text.toString()

                    if (email.isNotEmpty() && pass.length >= 6) {
                        registerStaffAuth(email, pass, nama, posisi)
                    } else {
                        Toast.makeText(requireContext(), "Email/Password tidak valid", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // LOGIC EDIT STAFF LAMA (Hanya Database)
                    val data = mapOf("nama" to nama, "posisi" to posisi)
                    viewModel.simpanStaff(st.id, data)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Fungsi pembantu untuk mendaftarkan ke Firebase Auth
    private fun registerStaffAuth(email: String, pass: String, nama: String, posisi: String) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        // Ambil UID Manager saat ini sebelum dia tertimpa oleh login staff baru
        val managerUid = auth.currentUser?.uid

        Log.d("VSMENT_DEBUG", "Memulai Auth untuk email: $email")

        auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener { result ->
            val staffUid = result.user?.uid
            Log.d("VSMENT_DEBUG", "Auth Sukses! Staff UID: $staffUid")

            val dataStaff = mapOf(
                "uid" to staffUid,
                "nama" to nama,
                "email" to email,
                "role" to "staff",
                "posisi" to posisi,
                "manager_id" to managerUid
            )

            // GUNAKAN INSTANCE DATABASE LANGSUNG
            val dbRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users")

            if (staffUid != null) {
                dbRef.child(staffUid).setValue(dataStaff)
                    .addOnSuccessListener {
                        Log.d("VSMENT_DEBUG", "BERHASIL SIMPAN KE REALTIME DATABASE!")
                        Toast.makeText(requireContext(), "Staff Berhasil Disimpan", Toast.LENGTH_SHORT).show()

                        // Supaya tidak logout otomatis dari Manager,
                        // Kita biarkan saja dulu atau arahkan login ulang.
                    }
                    .addOnFailureListener { e ->
                        Log.e("VSMENT_DEBUG", "GAGAL DATABASE: ${e.message}")
                        Toast.makeText(requireContext(), "Database Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("VSMENT_DEBUG", "GAGAL AUTH: ${e.message}")
            Toast.makeText(requireContext(), "Auth Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}