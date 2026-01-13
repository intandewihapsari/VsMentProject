package com.indri.vsmentproject.UI.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.indri.vsmentproject.Data.Model.StaffModel
import com.indri.vsmentproject.Data.Model.VillaModel
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
            setPadding(50, 20, 50, 0)
        }
        val etNama = EditText(requireContext()).apply { hint = "Nama Staff"; setText(st?.nama) }
        val etPosisi = EditText(requireContext()).apply { hint = "Posisi"; setText(st?.posisi) }
        layout.addView(etNama)
        layout.addView(etPosisi)

        AlertDialog.Builder(requireContext())
            .setTitle(if (st == null) "Tambah Staff" else "Edit Staff")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                if (etNama.text.isNotEmpty()) {
                    val data = mapOf("nama" to etNama.text.toString(), "posisi" to etPosisi.text.toString())
                    viewModel.simpanStaff(st?.id, data)
                }
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}