package com.indri.vsmentproject.data.model.task

data class TugasWithHeader(
    val tugas: TugasModel,
    val header: String,
    val showHeader: Boolean
)