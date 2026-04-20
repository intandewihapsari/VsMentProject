package com.indri.vsmentproject.ui.manager.task


import com.indri.vsmentproject.data.model.task.TugasModel

sealed class TugasListItem {
    data class Header(val title: String) : TugasListItem()
    data class Item(val tugas: TugasModel) : TugasListItem()
}