package com.indri.vsmentproject.data.model.task

data class TaskTemplateModel(
    var id: String = "",
    val name: String = "",
    val kategori: String = "",
    val tasks: List<String> = emptyList()
)