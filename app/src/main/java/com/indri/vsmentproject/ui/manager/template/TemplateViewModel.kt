package com.indri.vsmentproject.ui.manager.template

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.indri.vsmentproject.data.model.task.TaskTemplateModel
import com.indri.vsmentproject.data.repository.TaskRepository
import com.indri.vsmentproject.data.utils.Resource

class TemplateViewModel : ViewModel() {

    private val repository = TaskRepository()

    private val _templateList = MutableLiveData<Resource<List<TaskTemplateModel>>>()
    val templateList: LiveData<Resource<List<TaskTemplateModel>>> get() = _templateList

    private val _saveStatus = MutableLiveData<Resource<Boolean>>()
    val saveStatus: LiveData<Resource<Boolean>> get() = _saveStatus

    private val _applyStatus = MutableLiveData<Resource<Boolean>>()
    val applyStatus: LiveData<Resource<Boolean>> get() = _applyStatus

    private val _villaList = MutableLiveData<List<Pair<String, String>>>()
    val villaList: LiveData<List<Pair<String, String>>> get() = _villaList

    private val _staffList = MutableLiveData<List<Pair<String, String>>>()
    val staffList: LiveData<List<Pair<String, String>>> get() = _staffList

    fun fetchTemplates(managerId: String) {
        _templateList.value = Resource.Loading()
        repository.getTemplates(managerId) { list ->
            _templateList.postValue(Resource.Success(list))
        }
    }

    fun fetchVillasAndStaffs(managerId: String) {
        // Fetch Villas
        repository.getVillaList(managerId) { snapshot ->
            val villas = snapshot.children.mapNotNull {
                val id = it.key ?: ""
                val nama = it.child("nama").value?.toString() ?: ""
                if (id.isNotEmpty() && nama.isNotEmpty()) Pair(id, nama) else null
            }
            _villaList.postValue(villas)
        }

        // Fetch Staffs
        com.indri.vsmentproject.data.utils.FirebaseConfig.getStaffsRef(managerId)
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val staffs = snapshot.children.mapNotNull {
                        val id = it.key ?: "" // Menggunakan key staff (STF_...)
                        val nama = it.child("nama").value?.toString() ?: ""
                        if (id.isNotEmpty() && nama.isNotEmpty()) Pair(id, nama) else null
                    }
                    _staffList.postValue(staffs)
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
    }

    fun saveTemplate(managerId: String, template: TaskTemplateModel) {
        _saveStatus.value = Resource.Loading()
        repository.saveTemplate(managerId, template) { success ->
            if (success) {
                _saveStatus.postValue(Resource.Success(true))
                fetchTemplates(managerId)
            } else {
                _saveStatus.postValue(Resource.Error("Gagal menyimpan template"))
            }
        }
    }

    fun deleteTemplate(managerId: String, templateId: String) {
        repository.deleteTemplate(managerId, templateId) { success ->
            if (success) fetchTemplates(managerId)
        }
    }

    fun applyTemplate(
        managerId: String,
        villaId: String,
        villaNama: String,
        ruanganNama: String,
        selectedStaffs: List<Pair<String, String>>,
        template: TaskTemplateModel,
        deadline: String
    ) {
        _applyStatus.value = Resource.Loading()
        repository.applyTemplateToStaff(
            managerId, villaId, villaNama, ruanganNama, selectedStaffs, template, deadline
        ) { success ->
            if (success) {
                _applyStatus.postValue(Resource.Success(true))
            } else {
                _applyStatus.postValue(Resource.Error("Gagal menerapkan template ke staff"))
            }
        }
    }
}
