package com.indri.vsmentproject.data.model.tugas

import com.indri.vsmentproject.data.model.tugas.TugasModel

data class VillaTugasGroup(
    val namaVilla: String,
    val listTugas: List<TugasModel>
)