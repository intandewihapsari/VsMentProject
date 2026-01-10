package com.indri.vsmentproject.Data.Model.tugas

import com.indri.vsmentproject.Data.Model.TugasModel

data class VillaTugasGroup(
    val namaVilla: String,
    val listTugas: List<TugasModel>
)